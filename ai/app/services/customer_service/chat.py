"""AI 客服对话服务：意图分流 → 业务工具 / 混合检索 → 置信度兜底。

会话历史保存在进程内存（按 session_id 隔离）。分布式部署时可换成 Redis 等共享存储。
"""

from __future__ import annotations

import threading
from typing import Any, AsyncIterator

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import AIMessage, BaseMessage, HumanMessage, SystemMessage

from app.config import settings
from app.prompts.customer_service import (
    BUSINESS_ANSWER_SYSTEM,
    BUSINESS_SYSTEM,
    CUSTOMER_SERVICE_SYSTEM_TEMPLATE,
    FALLBACK_SYSTEM,
    NO_CONTEXT,
    OFF_TOPIC_SYSTEM,
)
from app.schemas.customer_service import ChatMessage
from app.services.customer_service.business import (
    BusinessDataService,
    classify_business_domain,
)
from app.services.customer_service.intent import Intent, IntentRouter
from app.services.customer_service.retrieval import HybridRetriever

_CONTEXT_PREFIX = "以下是可参考的平台知识，回答时优先依据这些内容：\n"


def _as_text(content: Any) -> str:
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return "".join(
            p.get("text", "") if isinstance(p, dict) else str(p) for p in content
        )
    return str(content)


class ChatService:
    def __init__(
        self,
        llm: BaseChatModel,
        vector_store: Any | None = None,
        top_k: int | None = None,
        router: IntentRouter | None = None,
        rewriter: Any | None = None,
        retriever: HybridRetriever | None = None,
        flywheel: Any | None = None,
        business: BusinessDataService | None = None,
        confidence_threshold: float | None = None,
    ):
        self._llm = llm
        self._top_k = top_k if top_k is not None else settings.rag_top_k
        self._router = router
        self._rewriter = rewriter
        self._flywheel = flywheel
        self._business = business
        self._confidence_threshold = (
            confidence_threshold
            if confidence_threshold is not None
            else settings.cs_confidence_threshold
        )
        if retriever is not None:
            self._retriever = retriever
        elif vector_store is not None:
            self._retriever = HybridRetriever(vector_store=vector_store)
        else:
            self._retriever = None
        self._histories: dict[str, list[BaseMessage]] = {}
        self._lock = threading.Lock()

    @staticmethod
    def _to_message(m: ChatMessage) -> BaseMessage:
        if m.role == "user":
            return HumanMessage(content=m.content)
        if m.role == "assistant":
            return AIMessage(content=m.content)
        return SystemMessage(content=m.content)

    def _get_history(
        self, session_id: str, seed: list[ChatMessage] | None
    ) -> list[BaseMessage]:
        with self._lock:
            if session_id not in self._histories:
                self._histories[session_id] = [
                    self._to_message(m) for m in (seed or [])
                ]
            return self._histories[session_id]

    def _classify(self, message: str, history: list[BaseMessage]) -> Intent:
        if self._router is not None:
            return self._router.classify(message, history)
        return Intent.KNOWLEDGE

    def _log_flywheel(self, message: str, intent: Intent, confidence: float) -> None:
        if self._flywheel is None:
            return
        try:
            self._flywheel.log(message, intent.value, confidence)
        except Exception:
            pass

    def _knowledge_messages(
        self, message: str, history: list[BaseMessage]
    ) -> list[BaseMessage]:
        context = NO_CONTEXT
        if self._retriever is not None:
            query = message
            if self._rewriter is not None and history:
                query = self._rewriter.rewrite(message, history)
            retrieved, confidence = self._retriever.retrieve(query, self._top_k)
            if confidence is None:
                context = NO_CONTEXT
            elif not retrieved or confidence < self._confidence_threshold:
                self._log_flywheel(message, Intent.KNOWLEDGE, confidence)
                return [SystemMessage(content=FALLBACK_SYSTEM), HumanMessage(content=message)]
            else:
                context = _CONTEXT_PREFIX + retrieved
        system = SystemMessage(
            content=CUSTOMER_SERVICE_SYSTEM_TEMPLATE.format(context=context)
        )
        return [system, *history, HumanMessage(content=message)]

    def _build_messages(
        self, session_id: str, message: str, seed: list[ChatMessage] | None
    ) -> list[BaseMessage]:
        history = self._get_history(session_id, seed)
        intent = self._classify(message, history)
        if intent == Intent.OFF_TOPIC:
            return [SystemMessage(content=OFF_TOPIC_SYSTEM), HumanMessage(content=message)]
        if intent == Intent.BUSINESS:
            return [SystemMessage(content=BUSINESS_SYSTEM), HumanMessage(content=message)]
        return self._knowledge_messages(message, history)

    async def _business_messages_async(
        self, message: str, token: str | None
    ) -> list[BaseMessage]:
        data = ""
        if token and self._business is not None:
            domain = classify_business_domain(message)
            if domain:
                data = await self._business.query(domain, token)
        if data:
            system = SystemMessage(content=BUSINESS_ANSWER_SYSTEM + data)
        else:
            system = SystemMessage(content=BUSINESS_SYSTEM)
        return [system, HumanMessage(content=message)]

    async def _abuild_messages(
        self,
        session_id: str,
        message: str,
        seed: list[ChatMessage] | None,
        token: str | None,
    ) -> list[BaseMessage]:
        history = self._get_history(session_id, seed)
        intent = self._classify(message, history)
        if intent == Intent.OFF_TOPIC:
            return [SystemMessage(content=OFF_TOPIC_SYSTEM), HumanMessage(content=message)]
        if intent == Intent.BUSINESS:
            return await self._business_messages_async(message, token)
        return self._knowledge_messages(message, history)

    def _append(self, session_id: str, user_msg: str, assistant_msg: str) -> None:
        with self._lock:
            self._histories.setdefault(session_id, []).extend(
                [HumanMessage(content=user_msg), AIMessage(content=assistant_msg)]
            )

    def chat(self, session_id: str, message: str, history: list[ChatMessage] | None = None) -> str:
        messages = self._build_messages(session_id, message, history)
        reply = self._llm.invoke(messages)
        content = _as_text(reply.content)
        self._append(session_id, message, content)
        return content

    async def achat(
        self,
        session_id: str,
        message: str,
        history: list[ChatMessage] | None = None,
        token: str | None = None,
    ) -> str:
        messages = await self._abuild_messages(session_id, message, history, token)
        reply = await self._llm.ainvoke(messages)
        content = _as_text(reply.content)
        self._append(session_id, message, content)
        return content

    async def astream(
        self,
        session_id: str,
        message: str,
        history: list[ChatMessage] | None = None,
        token: str | None = None,
    ) -> AsyncIterator[str]:
        messages = await self._abuild_messages(session_id, message, history, token)
        chunks: list[str] = []
        async for chunk in self._llm.astream(messages):
            text = _as_text(chunk.content)
            if text:
                chunks.append(text)
                yield text
        self._append(session_id, message, "".join(chunks))

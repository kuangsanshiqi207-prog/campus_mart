"""AI 客服对话服务：RAG + 会话历史。

会话历史保存在进程内存（按 session_id 隔离）。
分布式部署时可将 self._histories 替换为 Redis 等共享存储。
"""

from __future__ import annotations

import threading
from typing import Any, AsyncIterator

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import AIMessage, BaseMessage, HumanMessage, SystemMessage

from app.config import settings
from app.schemas.customer_service import ChatMessage
from app.services.customer_service.rag import build_system_prompt


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
    ):
        self._llm = llm
        self._vector_store = vector_store
        self._top_k = top_k if top_k is not None else settings.rag_top_k
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

    def _build_messages(
        self, session_id: str, message: str, seed: list[ChatMessage] | None
    ) -> list[BaseMessage]:
        history = self._get_history(session_id, seed)
        system = SystemMessage(
            content=build_system_prompt(self._vector_store, message, self._top_k)
        )
        return [system, *history, HumanMessage(content=message)]

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

    async def achat(self, session_id: str, message: str, history: list[ChatMessage] | None = None) -> str:
        messages = self._build_messages(session_id, message, history)
        reply = await self._llm.ainvoke(messages)
        content = _as_text(reply.content)
        self._append(session_id, message, content)
        return content

    async def astream(
        self, session_id: str, message: str, history: list[ChatMessage] | None = None
    ) -> AsyncIterator[str]:
        messages = self._build_messages(session_id, message, history)
        chunks: list[str] = []
        async for chunk in self._llm.astream(messages):
            text = _as_text(chunk.content)
            if text:
                chunks.append(text)
                yield text
        self._append(session_id, message, "".join(chunks))

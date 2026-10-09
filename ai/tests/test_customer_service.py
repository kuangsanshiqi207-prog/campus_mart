"""AI 客服模块单元测试。"""

from __future__ import annotations

from langchain_core.language_models.fake_chat_models import FakeListChatModel

from app.prompts.customer_service import NO_CONTEXT
from app.schemas.customer_service import ChatMessage
from app.services.customer_service.chat import ChatService
from app.services.customer_service.rag import build_context, build_system_prompt


def test_build_context_without_store() -> None:
    assert build_context(None, "query", 4) == NO_CONTEXT


def test_build_system_prompt_without_store() -> None:
    prompt = build_system_prompt(None, "query", 4)
    assert "校集" in prompt
    assert NO_CONTEXT in prompt


def test_chat_returns_reply_and_memory(cs_service: ChatService) -> None:
    reply = cs_service.chat("s1", "你好")
    assert reply == "这是测试回复。"
    assert len(cs_service._histories["s1"]) == 2  # 用户消息 + 助手回复


def test_chat_seeds_history_on_first_call() -> None:
    llm = FakeListChatModel(responses=["ok"])
    service = ChatService(llm, vector_store=None, top_k=4)
    seed = [ChatMessage(role="user", content="之前的问题")]
    service.chat("s2", "新问题", history=seed)
    # 历史 = seed(1 条) + 当前用户消息 + 助手回复
    assert len(service._histories["s2"]) == 3

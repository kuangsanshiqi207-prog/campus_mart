"""测试夹具。"""

from __future__ import annotations

import pytest
from langchain_core.language_models.fake_chat_models import FakeListChatModel

from app.services.customer_service.chat import ChatService


@pytest.fixture
def fake_llm() -> FakeListChatModel:
    return FakeListChatModel(responses=["这是测试回复。"])


@pytest.fixture
def cs_service(fake_llm: FakeListChatModel) -> ChatService:
    return ChatService(fake_llm, vector_store=None, top_k=4)

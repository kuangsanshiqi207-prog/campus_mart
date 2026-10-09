"""AI 客服相关 schema。"""

from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class ChatMessage(BaseModel):
    """一条对话消息。"""

    role: Literal["user", "assistant", "system"] = "user"
    content: str


class ChatRequest(BaseModel):
    """客服对话请求。"""

    session_id: str = Field(default="default", description="会话 ID，用于隔离历史")
    message: str = Field(description="用户最新消息")
    history: list[ChatMessage] = Field(
        default_factory=list, description="可选：首次调用时可传入客户端历史"
    )


class ChatReply(BaseModel):
    """客服同步回复。"""

    session_id: str
    content: str = Field(description="助手完整回复")

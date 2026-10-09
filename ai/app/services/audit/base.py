"""审核器基类：把文本 + 图片拼成多模态消息，调用结构化输出。"""

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import HumanMessage, SystemMessage

from app.core.images import to_image_content
from app.schemas.audit import AuditDecision, AuditRequest


class BaseAuditor(ABC):
    """审核器基类。子类只需定义 audit_type / rules / render_text。"""

    #: 审核类型标识（与 AuditType 枚举值一致）
    audit_type: str = ""
    #: 系统提示词（判定规则）
    rules: str = ""

    def __init__(self, llm: BaseChatModel):
        self._llm = llm
        # 绑定结构化输出：让模型严格返回 AuditDecision 结构
        self._structured = llm.with_structured_output(AuditDecision)

    @abstractmethod
    def render_text(self, request: AuditRequest) -> str:
        """把 request.text_fields 渲染成给模型看的文本。"""

    def _build_messages(self, request: AuditRequest) -> list[Any]:
        parts: list[dict[str, Any]] = [
            {"type": "text", "text": self.render_text(request)}
        ]
        if request.image_urls:
            parts.append(
                {"type": "text", "text": "\n\n以下是相关图片，请结合图片内容一起判断："}
            )
            for url in request.image_urls:
                parts.append(to_image_content(url))
        return [
            SystemMessage(content=self.rules),
            HumanMessage(content=parts),
        ]

    def audit(self, request: AuditRequest) -> AuditDecision:
        return self._structured.invoke(self._build_messages(request))

    async def aaudit(self, request: AuditRequest) -> AuditDecision:
        return await self._structured.ainvoke(self._build_messages(request))

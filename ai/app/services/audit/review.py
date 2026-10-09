"""评论审核。"""

from __future__ import annotations

from app.prompts.audit import REVIEW_RULES
from app.schemas.audit import AuditRequest
from app.services.audit.base import BaseAuditor


class ReviewAuditor(BaseAuditor):
    audit_type = "review"
    rules = REVIEW_RULES

    def render_text(self, request: AuditRequest) -> str:
        f = request.text_fields
        return "请审核以下评论内容：\n" f"- 评论：{f.get('content', '')}"

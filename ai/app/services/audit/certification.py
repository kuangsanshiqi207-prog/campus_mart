"""实名/学生认证审核。"""

from __future__ import annotations

from app.prompts.audit import CERTIFICATION_RULES
from app.schemas.audit import AuditRequest
from app.services.audit.base import BaseAuditor


class CertificationAuditor(BaseAuditor):
    audit_type = "certification"
    rules = CERTIFICATION_RULES

    def render_text(self, request: AuditRequest) -> str:
        f = request.text_fields
        lines = [
            "请审核以下认证材料：",
            f"- 学校：{f.get('school', '')}",
            f"- 学号：{f.get('studentNo', '')}",
            f"- 真实姓名：{f.get('realName', '')}",
        ]
        return "\n".join(lines)

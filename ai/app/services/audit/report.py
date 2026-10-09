"""举报审核。"""

from __future__ import annotations

from app.prompts.audit import REPORT_RULES
from app.schemas.audit import AuditRequest
from app.services.audit.base import BaseAuditor


class ReportAuditor(BaseAuditor):
    audit_type = "report"
    rules = REPORT_RULES

    def render_text(self, request: AuditRequest) -> str:
        f = request.text_fields
        lines = [
            "请审核以下举报信息：",
            f"- 举报对象类型：{f.get('targetType', '')}",
            f"- 举报对象 ID：{f.get('targetId', '')}",
            f"- 举报理由：{f.get('reason', '')}",
            f"- 详细描述：{f.get('description', '')}",
        ]
        return "\n".join(lines)

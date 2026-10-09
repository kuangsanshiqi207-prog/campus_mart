"""商品审核。"""

from __future__ import annotations

from app.prompts.audit import PRODUCT_RULES
from app.schemas.audit import AuditRequest
from app.services.audit.base import BaseAuditor


class ProductAuditor(BaseAuditor):
    audit_type = "product"
    rules = PRODUCT_RULES

    def render_text(self, request: AuditRequest) -> str:
        f = request.text_fields
        lines = [
            "请审核以下商品信息：",
            f"- 标题：{f.get('title', '')}",
            f"- 描述：{f.get('description', '')}",
            f"- 价格：{f.get('price', '')}",
            f"- 成色：{f.get('quality', '')}",
            f"- 交易方式：{f.get('tradeType', '')}",
            f"- 交易地点：{f.get('tradePlace', '')}",
        ]
        return "\n".join(lines)

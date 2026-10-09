"""AI 审核相关 schema。"""

from __future__ import annotations

from enum import Enum
from typing import Any, Literal

from pydantic import BaseModel, Field


class AuditType(str, Enum):
    """审核类型，与平台的管理端审核对象一一对应。"""

    product = "product"              # 商品
    review = "review"                # 评论
    report = "report"                # 举报
    certification = "certification"  # 实名/学生认证


RiskLevel = Literal["low", "medium", "high"]


class AuditRequest(BaseModel):
    """AI 审核入参。

    ``text_fields`` 的键名对齐 Java 实体字段名（title/description/price 等），
    方便 Java 侧组装请求体时无需二次映射。
    """

    audit_type: AuditType = Field(description="审核类型")
    text_fields: dict[str, str] = Field(
        default_factory=dict, description="文本字段，键对齐实体字段名"
    )
    image_urls: list[str] = Field(
        default_factory=list, description="图片 URL 或 base64 data URL"
    )
    extra: dict[str, Any] = Field(
        default_factory=dict, description="其他附加信息（可空）"
    )


class AuditDecision(BaseModel):
    """AI 审核结果（结构化输出）。"""

    passed: bool = Field(description="是否通过审核")
    reason: str = Field(default="", description="判定理由")
    risk_category: str = Field(
        default="正常",
        description="风险类别，如：违禁品/色情低俗/虚假信息/联系方式引流/辱骂攻击/身份存疑/正常",
    )
    risk_level: RiskLevel = Field(default="low", description="风险等级 low/medium/high")
    suggested_action: str = Field(
        default="通过", description="建议处置，如：通过/驳回/人工复核"
    )

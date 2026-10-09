"""审核类型注册表 + 审核服务（按 audit_type 分发）。"""

from __future__ import annotations

from langchain_core.language_models.chat_models import BaseChatModel

from app.core.exceptions import AuditTypeNotFound
from app.schemas.audit import AuditDecision, AuditRequest
from app.services.audit.base import BaseAuditor
from app.services.audit.certification import CertificationAuditor
from app.services.audit.product import ProductAuditor
from app.services.audit.report import ReportAuditor
from app.services.audit.review import ReviewAuditor


def build_auditors(llm: BaseChatModel) -> dict[str, BaseAuditor]:
    return {
        ProductAuditor.audit_type: ProductAuditor(llm),
        ReviewAuditor.audit_type: ReviewAuditor(llm),
        ReportAuditor.audit_type: ReportAuditor(llm),
        CertificationAuditor.audit_type: CertificationAuditor(llm),
    }


class AuditService:
    """对外审核服务：根据 audit_type 分发到对应审核器。"""

    def __init__(self, llm: BaseChatModel):
        self._auditors = build_auditors(llm)

    def _get_auditor(self, audit_type: str) -> BaseAuditor:
        auditor = self._auditors.get(audit_type)
        if auditor is None:
            raise AuditTypeNotFound(f"不支持的审核类型：{audit_type}")
        return auditor

    def audit(self, request: AuditRequest) -> AuditDecision:
        return self._get_auditor(request.audit_type.value).audit(request)

    async def aaudit(self, request: AuditRequest) -> AuditDecision:
        return await self._get_auditor(request.audit_type.value).aaudit(request)

"""AI 审核接口。"""

from __future__ import annotations

from fastapi import APIRouter, Depends

from app.api.deps import get_audit_service
from app.schemas.audit import AuditDecision, AuditRequest
from app.schemas.common import ApiResponse
from app.services.audit.registry import AuditService

router = APIRouter(prefix="/audit", tags=["AI审核"])


@router.post("", response_model=ApiResponse[AuditDecision], summary="AI 审核")
async def audit(
    request: AuditRequest,
    service: AuditService = Depends(get_audit_service),
) -> ApiResponse[AuditDecision]:
    decision = await service.aaudit(request)
    return ApiResponse.success(decision)

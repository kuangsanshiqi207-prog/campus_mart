"""API 层冒烟测试（依赖注入 mock，不调用真实 LLM）。"""

from __future__ import annotations

from unittest.mock import AsyncMock, MagicMock

from fastapi.testclient import TestClient

from app.api.deps import get_audit_service
from app.main import app
from app.schemas.audit import AuditDecision
from app.services.audit.registry import AuditService


def test_health() -> None:
    with TestClient(app) as client:
        resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json()["code"] == 1


def test_audit_endpoint() -> None:
    svc = AuditService(MagicMock())
    auditor = svc._auditors["product"]
    auditor._structured.ainvoke = AsyncMock(
        return_value=AuditDecision(
            passed=False, reason="涉及违禁品", risk_category="违禁品", risk_level="high"
        )
    )

    app.dependency_overrides[get_audit_service] = lambda: svc
    try:
        with TestClient(app) as client:
            resp = client.post(
                "/api/v1/audit",
                json={"audit_type": "product", "text_fields": {"title": "出售香烟"}},
            )
    finally:
        app.dependency_overrides.clear()

    assert resp.status_code == 200
    body = resp.json()
    assert body["code"] == 1
    assert body["data"]["passed"] is False
    assert body["data"]["risk_category"] == "违禁品"

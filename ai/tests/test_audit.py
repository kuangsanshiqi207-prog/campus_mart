"""AI 审核模块单元测试。"""

from __future__ import annotations

from unittest.mock import MagicMock

import pytest

from app.core.exceptions import AuditTypeNotFound
from app.core.images import to_image_content
from app.schemas.audit import AuditDecision, AuditRequest, AuditType
from app.services.audit.product import ProductAuditor
from app.services.audit.registry import AuditService, build_auditors


def test_to_image_content_http() -> None:
    assert to_image_content("https://example.com/a.jpg") == {
        "type": "image_url",
        "image_url": {"url": "https://example.com/a.jpg"},
    }


def test_to_image_content_data_url() -> None:
    url = "data:image/png;base64,AAAA"
    assert to_image_content(url)["image_url"]["url"] == url


def test_to_image_content_invalid() -> None:
    with pytest.raises(ValueError):
        to_image_content("ftp://example.com/a.jpg")


def test_product_render_text() -> None:
    auditor = ProductAuditor(MagicMock())
    req = AuditRequest(
        audit_type=AuditType.product,
        text_fields={"title": "iPhone 13", "description": "自用", "price": "3000"},
    )
    text = auditor.render_text(req)
    assert "iPhone 13" in text
    assert "3000" in text


def test_build_messages_contains_image() -> None:
    auditor = ProductAuditor(MagicMock())
    req = AuditRequest(
        audit_type=AuditType.product,
        text_fields={"title": "x"},
        image_urls=["https://example.com/a.jpg"],
    )
    messages = auditor._build_messages(req)
    # messages[0] 为 system，messages[1] 为 human（多模态列表）
    human_content = messages[1].content
    assert isinstance(human_content, list)
    assert any(p.get("type") == "image_url" for p in human_content)


def test_build_auditors_has_four_types() -> None:
    auditors = build_auditors(MagicMock())
    assert set(auditors) == {"product", "review", "report", "certification"}


def test_audit_service_dispatch() -> None:
    service = AuditService(MagicMock())
    req = AuditRequest(audit_type=AuditType.product, text_fields={"title": "x"})
    auditor = service._auditors["product"]
    auditor._structured.invoke.return_value = AuditDecision(passed=True, reason="ok")
    decision = service.audit(req)
    assert decision.passed is True


def test_audit_service_unknown_type() -> None:
    service = AuditService(MagicMock())
    with pytest.raises(AuditTypeNotFound):
        service._get_auditor("unknown")

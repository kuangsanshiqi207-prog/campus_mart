"""FastAPI 依赖注入。"""

from __future__ import annotations

from functools import lru_cache

from app.services.audit.registry import AuditService
from app.services.customer_service.chat import ChatService
from app.services.customer_service.vector_store import get_vector_store
from app.services.llm import get_llm


@lru_cache
def get_audit_service() -> AuditService:
    return AuditService(get_llm())


@lru_cache
def get_cs_service() -> ChatService:
    return ChatService(get_llm(), get_vector_store())

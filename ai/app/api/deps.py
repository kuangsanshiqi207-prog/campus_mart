"""FastAPI 依赖注入。"""

from __future__ import annotations

from functools import lru_cache

from app.config import settings
from app.services.audit.registry import AuditService
from app.services.customer_service.business import BusinessDataService
from app.services.customer_service.chat import ChatService
from app.services.customer_service.flywheel import QuestionPool
from app.services.customer_service.intent import IntentRouter
from app.services.customer_service.retrieval import HybridRetriever
from app.services.customer_service.rewrite import QueryRewriter
from app.services.customer_service.vector_store import get_vector_store
from app.services.llm import get_llm


@lru_cache
def get_audit_service() -> AuditService:
    return AuditService(get_llm())


@lru_cache
def get_cs_service() -> ChatService:
    llm = get_llm()
    return ChatService(
        llm,
        router=IntentRouter(llm),
        rewriter=QueryRewriter(llm) if settings.cs_enable_query_rewrite else None,
        retriever=HybridRetriever(vector_store=get_vector_store(), rrf_k=settings.cs_rrf_k),
        flywheel=QuestionPool(settings.cs_flywheel_path),
        business=BusinessDataService(),
    )

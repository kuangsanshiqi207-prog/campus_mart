"""RAG 检索与上下文组装。"""

from __future__ import annotations

from typing import Any

from app.prompts.customer_service import (
    CUSTOMER_SERVICE_SYSTEM_TEMPLATE,
    NO_CONTEXT,
)


def build_context(vector_store: Any | None, query: str, top_k: int) -> str:
    """从向量库检索 top_k 条相关内容，拼成上下文；无向量库或检索失败时优雅降级。"""
    if vector_store is None:
        return NO_CONTEXT
    try:
        docs = vector_store.similarity_search(query, k=top_k)
    except Exception:
        return NO_CONTEXT
    if not docs:
        return NO_CONTEXT
    parts = [f"【{i}】{d.page_content.strip()}" for i, d in enumerate(docs, 1)]
    return "以下是可参考的平台知识，回答时优先依据这些内容：\n" + "\n\n".join(parts)


def build_system_prompt(vector_store: Any | None, query: str, top_k: int) -> str:
    return CUSTOMER_SERVICE_SYSTEM_TEMPLATE.format(
        context=build_context(vector_store, query, top_k)
    )

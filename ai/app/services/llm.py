"""LLM 工厂：统一走 OpenAI 兼容接口，换模型只改 .env。"""

from __future__ import annotations

from functools import lru_cache

from langchain_openai import ChatOpenAI, OpenAIEmbeddings

from app.config import settings


@lru_cache
def get_llm() -> ChatOpenAI:
    """返回单例 ChatOpenAI。"""
    if not settings.llm_api_key:
        raise RuntimeError("未配置 LLM_API_KEY，请在 .env 中填写后重启服务")
    return ChatOpenAI(
        model=settings.llm_model,
        base_url=settings.llm_base_url,
        api_key=settings.llm_api_key,
        temperature=settings.llm_temperature,
        timeout=settings.llm_timeout,
    )


@lru_cache
def get_embeddings() -> OpenAIEmbeddings:
    """返回单例 OpenAIEmbeddings（base_url/api_key 留空时回退到 LLM 配置）。"""
    api_key = settings.embedding_api_key_resolved
    if not api_key:
        raise RuntimeError("未配置 Embedding API Key，请在 .env 中填写后重启服务")
    return OpenAIEmbeddings(
        model=settings.embedding_model,
        base_url=settings.embedding_base_url_resolved,
        api_key=api_key,
    )

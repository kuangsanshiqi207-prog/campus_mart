"""Chroma 向量库封装。

知识库索引由 scripts/ingest.py 构建；目录不存在或为空时返回 None，
客服服务会自动降级为纯对话。
"""

from __future__ import annotations

from langchain_chroma import Chroma

from app.config import settings
from app.services.llm import get_embeddings

_COLLECTION_NAME = "campus_mart_kb"


def get_vector_store() -> Chroma | None:
    persist_dir = settings.chroma_persist_path
    if not persist_dir.exists() or not any(persist_dir.iterdir()):
        return None
    try:
        return Chroma(
            persist_directory=str(persist_dir),
            embedding_function=get_embeddings(),
            collection_name=_COLLECTION_NAME,
        )
    except Exception:
        # 索引损坏或 Embedding 未配置时降级为纯对话
        return None

"""切分 data/knowledge/ 下的知识文件并写入 Chroma 向量库。

用法（在 ai 目录下）：
    python scripts/ingest.py
"""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from langchain_chroma import Chroma  # noqa: E402

from app.config import settings  # noqa: E402
from app.services.customer_service.knowledge import (  # noqa: E402
    COLLECTION_NAME,
    load_chunks,
)
from app.services.llm import get_embeddings  # noqa: E402


def main() -> None:
    chunks = load_chunks()
    if not chunks:
        print("未在 data/knowledge 找到任何 .md/.txt 文件，退出。")
        return

    persist_dir = settings.chroma_persist_path
    persist_dir.mkdir(parents=True, exist_ok=True)
    Chroma.from_documents(
        documents=chunks,
        embedding=get_embeddings(),
        persist_directory=str(persist_dir),
        collection_name=COLLECTION_NAME,
    )
    print(f"已写入 {len(chunks)} 个片段到向量库：{persist_dir}")


if __name__ == "__main__":
    main()

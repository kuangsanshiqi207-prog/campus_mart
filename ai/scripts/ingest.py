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
from langchain_core.documents import Document  # noqa: E402
from langchain_text_splitters import RecursiveCharacterTextSplitter  # noqa: E402

from app.config import settings  # noqa: E402
from app.services.llm import get_embeddings  # noqa: E402

KNOWLEDGE_DIR = ROOT / "data" / "knowledge"
SUPPORTED_SUFFIXES = (".md", ".txt")
COLLECTION_NAME = "campus_mart_kb"


def load_documents() -> list[Document]:
    docs: list[Document] = []
    for path in sorted(KNOWLEDGE_DIR.rglob("*")):
        if not path.is_file() or path.suffix.lower() not in SUPPORTED_SUFFIXES:
            continue
        text = path.read_text(encoding="utf-8")
        if not text.strip():
            continue
        docs.append(
            Document(
                page_content=text,
                metadata={"source": str(path.relative_to(KNOWLEDGE_DIR))},
            )
        )
    return docs


def main() -> None:
    docs = load_documents()
    if not docs:
        print(f"未在 {KNOWLEDGE_DIR} 找到任何 .md/.txt 文件，退出。")
        return

    splitter = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50)
    chunks = splitter.split_documents(docs)
    print(f"共 {len(docs)} 个文件，切分为 {len(chunks)} 个片段。")

    persist_dir = settings.chroma_persist_path
    persist_dir.mkdir(parents=True, exist_ok=True)
    Chroma.from_documents(
        documents=chunks,
        embedding=get_embeddings(),
        persist_directory=str(persist_dir),
        collection_name=COLLECTION_NAME,
    )
    print(f"已写入向量库：{persist_dir}")


if __name__ == "__main__":
    main()

"""知识库加载与切分：供 ingest 脚本与混合检索共用。"""
from __future__ import annotations

from pathlib import Path

from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

AI_ROOT = Path(__file__).resolve().parents[3]
KNOWLEDGE_DIR = AI_ROOT / "data" / "knowledge"
SUPPORTED_SUFFIXES = (".md", ".txt")
CHUNK_SIZE = 500
CHUNK_OVERLAP = 50
COLLECTION_NAME = "campus_mart_kb"


def load_documents() -> list[Document]:
    docs: list[Document] = []
    if not KNOWLEDGE_DIR.is_dir():
        return docs
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


def load_chunks() -> list[Document]:
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=CHUNK_SIZE, chunk_overlap=CHUNK_OVERLAP
    )
    return splitter.split_documents(load_documents())

"""混合检索：dense（Chroma）+ 词面 BM25，RRF 融合，输出置信度。

- 无任何检索能力（既无向量库也无知识库）时，置信度返回 None，表示不做门控；
- 有检索能力但召回为空时，返回空上下文 + 置信度 0.0，触发兜底。
"""
from __future__ import annotations

import math
import re
from collections import Counter

from langchain_core.documents import Document

from app.services.customer_service.knowledge import load_chunks

_CJK = re.compile(r"[一-鿿]")
_ASCII = re.compile(r"[a-zA-Z0-9]+")


def tokenize(text: str) -> list[str]:
    """中文按字 bigram、英文/数字按词切分，用于 BM25 词面检索。"""
    tokens: list[str] = []
    for seg in re.split(r"(\s+|[a-zA-Z0-9]+)", text):
        seg = seg.strip()
        if not seg:
            continue
        if _ASCII.fullmatch(seg):
            tokens.append(seg.lower())
        else:
            chars = _CJK.findall(seg)
            tokens.extend(a + b for a, b in zip(chars, chars[1:]))
            if len(chars) == 1:
                tokens.append(chars[0])
    return tokens


class BM25:
    """极简 Okapi BM25，纯 Python 实现，避免额外依赖。"""

    def __init__(self, corpus: list[str], k1: float = 1.5, b: float = 0.75):
        self._k1 = k1
        self._b = b
        self._corpus = corpus
        self._tokenized = [tokenize(d) for d in corpus]
        self._doc_len = [len(t) for t in self._tokenized]
        self._avgdl = sum(self._doc_len) / len(self._doc_len) if self._doc_len else 0.0
        self._n = len(corpus)
        self._df: dict[str, int] = {}
        for toks in self._tokenized:
            for t in set(toks):
                self._df[t] = self._df.get(t, 0) + 1
        self._idf = {
            t: math.log((self._n - df + 0.5) / (df + 0.5) + 1.0)
            for t, df in self._df.items()
        }

    def score(self, query: str, idx: int) -> float:
        dl = self._doc_len[idx]
        tf = Counter(self._tokenized[idx])
        total = 0.0
        for t in tokenize(query):
            idf = self._idf.get(t)
            if idf is None:
                continue
            f = tf.get(t, 0)
            denom = f + self._k1 * (1 - self._b + self._b * dl / self._avgdl) if self._avgdl else f + self._k1
            total += idf * (f * (self._k1 + 1)) / denom
        return total

    def search(self, query: str, k: int) -> list[tuple[int, float]]:
        scored = [(self.score(query, i), i) for i in range(self._n)]
        scored.sort(key=lambda x: x[0], reverse=True)
        return [(i, s) for s, i in scored if s > 0][:k]


def _rrf_fuse(
    ranked_lists: list[list[str]], k: int, rrf_k: int
) -> list[tuple[str, float]]:
    """Reciprocal Rank Fusion：按排名融合多路检索结果。"""
    scores: dict[str, float] = {}
    for ranked in ranked_lists:
        for rank, doc in enumerate(ranked, start=1):
            scores[doc] = scores.get(doc, 0.0) + 1.0 / (rrf_k + rank)
    return sorted(scores.items(), key=lambda x: x[1], reverse=True)[:k]


class HybridRetriever:
    """dense + BM25 混合检索，RRF 融合，返回 (上下文文本, 置信度)。"""

    def __init__(
        self,
        vector_store=None,
        chunks: list[Document] | None = None,
        rrf_k: int = 60,
    ):
        self._vector_store = vector_store
        self._chunks = list(chunks) if chunks is not None else load_chunks()
        self._bm25 = BM25([c.page_content for c in self._chunks]) if self._chunks else None
        self._rrf_k = rrf_k

    def _dense(self, query: str, k: int) -> tuple[list[tuple[str, float]], dict[str, float]]:
        if self._vector_store is None:
            return [], {}
        try:
            scored = self._vector_store.similarity_search_with_relevance_scores(query, k=k)
        except Exception:
            return [], {}
        sims: dict[str, float] = {}
        ranked: list[tuple[str, float]] = []
        for doc, score in scored:
            text = doc.page_content.strip()
            if not text:
                continue
            sims[text] = float(score)
            ranked.append((text, float(score)))
        ranked.sort(key=lambda x: x[1], reverse=True)
        return ranked, sims

    def _lexical(self, query: str, k: int) -> list[tuple[str, float]]:
        if self._bm25 is None:
            return []
        return [
            (self._chunks[i].page_content.strip(), s)
            for i, s in self._bm25.search(query, k)
        ]

    def retrieve(self, query: str, k: int) -> tuple[str, float | None]:
        dense_active = self._vector_store is not None
        lexical_active = self._bm25 is not None
        if not dense_active and not lexical_active:
            return "", None

        dense_ranked, dense_sims = self._dense(query, k)
        lexical_ranked = self._lexical(query, k)
        fused = _rrf_fuse(
            [[t for t, _ in dense_ranked], [t for t, _ in lexical_ranked]],
            k,
            self._rrf_k,
        )
        if not fused:
            return "", 0.0

        if dense_sims:
            confidence = max(dense_sims.get(t, 0.0) for t, _ in fused)
        else:
            confidence = 1.0

        context = "\n\n".join(f"【{i}】{t}" for i, (t, _) in enumerate(fused, 1))
        return context, min(max(confidence, 0.0), 1.0)

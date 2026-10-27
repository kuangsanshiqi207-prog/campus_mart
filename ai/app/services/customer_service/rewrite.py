"""Query 改写：结合历史做指代消解与扩写，提升检索召回。"""
from __future__ import annotations

from typing import Any

from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import BaseMessage

from app.prompts.customer_service import QUERY_REWRITE_TEMPLATE


def _as_text(content: Any) -> str:
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return "".join(
            p.get("text", "") if isinstance(p, dict) else str(p) for p in content
        )
    return str(content)


class QueryRewriter:
    def __init__(self, llm: BaseChatModel | None = None):
        self._llm = llm

    def rewrite(self, message: str, history: list[BaseMessage]) -> str:
        if self._llm is None or not history:
            return message
        lines: list[str] = []
        for m in history[-4:]:
            role = "用户" if m.type == "human" else "助手" if m.type == "ai" else "系统"
            lines.append(f"{role}：{_as_text(m.content)}")
        try:
            reply = self._llm.invoke(
                QUERY_REWRITE_TEMPLATE.format(history="\n".join(lines), message=message)
            )
            out = _as_text(reply.content).strip()
            return out if out else message
        except Exception:
            return message

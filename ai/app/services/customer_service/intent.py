"""意图分流：把用户消息分为 知识类 / 业务数据类 / 无关话题。"""
from __future__ import annotations

from enum import Enum
from typing import Any

from langchain_core.language_models.chat_models import BaseChatModel

from app.prompts.customer_service import INTENT_CLASSIFY_TEMPLATE


class Intent(str, Enum):
    KNOWLEDGE = "knowledge"  # 知识类：规则、流程、FAQ
    BUSINESS = "business"    # 业务数据类：订单/账号/举报等个人实时数据
    OFF_TOPIC = "off_topic"  # 无关话题


# LLM 不可用时的兜底关键词启发式
_BUSINESS_KEYWORDS = (
    "订单", "物流", "快递", "发货", "收货", "退款", "退货", "售后",
    "余额", "钱包", "提现", "收款", "付款", "支付",
    "实名认证", "学生认证", "认证进度", "账号", "密码", "注销", "封禁",
    "举报进度", "申诉", "处罚", "信用分",
)


def _as_text(content: Any) -> str:
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return "".join(
            p.get("text", "") if isinstance(p, dict) else str(p) for p in content
        )
    return str(content)


class IntentRouter:
    def __init__(self, llm: BaseChatModel | None = None):
        self._llm = llm

    def classify(self, message: str, history: list[Any] | None = None) -> Intent:
        if self._llm is not None:
            try:
                reply = self._llm.invoke(INTENT_CLASSIFY_TEMPLATE.format(message=message))
                text = _as_text(reply.content).strip().lower()
                for intent in Intent:
                    if intent.value in text:
                        return intent
            except Exception:
                pass
        return self._heuristic(message)

    @staticmethod
    def _heuristic(message: str) -> Intent:
        if any(k in message for k in _BUSINESS_KEYWORDS):
            return Intent.BUSINESS
        return Intent.KNOWLEDGE

"""AI 客服模块单元测试。"""

from __future__ import annotations

from langchain_core.language_models.fake_chat_models import FakeListChatModel

from app.prompts.customer_service import NO_CONTEXT
from app.schemas.customer_service import ChatMessage
from app.services.customer_service.chat import ChatService
from app.services.customer_service.business import classify_business_domain
from app.services.customer_service.intent import Intent, IntentRouter
from app.services.customer_service.rag import build_context, build_system_prompt
from app.services.customer_service.retrieval import HybridRetriever, tokenize


def test_build_context_without_store() -> None:
    assert build_context(None, "query", 4) == NO_CONTEXT


def test_build_system_prompt_without_store() -> None:
    prompt = build_system_prompt(None, "query", 4)
    assert "校集" in prompt
    assert NO_CONTEXT in prompt


def test_chat_returns_reply_and_memory(cs_service: ChatService) -> None:
    reply = cs_service.chat("s1", "你好")
    assert reply == "这是测试回复。"
    assert len(cs_service._histories["s1"]) == 2  # 用户消息 + 助手回复


def test_chat_seeds_history_on_first_call() -> None:
    llm = FakeListChatModel(responses=["ok"])
    service = ChatService(llm, vector_store=None, top_k=4)
    seed = [ChatMessage(role="user", content="之前的问题")]
    service.chat("s2", "新问题", history=seed)
    # 历史 = seed(1 条) + 当前用户消息 + 助手回复
    assert len(service._histories["s2"]) == 3


# ==================== 工作流各组件 ====================


def test_intent_router_heuristic_business() -> None:
    router = IntentRouter()  # 无 LLM，走关键词兜底
    assert router.classify("我的订单到哪了") == Intent.BUSINESS
    assert router.classify("怎么发布闲置") == Intent.KNOWLEDGE


def test_tokenize_cjk_bigrams() -> None:
    toks = tokenize("发布商品")
    assert "发布" in toks
    assert "商品" in toks


def test_hybrid_retriever_no_capability_returns_none() -> None:
    retriever = HybridRetriever(vector_store=None, chunks=[])
    context, confidence = retriever.retrieve("怎么发布商品", 4)
    assert context == ""
    assert confidence is None


class _StubRouter:
    def classify(self, message, history=None):
        return Intent.BUSINESS


def test_chat_business_intent_uses_guidance(fake_llm: FakeListChatModel) -> None:
    service = ChatService(fake_llm, router=_StubRouter())
    reply = service.chat("b1", "我的订单到哪了")
    assert reply == "这是测试回复。"
    assert len(service._histories["b1"]) == 2


# ==================== 业务数据工具调用 ====================


def test_classify_business_domain() -> None:
    assert classify_business_domain("我的订单到哪了") == "orders"
    assert classify_business_domain("我的举报进度") == "reports"
    assert classify_business_domain("实名认证通过了吗") == "certification"
    assert classify_business_domain("怎么发布商品") is None


class _StubBusiness:
    async def query(self, domain, token):
        return "订单号 X2026：iPhone｜状态 pending"


async def test_chat_business_fetches_data(fake_llm: FakeListChatModel) -> None:
    service = ChatService(fake_llm, router=_StubRouter(), business=_StubBusiness())
    reply = await service.achat("b2", "我的订单到哪了", token="tok")
    assert reply == "这是测试回复。"
    assert len(service._histories["b2"]) == 2

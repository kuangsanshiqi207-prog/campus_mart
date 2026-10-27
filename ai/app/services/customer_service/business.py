"""业务数据查询：按子域路由，通过网关转发用户 token 调用 Java 后端。

网关会把用户 JWT 解析为 X-User-Id 并转发；下游服务的 JWT 拦截器据此鉴权。
"""
from __future__ import annotations

import httpx
from typing import Any

from app.config import settings

# 子域关键词（按顺序匹配，命中即返回；仅在意图已判定为 business 后使用）
_DOMAINS: list[tuple[str, tuple[str, ...]]] = [
    ("orders", ("订单", "物流", "快递", "发货", "收货", "退款", "退货", "售后")),
    ("reports", ("举报", "申诉", "处罚", "封禁")),
    ("certification", ("实名", "认证", "学生证", "学号")),
]


def classify_business_domain(message: str) -> str | None:
    for domain, keywords in _DOMAINS:
        if any(k in message for k in keywords):
            return domain
    return None


class BusinessDataService:
    def __init__(self, base_url: str | None = None, timeout: float = 10.0):
        self._base_url = (base_url or settings.gateway_base_url).rstrip("/")
        self._timeout = timeout

    async def _get(self, path: str, token: str) -> dict[str, Any]:
        headers = {"Authorization": token}
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            resp = await client.get(f"{self._base_url}{path}", headers=headers)
            resp.raise_for_status()
            return resp.json()

    async def query(self, domain: str, token: str) -> str:
        """返回可读的数据摘要；失败或未命中时返回空字符串（由调用方降级为引导话术）。"""
        try:
            if domain == "orders":
                return await self._list_orders(token)
            if domain == "reports":
                return await self._list_reports(token)
            if domain == "certification":
                return await self._get_certification(token)
        except Exception:
            pass
        return ""

    async def _list_orders(self, token: str) -> str:
        data = await self._get("/user/orders?pageNum=1&pageSize=10", token)
        records = ((data.get("data") or {}).get("records")) or []
        if not records:
            return "你目前没有订单。"
        lines = [
            f"订单号 {r.get('orderNo')}｜商品 {r.get('productTitle')}｜状态 {r.get('status')}"
            f"｜金额 {r.get('amount')} 元｜交易方式 {r.get('tradeType') or '未填'}"
            for r in records[:10]
        ]
        return "以下是你的订单（最多 10 条）：\n" + "\n".join(lines)

    async def _list_reports(self, token: str) -> str:
        data = await self._get("/user/reports?pageNum=1&pageSize=10", token)
        records = ((data.get("data") or {}).get("records")) or []
        if not records:
            return "你目前没有提交过举报。"
        lines = [
            f"举报 #{r.get('id')}｜原因 {r.get('reason')}｜状态 {r.get('status')}"
            f"｜处理说明 {r.get('handleReason') or '暂无'}"
            for r in records[:10]
        ]
        return "以下是你的举报记录（最多 10 条）：\n" + "\n".join(lines)

    async def _get_certification(self, token: str) -> str:
        data = await self._get("/user/certification", token)
        cert = data.get("data")
        if not cert:
            return "你还没有提交校园/学生认证。"
        return (
            f"认证状态 {cert.get('status')}｜学校 {cert.get('school') or '未填'}｜"
            f"学号 {cert.get('studentNo') or '未填'}｜拒绝原因 {cert.get('reason') or '无'}"
        )

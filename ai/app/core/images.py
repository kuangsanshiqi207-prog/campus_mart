"""把图片输入归一化为 LangChain 多模态消息片段。

仅支持两种形式：
- http(s) 外部 URL，直接透传给模型拉取；
- data:image/...;base64,... 数据 URL。
"""

from __future__ import annotations

from typing import Any


def to_image_content(url: str) -> dict[str, Any]:
    """把一张图片的地址转成 LangChain 消息的 image_url 内容片段。"""
    url = (url or "").strip()
    if not url:
        raise ValueError("图片地址不能为空")
    if not url.startswith(("http://", "https://", "data:")):
        raise ValueError(f"不支持的图片地址：{url!r}（仅支持 http(s) 或 data: URL）")
    return {"type": "image_url", "image_url": {"url": url}}

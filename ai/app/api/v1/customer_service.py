"""AI 客服接口：同步 + SSE 流式。"""

from __future__ import annotations

import logging

from fastapi import APIRouter, Depends
from sse_starlette.sse import EventSourceResponse

from app.api.deps import get_cs_service
from app.schemas.common import ApiResponse
from app.schemas.customer_service import ChatReply, ChatRequest
from app.services.customer_service.chat import ChatService

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/customer-service", tags=["AI客服"])


@router.post("/chat", response_model=ApiResponse[ChatReply], summary="客服对话（同步）")
async def chat(
    request: ChatRequest,
    service: ChatService = Depends(get_cs_service),
) -> ApiResponse[ChatReply]:
    content = await service.achat(request.session_id, request.message, request.history)
    return ApiResponse.success(ChatReply(session_id=request.session_id, content=content))


@router.post("/chat/stream", summary="客服对话（SSE 流式）")
async def chat_stream(
    request: ChatRequest,
    service: ChatService = Depends(get_cs_service),
) -> EventSourceResponse:
    async def event_gen():
        try:
            async for text in service.astream(
                request.session_id, request.message, request.history
            ):
                yield {"event": "message", "data": text}
            yield {"event": "done", "data": "[DONE]"}
        except Exception as exc:  # noqa: BLE001
            logger.exception("客服流式输出失败")
            yield {"event": "error", "data": str(exc)}

    return EventSourceResponse(event_gen())

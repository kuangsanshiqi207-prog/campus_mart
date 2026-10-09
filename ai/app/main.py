"""FastAPI 入口。"""

from __future__ import annotations

import logging

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.api.v1.router import api_router
from app.core.exceptions import AppError
from app.core.logging import setup_logging
from app.schemas.common import ApiResponse

setup_logging()
logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
    app = FastAPI(
        title="校集 AI 服务",
        description="AI 审核 + AI 客服（FastAPI + LangChain）",
        version="0.1.0",
    )

    app.include_router(api_router)

    @app.exception_handler(AppError)
    async def app_error_handler(request: Request, exc: AppError) -> JSONResponse:
        # 业务异常：HTTP 200 + code=0，对齐 Java Result，方便 Feign 直接解析
        return JSONResponse(
            status_code=200,
            content=ApiResponse.error(exc.msg, exc.code).model_dump(),
        )

    @app.exception_handler(RequestValidationError)
    async def validation_error_handler(
        request: Request, exc: RequestValidationError
    ) -> JSONResponse:
        return JSONResponse(
            status_code=200,
            content=ApiResponse.error(f"请求参数错误：{exc.errors()}").model_dump(),
        )

    @app.exception_handler(Exception)
    async def unhandled_error_handler(request: Request, exc: Exception) -> JSONResponse:
        logger.exception("未处理异常")
        return JSONResponse(
            status_code=500,
            content=ApiResponse.error("服务器内部错误").model_dump(),
        )

    @app.get("/health", tags=["健康检查"])
    async def health() -> ApiResponse:
        return ApiResponse.success({"status": "ok"})

    return app


app = create_app()

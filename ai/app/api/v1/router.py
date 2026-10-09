"""聚合 /api/v1 下的所有子路由。"""

from __future__ import annotations

from fastapi import APIRouter

from app.api.v1 import audit, customer_service

api_router = APIRouter(prefix="/api/v1")
api_router.include_router(audit.router)
api_router.include_router(customer_service.router)

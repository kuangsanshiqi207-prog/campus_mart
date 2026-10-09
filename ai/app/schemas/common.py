"""统一返回结构，对齐 Java 端 Result（code=1 成功，code=0 失败）。"""

from __future__ import annotations

from typing import Any, Generic, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    code: int = 1
    msg: str = "success"
    data: T | None = None

    @classmethod
    def success(cls, data: Any = None, msg: str = "success") -> "ApiResponse":
        return cls(code=1, msg=msg, data=data)

    @classmethod
    def error(cls, msg: str, code: int = 0) -> "ApiResponse":
        return cls(code=code, msg=msg, data=None)

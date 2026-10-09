"""业务异常定义，配合 main.py 中的全局异常处理器统一返回。"""

from __future__ import annotations

from typing import Any


class AppError(Exception):
    """业务异常基类：携带可对外返回的 msg / code / data。"""

    def __init__(self, msg: str, code: int = 0, data: Any = None):
        self.msg = msg
        self.code = code
        self.data = data
        super().__init__(msg)


class ConfigError(AppError):
    """配置缺失或错误。"""


class AuditTypeNotFound(AppError):
    """不支持的审核类型。"""

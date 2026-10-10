"""Nacos 服务注册（基于 Nacos OpenAPI，默认地址 127.0.0.1:8848）。

- 启动时注册实例；ephemeral=true 时后台线程定时发送心跳。
- 优雅关闭时注销实例。
- 注册/心跳失败仅记录日志，不阻断服务启动，方便本地无 Nacos 时也能跑。
"""

from __future__ import annotations

import json
import logging
import socket
import threading

import httpx

from app.config import settings

logger = logging.getLogger(__name__)

_CLUSTER = "DEFAULT"


def _resolve_ip() -> str:
    """返回本机 IP：优先用 service_ip 配置，否则探测出口网卡，兜底 127.0.0.1。"""
    if settings.service_ip:
        return settings.service_ip
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))  # 不真正发包，仅用于选定出口网卡
        ip = s.getsockname()[0]
        s.close()
        return ip
    except OSError:
        return "127.0.0.1"


class NacosRegistry:
    def __init__(self) -> None:
        # trust_env=False：Nacos 走本机直连，避免被系统/环境代理劫持到外部
        self._client = httpx.Client(timeout=settings.nacos_timeout, trust_env=False)
        self._ip = _resolve_ip()
        self._port = settings.app_port
        self._stop = threading.Event()
        self._thread: threading.Thread | None = None

    @staticmethod
    def _ephemeral_str() -> str:
        return "true" if settings.nacos_ephemeral else "false"

    @property
    def _base_params(self) -> dict:
        return {
            "serviceName": settings.service_name,
            "ip": self._ip,
            "port": self._port,
            "namespaceId": settings.nacos_namespace,
            "groupName": settings.nacos_group,
            "clusterName": _CLUSTER,
        }

    def _url(self, path: str) -> str:
        return f"http://{settings.nacos_server_addr}{path}"

    def register(self) -> bool:
        params = {
            **self._base_params,
            "weight": 1.0,
            "enabled": True,
            "healthy": True,
            "ephemeral": self._ephemeral_str(),
            "metadata": "{}",
        }
        try:
            resp = self._client.post(self._url("/nacos/v1/ns/instance"), params=params)
            ok = resp.status_code == 200 and "ok" in resp.text.lower()
            if ok:
                logger.info(
                    "已注册到 Nacos：%s @ %s:%s (ephemeral=%s)",
                    settings.service_name,
                    self._ip,
                    self._port,
                    settings.nacos_ephemeral,
                )
            else:
                logger.warning("Nacos 注册失败：HTTP %s %s", resp.status_code, resp.text)
            return ok
        except Exception as exc:  # noqa: BLE001
            logger.warning("Nacos 注册异常：%s", exc)
            return False

    def _beat(self) -> bool:
        beat = {
            "serviceName": settings.service_name,
            "ip": self._ip,
            "port": self._port,
            "cluster": _CLUSTER,
            "weight": 1,
            "metadata": {},
            "scheduled": False,
            "period": settings.nacos_heartbeat_interval * 1000,
            "stopped": False,
        }
        params = {**self._base_params, "ephemeral": "true", "beat": json.dumps(beat)}
        try:
            resp = self._client.put(
                self._url("/nacos/v1/ns/instance/beat"), params=params
            )
            return resp.status_code == 200
        except Exception:  # noqa: BLE001
            return False

    def _heartbeat_loop(self) -> None:
        while not self._stop.wait(settings.nacos_heartbeat_interval):
            if not self._beat():
                logger.warning("Nacos 心跳失败，尝试重新注册")
                self.register()

    def deregister(self) -> None:
        params = {**self._base_params, "ephemeral": self._ephemeral_str()}
        try:
            resp = self._client.delete(
                self._url("/nacos/v1/ns/instance"), params=params
            )
            if resp.status_code == 200:
                logger.info(
                    "已从 Nacos 注销：%s @ %s:%s",
                    settings.service_name,
                    self._ip,
                    self._port,
                )
            else:
                logger.warning("Nacos 注销失败：HTTP %s", resp.status_code)
        except Exception as exc:  # noqa: BLE001
            logger.warning("Nacos 注销异常：%s", exc)

    def start(self) -> None:
        if not settings.nacos_enabled:
            logger.info("Nacos 注册已关闭（NACOS_ENABLED=false）")
            return
        if not self.register():
            return
        if settings.nacos_ephemeral:
            self._thread = threading.Thread(
                target=self._heartbeat_loop, daemon=True, name="nacos-heartbeat"
            )
            self._thread.start()

    def stop(self) -> None:
        self._stop.set()
        if settings.nacos_enabled:
            self.deregister()
        self._client.close()

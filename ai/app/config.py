"""应用配置：通过 pydantic-settings 读取 .env / 环境变量。"""

from __future__ import annotations

from functools import lru_cache
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    # LLM（OpenAI 兼容接口，换模型只改这里）
    llm_base_url: str = "https://api.deepseek.com/v1"
    llm_api_key: str = ""
    llm_model: str = "deepseek-chat"
    llm_temperature: float = 0.1
    llm_timeout: int = 60

    # Embedding（留空时回退到 LLM 的 base_url / api_key）
    embedding_base_url: str = ""
    embedding_api_key: str = ""
    embedding_model: str = "text-embedding-3-small"

    # 向量库
    chroma_persist_dir: str = "./data/chroma"
    rag_top_k: int = 4

    # 服务
    app_port: int = 8000
    log_level: str = "INFO"

    # Nacos 注册中心（默认地址 127.0.0.1:8848）
    nacos_enabled: bool = True
    nacos_server_addr: str = "127.0.0.1:8848"
    nacos_namespace: str = ""
    nacos_group: str = "DEFAULT_GROUP"
    nacos_ephemeral: bool = True
    nacos_heartbeat_interval: int = 5
    nacos_timeout: int = 5
    service_name: str = "ai-service"
    service_ip: str = ""

    @property
    def embedding_base_url_resolved(self) -> str:
        return self.embedding_base_url or self.llm_base_url

    @property
    def embedding_api_key_resolved(self) -> str:
        return self.embedding_api_key or self.llm_api_key

    @property
    def chroma_persist_path(self) -> Path:
        return Path(self.chroma_persist_dir)


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()

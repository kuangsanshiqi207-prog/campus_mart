# 校集 AI 服务

基于 **FastAPI + LangChain** 的 AI 服务，为校集平台提供两类能力：

- **AI 审核**：对商品、评论、举报、实名/学生认证做智能审核，辅助管理端人工审核。
- **AI 客服**：面向用户的智能客服对话（支持 RAG 知识库 + 流式输出）。

本模块是独立的分布式服务，后续由 Java 后端（`backend/`）通过 HTTP 调用。接口返回结构已对齐 Java 端 `Result`（`{code, msg, data}`，成功 `code=1`、失败 `code=0`），方便以后用 OpenFeign 直接映射。

## 目录结构

```
ai/
├── app/
│   ├── main.py                 # FastAPI 入口（路由 + 全局异常处理）
│   ├── config.py               # 配置（pydantic-settings 读 .env）
│   ├── api/                    # 传输层：deps（依赖注入）+ v1 路由
│   ├── schemas/                # 请求/响应模型（对齐 Java Result / 实体字段）
│   ├── services/               # 业务层：llm 工厂 + audit + customer_service
│   ├── prompts/                # 提示词（审核规则 / 客服人设）
│   └── core/                   # 异常、日志、图片处理
├── data/knowledge/             # 客服知识库（.md/.txt）
├── scripts/ingest.py           # 切分知识库并写入向量库
└── tests/                      # 单元测试（不依赖真实 LLM key）
```

分层说明：`api`（HTTP 传输）→ `services`（LangChain 链 / 业务）→ `prompts` + `schemas` + `core`（横切）。

## 快速开始

```bash
cd ai
python -m venv .venv
# Windows
.venv\Scripts\activate
# macOS / Linux
source .venv/bin/activate

pip install -e ".[dev]"
cp .env.example .env     # 填入真实的 LLM_API_KEY
uvicorn app.main:app --reload
```

打开 http://localhost:8000/docs 查看自动生成的接口文档，http://localhost:8000/health 检查健康。

### 构建客服知识库（可选）

把平台帮助文档放到 `data/knowledge/`（`.md` / `.txt`），然后：

```bash
python scripts/ingest.py
```

没有知识库索引时，客服会自动降级为纯对话。

## 接口

### AI 审核

`POST /api/v1/audit`

请求体：

```json
{
  "audit_type": "product",
  "text_fields": { "title": "九成新 iPhone 13", "description": "自用闲置，功能正常" },
  "image_urls": ["https://example.com/photo.jpg"]
}
```

`audit_type` 取值：`product`（商品）、`review`（评论）、`report`（举报）、`certification`（实名认证）。`text_fields` 键名对齐 Java 实体字段名。

响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "passed": true,
    "reason": "商品信息正常",
    "risk_category": "正常",
    "risk_level": "low",
    "suggested_action": "通过"
  }
}
```

### AI 客服

- `POST /api/v1/customer-service/chat` —— 同步返回完整回答。
- `POST /api/v1/customer-service/chat/stream` —— SSE 流式输出（逐 token，事件 `message` 为内容、`done` 为结束）。

请求体：

```json
{
  "session_id": "user-1",
  "message": "怎么发布商品？",
  "history": []
}
```

## 配置（.env）

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `LLM_BASE_URL` | OpenAI 兼容接口地址 | `https://api.deepseek.com/v1` |
| `LLM_API_KEY` | API Key（必填） | 空 |
| `LLM_MODEL` | 模型名 | `deepseek-chat` |
| `LLM_TEMPERATURE` | 采样温度 | `0.1` |
| `LLM_TIMEOUT` | 请求超时（秒） | `60` |
| `EMBEDDING_BASE_URL` / `EMBEDDING_API_KEY` | 向量模型配置，留空回退到 LLM 配置 | 空 |
| `EMBEDDING_MODEL` | 向量模型名 | `text-embedding-3-small` |
| `CHROMA_PERSIST_DIR` | 向量库目录 | `./data/chroma` |
| `RAG_TOP_K` | 检索条数 | `4` |
| `NACOS_ENABLED` | 是否注册到 Nacos | `true` |
| `NACOS_SERVER_ADDR` | Nacos 地址 | `127.0.0.1:8848` |
| `NACOS_NAMESPACE` | 命名空间（空=public） | 空 |
| `NACOS_GROUP` | 分组 | `DEFAULT_GROUP` |
| `NACOS_EPHEMERAL` | 临时实例（true 需心跳） | `true` |
| `SERVICE_NAME` | 服务名（Java 侧按此发现） | `ai-service` |
| `SERVICE_IP` | 注册 IP（空=自动探测） | 空 |

> 换模型只改 `.env`：`LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL`。支持 DeepSeek、通义千问、Moonshot 等所有 OpenAI 兼容服务。AI 审核使用 `function_calling` 方式做结构化输出，请确保所选模型支持工具调用（DeepSeek、Qwen 等均支持）。

## 注册到 Nacos

服务启动时会自动向 Nacos（默认 `127.0.0.1:8848`）注册实例，服务名为 `ai-service`，Java 侧可通过服务名发现并调用。启动日志出现 `已注册到 Nacos：ai-service @ <ip>:8000` 即成功。

- 默认以**临时实例**（`ephemeral=true`）注册，后台每 5 秒发送心跳，关闭时自动注销。
- 本地暂时没起 Nacos 也不影响启动：注册失败只打日志、不阻断服务。
- 想临时关闭注册：`.env` 里设 `NACOS_ENABLED=false`。

## 测试

```bash
pytest
```

单测使用 LangChain 的 `FakeListChatModel` 与 mock，不依赖真实 LLM key。

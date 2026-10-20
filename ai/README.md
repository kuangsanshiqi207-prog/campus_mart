# 校集 AI 服务（开发文档）

为校集平台提供两类 AI 能力的独立 Python 服务：

- **AI 审核**：对商品、评论、举报、实名/学生认证做智能审核，辅助管理端人工审核。
- **AI 客服**：面向用户的智能客服对话（支持 RAG 知识库 + SSE 流式输出）。

基于 **FastAPI + LangChain**，统一走 OpenAI 兼容接口调用大模型（DeepSeek / 通义千问 / Moonshot 等），启动时注册到 **Nacos**，供 Java 后端（`backend/`，Spring Cloud + OpenFeign）以服务名 `ai-service` 发现并调用。

> 接口返回结构对齐 Java 端 `Result`（`{code, msg, data}`，成功 `code=1`、失败 `code=0`），方便 OpenFeign 直接映射，无需二次解析。

---

## 目录

1. [技术栈](#技术栈)
2. [目录结构](#目录结构)
3. [快速开始](#快速开始)
4. [架构设计](#架构设计)
5. [核心流程](#核心流程)
6. [接口契约](#接口契约)
7. [配置说明](#配置说明)
8. [与 Java 后端集成](#与-java-后端集成)
9. [如何扩展](#如何扩展)
10. [错误处理与降级](#错误处理与降级)
11. [测试](#测试)
12. [注意事项与已知限制](#注意事项与已知限制)

---

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 | Python 3.11+ |
| Web 框架 | FastAPI + uvicorn |
| 校验/配置 | pydantic v2 + pydantic-settings（读 `.env`） |
| LLM 编排 | `langchain-core`、`langchain-openai`（`ChatOpenAI` / `OpenAIEmbeddings`） |
| 结构化输出 | `llm.with_structured_output(AuditDecision)`（function calling） |
| 向量库 | Chroma（`langchain-chroma` + `chromadb`），切分用 `langchain-text-splitters` |
| 流式输出 | `sse-starlette` |
| 服务发现 | 自研 `core/nacos.py`（调 Nacos OpenAPI，`httpx` 直连） |
| 测试 | pytest + pytest-asyncio + LangChain `FakeListChatModel` |

## 目录结构

```
ai/
├── .env.example                # 配置样例
├── pyproject.toml              # 依赖与打包配置
├── app/
│   ├── main.py                 # FastAPI 入口：lifespan + 路由 + 全局异常处理
│   ├── config.py               # Settings（pydantic-settings 读 .env）
│   ├── api/
│   │   ├── deps.py             # 依赖注入：get_audit_service / get_cs_service
│   │   └── v1/
│   │       ├── router.py       # 聚合 /api/v1 下所有子路由
│   │       ├── audit.py        # AI 审核接口
│   │       └── customer_service.py  # AI 客服接口（同步 + SSE）
│   ├── schemas/                # 请求/响应模型（对齐 Java Result / 实体字段）
│   │   ├── common.py           # ApiResponse {code, msg, data}
│   │   ├── audit.py            # AuditRequest / AuditDecision / AuditType
│   │   └── customer_service.py # ChatRequest / ChatReply / ChatMessage
│   ├── services/
│   │   ├── llm.py              # get_llm() / get_embeddings() 单例工厂
│   │   ├── audit/              # 审核业务层
│   │   │   ├── base.py         # BaseAuditor 基类（结构化输出）
│   │   │   ├── product.py      # 商品审核
│   │   │   ├── review.py       # 评论审核
│   │   │   ├── report.py       # 举报审核
│   │   │   ├── certification.py# 实名/学生认证审核
│   │   │   └── registry.py     # audit_type → 审核器注册表 + 分发
│   │   └── customer_service/   # 客服业务层
│   │       ├── chat.py         # ChatService（会话历史 + 同步/异步/流式）
│   │       ├── rag.py          # RAG 检索与上下文组装
│   │       └── vector_store.py # Chroma 封装（可降级）
│   ├── prompts/                # 提示词
│   │   ├── audit.py            # 四类审核规则
│   │   └── customer_service.py # 客服人设模板
│   └── core/                   # 横切：异常 / 日志 / 图片 / Nacos
│       ├── exceptions.py       # AppError / ConfigError / AuditTypeNotFound
│       ├── images.py           # 图片地址归一化为多模态消息片段
│       ├── logging.py          # 日志配置
│       └── nacos.py            # Nacos 注册/心跳/注销
├── data/knowledge/             # 客服知识库（.md/.txt，样例 sample-faq.md）
├── scripts/ingest.py           # 切分知识库并写入 Chroma 索引
└── tests/                      # 单测（不依赖真实 LLM key）
```

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

- 接口文档：http://localhost:8000/docs
- 健康检查：http://localhost:8000/health

### 构建客服知识库（可选）

把平台帮助文档放到 `data/knowledge/`（`.md` / `.txt`），然后：

```bash
python scripts/ingest.py
```

没有知识库索引时，客服会自动降级为纯对话。

### 跑测试

```bash
pytest
```

---

## 架构设计

### 分层

```
HTTP 请求
   │
   ▼
api/v1/*           传输层：参数校验、路由、异常→ApiResponse 转换
   │  Depends 注入
   ▼
services/          业务层：LangChain 链、审核分发、RAG、会话管理
   │
   ├─ prompts/      提示词（审核规则 / 客服人设）
   ├─ schemas/      请求响应模型
   └─ core/         横切能力（图片归一化、Nacos、异常、日志）
```

- **`api` 层不写业务**，只做参数接收与结果封装，具体逻辑全部下沉到 `services`。
- **`services.llm` 是唯一的 LLM 入口**，业务层不直接 `new ChatOpenAI`，换模型只改 `.env`。
- **`schemas.audit.text_fields` 键名对齐 Java 实体字段名**（`title` / `description` / `tradeType` / `studentNo`…），Java 侧组装请求体时无需二次映射。

### 依赖注入与单例

`api/deps.py` 用 `@lru_cache` 保证全局只有一个 LLM 客户端 / 一个审核服务 / 一个客服服务实例：

```python
@lru_cache
def get_audit_service() -> AuditService:
    return AuditService(get_llm())

@lru_cache
def get_cs_service() -> ChatService:
    return ChatService(get_llm(), get_vector_store())
```

> 注意：`get_llm()` 是**惰性**初始化的——`LLM_API_KEY` 缺失时不会在启动时立刻报错，而是在**首个请求**触发依赖注入时才抛 `RuntimeError`。排查「启动成功但接口 500」时先看 `.env`。

---

## 核心流程

### 1. AI 审核

```
AuditRequest(audit_type, text_fields, image_urls)
   │
   ▼
AuditService.aaudit()          # 按 audit_type 分发到对应审核器
   │
   ▼
BaseAuditor._build_messages()  # 组装 system=rules + human=[文本 + 图片片段]
   │
   ▼
llm.with_structured_output(AuditDecision).ainvoke()
   │
   ▼
AuditDecision(passed, reason, risk_category, risk_level, suggested_action)
```

- **文本**由各审核器的 `render_text()` 渲染成「请审核以下…」的列表。
- **图片**经 `core/images.py` 归一化为 `{"type":"image_url","image_url":{"url":...}}` 多模态片段，与文本一起作为 `HumanMessage` 的 `content` 列表传入。
- **结构化输出**用 `with_structured_output(AuditDecision)`，靠模型的 function calling 能力把结论约束成固定结构（因此所选模型需支持工具调用）。

### 2. AI 客服（RAG + 会话历史 + SSE）

```
ChatRequest(session_id, message, history)
   │
   ▼
ChatService._build_messages()
   │   ① build_system_prompt()：检索向量库 top_k 拼进系统提示词
   │   ② 追加该 session 的历史消息
   │   ③ 追加本次用户消息
   ▼
同步 /achat 或流式 /astream
   │
   ▼
_append(session_id, user, assistant)  # 回写会话历史
```

- **会话历史**按 `session_id` 隔离，存在**进程内存** `self._histories`（有 `threading.Lock` 保护）。分布式部署时需替换为 Redis 等共享存储（代码里已注释说明）。
- **RAG**：有向量库时 `similarity_search(query, k=top_k)` 检索并把结果拼进提示词；无索引 / 检索失败时优雅降级为 `NO_CONTEXT`。
- **SSE**：`astream()` 逐 chunk `yield`，`api/v1/customer_service.py` 用 `EventSourceResponse` 包装，事件名 `message` 为内容、`done` 为结束、`error` 为异常。

### 3. Nacos 注册

`main.py` 的 `lifespan` 在服务启动时 `NacosRegistry().start()`、关闭时 `stop()`：

- 通过 Nacos OpenAPI `POST /nacos/v1/ns/instance` 注册实例，`ephemeral=true` 时后台线程按 `NACOS_HEARTBEAT_INTERVAL`（默认 5s）发心跳。
- 心跳失败会自动尝试重新注册；注册/心跳/注销失败都**只打日志、不阻断服务**，本地没起 Nacos 也能跑。
- `httpx` 客户端设了 `trust_env=False`，避免被系统/环境代理劫持到外部。

---

## 接口契约

所有接口统一返回 `ApiResponse`（`{code, msg, data}`）。业务异常（`AppError`）返回 **HTTP 200 + `code=0`**，这样 OpenFeign 不会因非 2xx 抛异常，可直接解析 `code` 判断成败。

### AI 审核 `POST /api/v1/audit`

请求体：

```json
{
  "audit_type": "product",
  "text_fields": { "title": "九成新 iPhone 13", "description": "自用闲置，功能正常" },
  "image_urls": ["https://example.com/photo.jpg"],
  "extra": {}
}
```

| 字段 | 说明 |
| --- | --- |
| `audit_type` | `product` / `review` / `report` / `certification` |
| `text_fields` | 键名对齐 Java 实体字段名（见下） |
| `image_urls` | 图片 URL 或 `data:image/...;base64` 数据 URL |
| `extra` | 附加信息，可空 |

各类型 `text_fields` 约定：

| audit_type | text_fields 键 |
| --- | --- |
| `product` | `title` `description` `price` `quality` `tradeType` `tradePlace` |
| `review` | `content` |
| `report` | `targetType` `targetId` `reason` `description` |
| `certification` | `school` `studentNo` `realName` |

响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "passed": false,
    "reason": "标题含联系方式引流",
    "risk_category": "联系方式引流",
    "risk_level": "high",
    "suggested_action": "驳回"
  }
}
```

`AuditDecision` 字段：`passed`（是否通过）、`reason`（理由）、`risk_category`（违禁品/色情低俗/虚假信息/联系方式引流/辱骂攻击/身份存疑/正常）、`risk_level`（`low`/`medium`/`high`）、`suggested_action`（通过/驳回/人工复核）。

### AI 客服

- `POST /api/v1/customer-service/chat` —— 同步返回完整回答。
- `POST /api/v1/customer-service/chat/stream` —— SSE 流式输出。

请求体：

```json
{
  "session_id": "user-1",
  "message": "怎么发布商品？",
  "history": []
}
```

| 字段 | 说明 |
| --- | --- |
| `session_id` | 会话 ID，用于隔离历史，默认 `default` |
| `message` | 用户最新消息 |
| `history` | 可选，首次调用时传入客户端已有的 `[{role, content}]` |

---

## 配置说明

配置文件为 `.env`（`app/config.py` 用 pydantic-settings 读取，环境变量优先，字段名忽略大小写）。

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `LLM_BASE_URL` | OpenAI 兼容接口地址 | `https://api.deepseek.com/v1` |
| `LLM_API_KEY` | API Key（**必填**） | 空 |
| `LLM_MODEL` | 模型名 | `deepseek-chat` |
| `LLM_TEMPERATURE` | 采样温度 | `0.1` |
| `LLM_TIMEOUT` | 请求超时（秒） | `60` |
| `EMBEDDING_BASE_URL` | 向量模型地址，留空回退到 `LLM_BASE_URL` | 空 |
| `EMBEDDING_API_KEY` | 向量模型 Key，留空回退到 `LLM_API_KEY` | 空 |
| `EMBEDDING_MODEL` | 向量模型名 | `text-embedding-3-small` |
| `CHROMA_PERSIST_DIR` | 向量库目录 | `./data/chroma` |
| `RAG_TOP_K` | 检索条数 | `4` |
| `APP_PORT` | 服务端口 | `8000` |
| `LOG_LEVEL` | 日志级别 | `INFO` |
| `NACOS_ENABLED` | 是否注册到 Nacos | `true` |
| `NACOS_SERVER_ADDR` | Nacos 地址 | `127.0.0.1:8848` |
| `NACOS_NAMESPACE` | 命名空间（空=public） | 空 |
| `NACOS_GROUP` | 分组 | `DEFAULT_GROUP` |
| `NACOS_EPHEMERAL` | 临时实例（true 需心跳） | `true` |
| `NACOS_HEARTBEAT_INTERVAL` | 心跳间隔（秒） | `5` |
| `NACOS_TIMEOUT` | Nacos 请求超时（秒） | `5` |
| `SERVICE_NAME` | 服务名（Java 侧按此发现） | `ai-service` |
| `SERVICE_IP` | 注册 IP（空=自动探测） | 空 |

> 换模型只改 `.env`：`LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL`。支持所有 OpenAI 兼容服务。审核走 function calling 做结构化输出，请确保所选模型支持工具调用。

---

## 与 Java 后端集成

Java 侧通过 `backend/campus-cloud/ai-api` 模块调用本服务：

### Feign 客户端 `AiFeignClient`

```java
@FeignClient(name = "ai-service", path = "/api/v1")
public interface AiFeignClient {
    @PostMapping("/audit")
    Result<AiAuditVO> audit(@RequestBody AiAuditDTO dto);

    @PostMapping("/customer-service/chat")
    Result<AiChatVO> chat(@RequestBody AiChatDTO dto);
}
```

- 服务名 `ai-service` 由 Nacos 解析，`path=/api/v1` 与本服务路由前缀一致。
- **SSE 流式接口（`/customer-service/chat/stream`）Feign 不支持**，Java 侧需用 HTTP 客户端直连。

### 支撑类 `AiAuditSupport`

封装 Feign 调用与请求体组装，供各业务服务复用：

- `callAudit(AiAuditDTO)`：调用审核，超时/失败/`code!=1` 统一返回 `null`（由调用方降级为人工审核）。
- `putIfPresent(map, key, value)`：值非空才放入 `text_fields`。
- `splitImages(images)`：逗号分隔的图片串 → 列表。

### 实际调用方（biz-service / user-service）

| 调用方 | 场景 | 失败降级 |
| --- | --- | --- |
| `AdminProductServiceImpl` | 管理端辅助审核商品 | `null` → 交给人工判断 |
| `ProductAuditEventListener` | 商品发布后 `AFTER_COMMIT` 异步自动审核 | `null` → 保持「审核中」等人工 |
| `AdminReviewServiceImpl` | 管理端辅助审核评论 | 同上 |
| `AdminReportServiceImpl` | 管理端辅助审核举报 | 同上 |
| `AdminUserServiceImpl` | 管理端辅助审核实名认证 | 同上 |

> 设计约定：**AI 只做「辅助」**，最终决定权在管理端人工审核。异步自动审核通过后自动上架、驳回自动下架并写回原因（见 `ProductAuditEventListener`），但所有远程调用都在事务提交后执行（`@TransactionalEventListener(AFTER_COMMIT)` + `@Async`），不影响主流程。

---

## 如何扩展

### 新增一种审核类型

以新增「公告 `announcement`」为例：

1. **`app/schemas/audit.py`**：给 `AuditType` 枚举加一项 `announcement = "announcement"`。
2. **`app/prompts/audit.py`**：新增 `ANNOUNCEMENT_RULES = """..."""`（写明违规判定规则）。
3. **新建 `app/services/audit/announcement.py`**：

   ```python
   from app.prompts.audit import ANNOUNCEMENT_RULES
   from app.schemas.audit import AuditRequest
   from app.services.audit.base import BaseAuditor

   class AnnouncementAuditor(BaseAuditor):
       audit_type = "announcement"
       rules = ANNOUNCEMENT_RULES

       def render_text(self, request: AuditRequest) -> str:
           f = request.text_fields
           return "请审核以下公告内容：\n" f"- 标题：{f.get('title', '')}\n- 内容：{f.get('content', '')}"
   ```

4. **`app/services/audit/registry.py`**：在 `build_auditors()` 里注册：

   ```python
   from app.services.audit.announcement import AnnouncementAuditor
   ...
   AnnouncementAuditor.audit_type: AnnouncementAuditor(llm),
   ```

5. （可选）在 `tests/test_audit.py` 补一条 `render_text` / 分发用例。

Java 侧随之组装 `AiAuditDTO(auditType="announcement", textFields={...})` 即可。

### 更新客服知识库

把 `.md` / `.txt` 放进 `data/knowledge/`，重跑 `python scripts/ingest.py`。切分参数在 `scripts/ingest.py`（`chunk_size=500`、`chunk_overlap=50`），检索条数在 `.env` 的 `RAG_TOP_K`。

### 换模型

只改 `.env`：`LLM_BASE_URL` / `LLM_API_KEY` / `LLM_MODEL`（`EMBEDDING_*` 留空自动回退到 LLM 配置）。

---

## 错误处理与降级

| 场景 | 行为 |
| --- | --- |
| 业务异常 `AppError` | HTTP 200 + `code=0`，`msg` 携带原因，方便 Feign 直接解析 |
| 参数校验失败 `RequestValidationError` | HTTP 200 + `code=0` + 错误详情 |
| 未捕获异常 | HTTP 500 + `code=0`，日志记录堆栈 |
| 未配置 `LLM_API_KEY` | 首个请求触发 `RuntimeError`（见上文「依赖注入」注意） |
| AI 调用失败 / 超时（Java 侧） | `AiAuditSupport.callAudit` 返回 `null` → 降级人工审核 |
| 向量库缺失 / 损坏 / 未配置 Embedding | `get_vector_store()` 返回 `None` → 客服降级纯对话 |
| Nacos 不可用 | 注册/心跳失败仅告警，不影响服务启动与本地调试 |

---

## 测试

`tests/` 不依赖真实 LLM key：

- `conftest.py`：导入 `app` 前先设 `NACOS_ENABLED=false`（避免测试连 Nacos）；提供 `FakeListChatModel` 与 `ChatService` 夹具。
- `test_audit.py`：审核器文本渲染、多模态消息组装、注册表分发、未知类型抛错等。
- `test_api.py`：`TestClient` + `dependency_overrides` 做接口冒烟，覆盖 `/health` 与 `/api/v1/audit`。
- `test_customer_service.py`：客服会话 / 流式。

```bash
pytest
```

---

## 注意事项与已知限制

1. **会话历史在进程内存**：`ChatService._histories` 只适合单实例。多实例 / 无状态部署时需替换为 Redis 等共享存储。
2. **Feign 不支持 SSE**：客服流式接口需 Java 侧用 HTTP 客户端直连。
3. **图片仅支持 http(s) URL 或 base64 data URL**：`core/images.py` 会拒绝本地路径 / `ftp://` 等其它形式。
4. **结构化输出依赖模型的 function calling**：`with_structured_output` 要求所选模型支持工具调用（DeepSeek、Qwen、Moonshot 均支持）。
5. **LLM 客户端惰性初始化**：`LLM_API_KEY` 缺失不会阻止启动，只在首个请求时报错——排障时优先检查 `.env`。
6. **`data/knowledge/sample-faq.md` 是样例**，上线前替换为真实平台帮助文档并重新 `ingest`。

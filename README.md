# 校集

校园二手交易平台。用户端可以浏览、发布、收藏、下单和私信，管理端可以审核商品、处理举报并查看概况。

## 技术栈

- 后端：Spring Boot 3.4.6、MyBatis、MySQL 8、Redis，JDK 17
- 前端：Vue 3、Vue Router、Vite
- 接口文档：Knife4j，地址为 `http://localhost:<端口>/doc.html`

## 环境

- JDK 17 或更高版本
- Maven 3.9，或直接使用 `backend/mart-server/mvnw.cmd`
- MySQL 8，字符集 `utf8mb4`
- Redis，本地开发可无密码
- Node.js 20 或更高版本，仅构建前端时需要

## 初始化数据库

在仓库根目录执行，按文件名顺序导入：

```bash
mysql -u root -p --default-character-set=utf8mb4 < docs/sql/01-create-database.sql
mysql -u root -p --default-character-set=utf8mb4 campus_mart < docs/sql/02-user.sql
```

`docs/sql` 里从 `02` 到 `17` 的脚本都要导入。`17-admin.sql` 会写入管理员账号 `admin`，密码 `123456`。上线后请立刻修改这个密码。

## 配置后端

配置文件是 `backend/mart-server/src/main/resources/application-dev.yml`。克隆后按本机环境修改这些项：

- `app.port`：服务端口，例如 `8080`
- `db.url`、`db.username`、`db.password`
- `redis.host`、`redis.port`、`redis.password`
- `jwt.*-secret-key`：换成足够长的随机字符串，请求头名称保持 `token`
- `file.storage-type`：本地部署用 `local`
- `file.local.base-path`：例如 `./uploads`
- `file.local.base-url`：填 `/uploads`，页面才能通过前端访问图片

短信尚未接入。注册时调用发送验证码接口，验证码会直接返回，并写在服务日志里。

## 启动后端

在 `backend` 目录打包并启动。启动时的工作目录要能写到 `file.local.base-path`。

```bash
mvnw -f pom.xml -pl mart-server -am -DskipTests package
java -jar mart-server/target/mart-server-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

Windows 使用 `mart-server\mvnw.cmd`。看到服务启动日志后，访问 `http://localhost:<app.port>/doc.html`。

## 启动前端

开发时在 `frontend` 目录执行：

```bash
npm install
npm run dev
```

页面地址是 http://localhost:5173 。`frontend/vite.config.js` 里的 `/user`、`/admin`、`/uploads` 代理必须指向后端的实际端口。`/admin` 既是管理页面，也是管理接口前缀，代理只转发接口请求。

生产构建：

```bash
npm run build
```

产物在 `frontend/dist`。用 Nginx 托管，并把接口转到后端。下面假设后端端口是 `8080`：

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/campus-mart;
    index index.html;

    location /user/ {
        proxy_pass http://127.0.0.1:8080;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
    }

    location = /admin {
        try_files /index.html =404;
    }

    location /admin/ {
        proxy_pass http://127.0.0.1:8080;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

`location = /admin` 用来打开管理页面，`/admin/` 才转给后端接口。

## 页面入口

- 集市：http://localhost:5173/market
- 登录：http://localhost:5173/login
- 管理端：http://localhost:5173/admin ，初始账号 `admin` / `123456`

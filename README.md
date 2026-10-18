# 飞马市集

校园二手交易平台。用户可以浏览、发布、收藏、下单和私信，管理员可以审核商品、处理举报并查看概况。

要在自己电脑上打开网站，需要同时准备四样东西：MySQL、Redis、后端、前端。按下面的顺序做，做完打开 http://localhost:5173 。

## 先安装这些软件

- JDK 17 或更高版本。电脑上如果只有 Java 8，后端启动不了。
- MySQL 8。建库时使用字符集 `utf8mb4`。
- Redis。自己电脑上可以不设密码。
- Node.js 20 或更高版本。用来打开网页。

后端自带 Maven，不用单独安装。Windows 用 `backend\mart-server\mvnw.cmd`。

## 第一步：准备数据库

先把 MySQL 和 Redis 启动。数据库只需要导入一次，以后重启电脑不用再导。

在项目根目录，按编号执行 `docs/sql` 里的脚本。`01` 负责建库，`02` 到 `17` 负责建表和初始数据，不能跳号，也不能只执行前两个。

Windows 可以在 PowerShell 里这样导入，把 `mysql` 换成你电脑上 `mysql.exe` 的路径，密码换成你的 MySQL 密码：

```powershell
mysql -u root -p --default-character-set=utf8mb4 -e "source docs/sql/01-create-database.sql"
Get-ChildItem docs\sql\*.sql | Where-Object { $_.Name -ne "01-create-database.sql" } | Sort-Object Name | ForEach-Object {
  mysql -u root -p --default-character-set=utf8mb4 campus_mart -e "source $($_.FullName)"
}
```

`17-admin.sql` 会写入管理员账号 `admin`，密码 `123456`。这是初始密码，给别人使用前要改掉。

## 第二步：填写后端配置

打开 `backend/mart-server/src/main/resources/application-dev.yml`，按你自己的电脑改这几项：

- `app.port`：后端端口。仓库里的前端默认把请求转到 `8081`，所以这里建议也填 `8081`。如果改成别的端口，下一步的前端配置也要改成同一个数字。
- `db.url`、`db.username`、`db.password`：你的 MySQL 地址、账号和密码。库名是 `campus_mart`。
- `redis.host`、`redis.port`、`redis.password`：本机一般是 `127.0.0.1`、`6379`、空密码。
- `file.storage-type` 填 `local`。
- `file.local.base-path` 填 `./uploads`。上传的图片会放在这里。
- `file.local.base-url` 填 `/uploads`。这样网页才能显示这些图片。

短信还没有接入。注册时点「获取验证码」，验证码会直接出现在页面上，也会写在后端日志里。

## 第三步：启动后端

打开一个终端，进入 `backend` 目录再启动。工作目录必须是 `backend`，图片才会保存到 `backend/uploads`。

Windows：

```powershell
cd backend
.\mart-server\mvnw.cmd -f pom.xml -pl mart-server -am -DskipTests package
java -jar mart-server\target\mart-server-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

这个窗口要一直开着。看到启动完成的日志后，用浏览器打开 http://localhost:8081/doc.html 。能看到接口文档，就说明后端已经启动。如果你把 `app.port` 改成了别的数字，地址里的 `8081` 也要换成那个数字。

## 第四步：启动前端

再开一个终端，不要关掉后端那个窗口。

```powershell
cd frontend
npm install
npm run dev
```

`npm install` 只需要第一次执行。这个窗口也要一直开着。

然后打开：

- 集市：http://localhost:5173/market
- 登录：http://localhost:5173/login
- 管理后台：http://localhost:5173/admin

管理后台的初始账号是 `admin`，密码是 `123456`。

前端页面在 `5173`，数据来自后端。`frontend/vite.config.js` 里的 `/user`、`/admin`、`/uploads` 都要指向后端的实际端口。现在写的是 `8081`。后端端口和这里不一致时，页面能打开，但登录和商品会请求失败。

## 放到服务器上时

自己电脑上开发，做到第四步就可以。要让别人通过域名访问，再做下面这步。

在 `frontend` 目录执行 `npm run build`，生成的网页在 `frontend/dist`。用 Nginx 托管这个目录，并把接口转给同一台机器上的后端。下面假设后端端口是 `8081`：

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/campus-mart;
    index index.html;

    location /user/ {
        proxy_pass http://127.0.0.1:8081;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8081;
    }

    location = /admin {
        try_files /index.html =404;
    }

    location /admin/ {
        proxy_pass http://127.0.0.1:8081;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

`/admin` 这个地址有两个用途：浏览器打开它时是管理页面，程序调用它时是管理接口。所以精确地址 `/admin` 交给网页，带后续路径的 `/admin/` 才转给后端。`8081` 要和服务器上 `app.port` 的实际值一致。

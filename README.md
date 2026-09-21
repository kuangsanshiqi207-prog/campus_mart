# CampusTrade 校园二手交易平台

## 项目简介
校园二手交易平台，包含用户端和管理端。

## 技术栈
- 后端：Spring Boot 3.4.6 + MyBatis + MySQL + Redis
- 前端：Vue3（待开发）
- AI：FastAPI + LangChain（待开发）
- 文档：Knife4j

## 模块结构
- mart-common：通用工具、常量、异常
- mart-pojo：实体、DTO、VO
- mart-server：主服务

## 已完成模块
- [x] 用户认证（注册、登录、验证码、重置密码）

## 开发计划
- [ ] 个人中心
- [ ] 商品模块
- [ ] 交易模块
- [ ] AI 审核
- [ ] AI 客服

## 本地运行
1. 建库建表（sql/user.sql）
2. 启动 MySQL、Redis
3. 修改 application.yml
4. 运行 MartServerApplication
5. 访问 http://localhost:8080/doc.html

## 接口文档
http://localhost:8080/doc.html
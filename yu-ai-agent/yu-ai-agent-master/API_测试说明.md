# 认证接口完整测试指南

## 📌 后端 AuthController 接口清单

已成功实现的接口：

### 1️⃣ 用户注册接口

```
POST /auth/register
Content-Type: application/x-www-form-urlencoded

参数：
- account: 账号（必填）
- password: 密码（必填）
- userName: 用户名（必填）
- userRole: 用户角色（必填）

示例请求：
POST http://localhost:8123/api/auth/register?account=test001&password=123456&userName=测试用户&userRole=miner

响应示例：
{
  "code": 200,
  "msg": "注册成功",
  "data": "550e8400-e29b-41d4-a716-446655440000"  // userId
}
```

### 2️⃣ 用户登录接口

```
POST /auth/login
Content-Type: application/x-www-form-urlencoded

参数：
- account: 账号（必填）
- password: 密码（必填）

示例请求：
POST http://localhost:8123/api/auth/login?account=test001&password=123456

响应示例：
{
  "code": 200,
  "msg": "登录成功",
  "data": "550e8400-e29b-41d4-a716-446655440000"  // userId
}
```

### 3️⃣ 用户登出接口

```
POST /auth/logout

请求头：
- X-User-Id: 用户ID（可选）

示例请求：
POST http://localhost:8123/api/auth/logout
Headers:
  X-User-Id: 550e8400-e29b-41d4-a716-446655440000

响应示例：
{
  "code": 200,
  "msg": "登出成功",
  "data": null
}
```

### 4️⃣ 获取当前用户信息接口（测试用）

```
GET /auth/test

请求头：
- X-User-Id: 用户ID（必填）

示例请求：
GET http://localhost:8123/api/auth/test
Headers:
  X-User-Id: 550e8400-e29b-41d4-a716-446655440000

响应示例：
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "account": "test001",
    "password": "123456",
    "userName": "测试用户",
    "userRole": "miner",
    "phone": null,
    "createTime": "2025-12-08T10:30:45",
    "isDeleted": false
  }
}
```

## 🧪 使用 Curl 测试

### 注册

```bash
curl -X POST "http://localhost:8123/api/auth/register?account=user001&password=pass123&userName=用户1&userRole=miner"
```

### 登录

```bash
curl -X POST "http://localhost:8123/api/auth/login?account=user001&password=pass123"
```

### 登出（使用返回的 userId）

```bash
curl -X POST "http://localhost:8123/api/auth/logout" \
  -H "X-User-Id: 返回的userId"
```

### 获取用户信息

```bash
curl -X GET "http://localhost:8123/api/auth/test" \
  -H "X-User-Id: 返回的userId"
```

## 🔗 前端配置对应

前端 API 调用（`src/api/index.js`）已配置以下接口：

```javascript
// 注册
register(account, password, userName, userRole)
  → POST /auth/register

// 登录
login(account, password)
  → POST /auth/login

// 登出
logout(userId)
  → POST /auth/logout
  → 请求头: X-User-Id: userId

// 获取用户信息
getCurrentUser(userId)
  → GET /auth/test
  → 请求头: X-User-Id: userId
```

## ✅ 前后端对应情况

| 功能         | 前端页面     | 后端接口            | 状态    |
| ------------ | ------------ | ------------------- | ------- |
| 注册         | Register.vue | POST /auth/register | ✅ 完成 |
| 登录         | Login.vue    | POST /auth/login    | ✅ 完成 |
| 登出         | App.vue      | POST /auth/logout   | ✅ 完成 |
| 获取用户信息 | App.vue      | GET /auth/test      | ✅ 完成 |

## 🚀 启动步骤

### 1. 启动后端

```bash
cd yu-ai-agent-master
mvn spring-boot:run
```

### 2. 启动前端

```bash
cd yu-ai-agent-frontend
npm run dev
```

### 3. 打开浏览器

访问：http://localhost:5173

## 💡 注意事项

1. **后端端口**：8123（开发环境）
2. **前端端口**：5173（Vite 开发服务器）
3. **数据库**：PostgreSQL，表位于 sdagent schema
4. **密码存储**：明文存储（学习项目，禁用加密）
5. **用户 ID**：UUID 格式，由后端自动生成

---

**前后端接口已完全对应，可以进行集成测试！** 🎉

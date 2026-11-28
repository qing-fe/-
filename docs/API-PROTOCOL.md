# Red-Black 前后端 API 协议规范

> 版本：v1.1  
> 适用范围：Red-Black 项目所有前后端接口  
> 生效日期：2026-11-26
> 变更记录：v1.1 将响应字段 `message` 修正为 `message`

---

## 1. 通用响应协议

### 1.1 统一响应结构

所有接口（除文件下载等特殊场景外）必须返回统一的 JSON 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-11-26T14:30:00"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `code` | int | 是 | 业务状态码，见 **第 4 章 错误码规范** |
| `message` | string | 是 | 提示信息 |
| `data` | any | 否 | 响应数据，可为对象/数组/null |
| `timestamp` | string | 是 | 响应时间，ISO 8601 格式（`yyyy-MM-dd'T'HH:mm:ss`） |

### 1.2 成功响应示例

**普通数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10086,
    "username": "zhangsan"
  },
  "timestamp": "2026-11-26T14:30:00"
}
```

**列表数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "username": "a" },
    { "id": 2, "username": "b" }
  ],
  "timestamp": "2026-11-26T14:30:00"
}
```

**无数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": "2026-11-26T14:30:00"
}
```

### 1.3 失败响应示例

```json
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": "2026-11-26T14:30:00"
}
```

---

## 2. 命名规范

### 2.1 分层命名约定

| 层级 | 语言/场景 | 命名规范 | 示例 |
|------|----------|---------|------|
| **前端变量** | JavaScript/TypeScript | 小驼峰（camelCase） | `userName`, `accessToken`, `createdAt` |
| **前端组件** | Vue/React | 大驼峰（PascalCase） | `UserCard`, `LoginForm` |
| **前端常量** | JavaScript/TypeScript | 全大写 + 下划线（UPPER_SNAKE_CASE） | `MAX_PAGE_SIZE`, `API_BASE_URL` |
| **后端 Java** | Java 类/字段 | 类：大驼峰，字段：小驼峰 | `UserService`, `accessToken` |
| **后端 URL** | RESTful API | 全小写 + 连字符/斜杠 | `/api/users`, `/api/auth/login` |
| **数据库表名** | MySQL | 全小写 + 下划线（snake_case） | `users`, `user_auth_logs` |
| **数据库字段** | MySQL | 全小写 + 下划线（snake_case） | `user_name`, `created_at` |
| **Redis Key** | Redis | 冒号分隔（全小写） | `user:token:10086`, `login:fail:13800138000` |
| **环境变量** | Shell/Docker | 全大写 + 下划线 | `JWT_SECRET`, `DB_PASSWORD` |

### 2.2 前后端传输字段命名

**JSON 传输时统一使用小驼峰（camelCase）：**

| 含义 | JSON 字段名 | ❌ 禁止写法 |
|------|------------|------------|
| 用户名 | `userName` | `user_name`, `UserName` |
| 访问令牌 | `accessToken` | `access_token`, `access-token` |
| 创建时间 | `createdAt` | `created_at`, `createdTime` |
| 头像地址 | `avatarUrl` | `avatar_url`, `avatarURL` |
| 验证码 | `verifyCode` | `verify_code`, `code` |

> ⚠️ **特殊说明**：当前项目中数据库实体类字段仍使用下划线（如 `created_at`），但对外暴露给前端的 VO/DTO 必须转换为驼峰命名。

---

## 3. HTTP 协议规范

### 3.1 请求方法

| 操作 | 方法 | 示例 |
|------|------|------|
| 查询 | `GET` | `GET /api/users` |
| 创建 | `POST` | `POST /api/users` |
| 更新（全量） | `PUT` | `PUT /api/users/10086` |
| 更新（部分） | `PATCH` | `PATCH /api/users/10086` |
| 删除 | `DELETE` | `DELETE /api/users/10086` |

### 3.2 请求头规范

| Header | 必填 | 说明 |
|--------|------|------|
| `Content-Type` | 是 | 固定 `application/json` |
| `Authorization` | 条件 | 认证接口需携带：`Bearer <accessToken>` |
| `X-Refresh-Token` | 条件 | 刷新 Token 时携带 Refresh Token |
| `X-Device-Id` | 否 | 设备唯一标识（可选，用于多端管理） |
| `X-Request-Id` | 否 | 请求追踪 ID（可选，用于日志追踪） |

### 3.3 URL 设计规范

```
/api/{模块}/{资源}/{动作?}
```

- 统一前缀 `/api`
- 资源名用复数名词
- 动作尽量用 HTTP 方法表达，特殊情况用动词

**示例：**
```
GET    /api/auth/captcha          # 获取验证码
POST   /api/auth/login/password   # 密码登录
POST   /api/auth/refresh          # 刷新 Token
POST   /api/auth/logout           # 退出登录
GET    /api/users                 # 用户列表
GET    /api/users/10086           # 用户详情
PUT    /api/users/10086           # 更新用户
```

---

## 4. 错误码规范

### 4.1 HTTP 状态码 vs 业务状态码

| HTTP 状态码 | 场景 | 业务 code |
|------------|------|----------|
| `200 OK` | 正常响应 | 200 |
| `400 Bad Request` | 请求参数错误 | 400 |
| `401 Unauthorized` | 未登录或 Token 失效 | 401 |
| `403 Forbidden` | 无权访问 | 403 |
| `404 Not Found` | 资源不存在 | 404 |
| `500 Internal Server Error` | 服务端内部错误 | 500 |

### 4.2 业务状态码定义

| code | 含义 | 说明 |
|------|------|------|
| `200` | 成功 | 请求处理成功 |
| `400` | 请求参数错误 | 参数校验失败、格式错误 |
| `401` | 未授权 | Token 缺失、过期、无效 |
| `403` | 禁止访问 | 权限不足、账号被禁用 |
| `404` | 资源不存在 | 用户不存在、课程不存在 |
| `409` | 资源冲突 | 用户名已存在、邮箱已注册 |
| `429` | 请求过于频繁 | 验证码发送太频繁、刷接口限制 |
| `500` | 服务端错误 | 数据库异常、未知错误 |

### 4.3 认证相关扩展码

| code | 含义 | 场景 |
|------|------|------|
| `401001` | Access Token 过期 | 前端应自动调用 `/refresh` |
| `401002` | Refresh Token 过期 | 需重新登录 |
| `401003` | Token 类型错误 | AT 被误用为 RT 或反之 |
| `401004` | 账号被锁定 | 登录失败次数过多 |
| `401005` | 登录状态异常 | RT 重放攻击，全部 Token 被吊销 |
| `401006` | 设备指纹不匹配 | 疑似盗用，需重新登录 |

---

## 5. Token 认证协议

### 5.1 双 Token 机制

| Token 类型 | 有效期 | 载体 | 用途 |
|------------|--------|------|------|
| **Access Token (AT)** | 2 小时 | `Authorization: Bearer xxx` | 访问业务接口 |
| **Refresh Token (RT)** | 7 天 | `X-Refresh-Token: xxx` | 换取新的 AT + RT |

### 5.2 登录成功响应

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  },
  "timestamp": "2026-11-26T14:30:00"
}
```

### 5.3 请求业务接口

```http
GET /api/users/profile HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json
```

### 5.4 Token 刷新流程

**触发时机：**
- AT 过期（收到 401）
- AT 即将过期（剩余 < 10 分钟，前端主动刷新）

**刷新请求：**
```http
POST /api/auth/refresh HTTP/1.1
X-Refresh-Token: eyJhbGciOiJIUzI1NiIs...
```

**刷新响应（Rotation 策略）：**
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGci...（新的）",
    "refreshToken": "eyJhbGci...（新的，旧的作废）",
    "tokenType": "Bearer",
    "expiresIn": 7200
  },
  "timestamp": "2026-11-26T14:30:00"
}
```

### 5.5 退出登录

```http
POST /api/auth/logout HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
X-Refresh-Token: eyJhbGciOiJIUzI1NiIs...
```

---

## 6. 数据类型规范

### 6.1 前后端传输类型映射

| 含义 | Java 类型 | JSON 类型 | 示例 |
|------|----------|----------|------|
| 自增 ID | `Long` | number | `10086` |
| 字符串 | `String` | string | `"zhangsan"` |
| 布尔值 | `Boolean` | boolean | `true`, `false` |
| 日期时间 | `LocalDateTime` | string | `"2026-11-26T14:30:00"` |
| 日期 | `LocalDate` | string | `"2026-11-26"` |
| 金额 | `BigDecimal` | string | `"99.99"`（避免浮点精度问题） |
| 枚举值 | `Integer` / `String` | number/string | `1` 或 `"ADMIN"` |

### 6.2 时间格式

**统一使用 ISO 8601 格式：**
- 日期时间：`yyyy-MM-dd'T'HH:mm:ss` → `"2026-11-26T14:30:00"`
- 日期：`yyyy-MM-dd` → `"2026-11-26"`
- 时间：`HH:mm:ss` → `"14:30:00"`

> ⚠️ 禁止使用时间戳（如 `1732617000000`）或自定义格式（如 `"2026/11/26"`）。

---

## 7. 分页规范

### 7.1 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `pageNum` | int | 否 | 1 | 当前页码，从 1 开始 |
| `pageSize` | int | 否 | 10 | 每页条数，最大 100 |
| `sortField` | string | 否 | `createdAt` | 排序字段（驼峰） |
| `sortOrder` | string | 否 | `desc` | 排序方向：`asc` / `desc` |

### 7.2 响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [ ... ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  },
  "timestamp": "2026-11-26T14:30:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `list` | array | 当前页数据列表 |
| `total` | int | 总记录数 |
| `pageNum` | int | 当前页码 |
| `pageSize` | int | 每页条数 |
| `pages` | int | 总页数 |

---

## 8. 安全规范

### 8.1 敏感字段传输

| 场景 | 要求 |
|------|------|
| 密码传输 | 前端先用 **RSA 公钥加密**，后端私钥解密后再 **BCrypt 哈希** |
| Token 传输 | 使用 `Authorization` Header，**禁止放 URL 参数或 Body 中** |
| 验证码 | 仅用于校验，**禁止在响应中返回**真实验证码值 |

### 8.2 防刷限制

| 场景 | 限制策略 |
|------|---------|
| 登录失败 | 同一账号 5 次失败锁定 30 分钟 |
| 验证码发送 | 同一邮箱 60 秒内只能发送 1 次 |
| 接口访问 | 全局速率限制（如 100 次/分钟/IP） |

---

## 9. 附录：项目已知问题记录

> 以下问题将在后续版本中逐步修正：

| 问题 | 位置 | 说明 |
|------|------|------|
| 数据库字段下划线 | `Users.java` 等 Entity | 数据库用 `created_at`，VO 应转换为 `createdAt` |

---

## 10. 文档维护

- **变更记录**：每次协议变更需在本文档顶部更新版本号，并在 Git Commit 中说明
- **兼容性**：字段删除/类型变更属于 **破坏性变更（Breaking Change）**，需提前通知前端
- **生效范围**：新增接口必须遵守本协议，存量接口逐步迁移

---

**文档维护人：** 后端团队  
**审核人：** 前端团队 + 后端 Team Leader  
**下次评审日期：** 2027-02-26
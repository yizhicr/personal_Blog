# 前端 API 接口文档

本文档详细说明了前端如何与后端API进行交互。

## 基础配置

- **API 基础地址**: `http://localhost:8080/api` (开发环境)
- **请求头**: `Content-Type: application/json`
- **认证方式**: JWT Token (通过 Authorization: Bearer {token} 传递)

## 请求封装

前端使用 Axios 封装了 API 请求，位于 `src/utils/request.ts`。

### 请求拦截器
- 自动在请求头中添加 JWT Token (从 localStorage 获取)
- Token 格式: `Bearer {token}`

### 响应拦截器
- 自动处理响应格式，若响应包含 `success` 字段且为 `false`，则显示错误消息
- 统一处理错误状态码 (400, 401, 403, 404, 500)
- 自动跳转到登录页面 (当 Token 过期时)

## 接口说明

### 认证相关

#### 1. 用户注册
- **接口**: `POST /auth/register`
- **参数**:
  ```json
  {
    "username": "用户名",
    "email": "邮箱",
    "password": "密码"
  }
  ```
- **示例**:
  ```ts
  const response = await request.post('/auth/register', {
    username: 'testuser',
    email: 'test@example.com',
    password: 'password123'
  })
  ```

#### 2. 用户登录
- **接口**: `POST /auth/login`
- **参数**:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- **示例**:
  ```ts
  const response = await axios.post('http://localhost:8080/api/auth/login', {
    username: 'testuser',
    password: 'password123'
  })
  // 保存返回的 JWT token 到 localStorage
  localStorage.setItem('token', response.data.data.token)
  ```

#### 3. 验证 JWT 令牌
- **接口**: `GET /auth/validate`
- **认证**: 需要 JWT Token
- **示例**:
  ```ts
  const response = await request.get('/auth/validate')
  ```

### 文章相关

#### 1. 创建文章
- **接口**: `POST /articles`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "title": "文章标题",
    "content": "文章内容",
    "summary": "文章摘要",
    "coverImage": "封面图片URL (可选)",
    "authorId": "作者ID (可选)",
    "categoryId": "分类ID (可选)",
    "tagNames": ["标签名数组 (可选)"]
  }
  ```
- **示例**:
  ```ts
  const response = await request.post('/articles', {
    title: '测试文章标题',
    content: '这是测试文章的内容',
    summary: '测试摘要'
  })
  ```

#### 2. 获取文章列表
- **接口**: `GET /articles`
- **参数**:
  - `page`: 页码 (默认: 0)
  - `size`: 每页数量 (默认: 10)
  - `title`: 文章标题 (可选)
  - `categoryId`: 分类ID (可选)
  - `tagId`: 标签ID (可选)
- **示例**:
  ```ts
  const response = await request.get('/articles')
  ```

#### 3. 获取已发布文章列表
- **接口**: `GET /articles/published`
- **参数**:
  - `page`: 页码 (默认: 0)
  - `size`: 每页数量 (默认: 10)
  - `title`: 文章标题 (可选)
  - `categoryId`: 分类ID (可选)
  - `tagId`: 标签ID (可选)
- **示例**:
  ```ts
  const response = await request.get('/articles/published')
  ```

#### 4. 获取文章详情
- **接口**: `GET /articles/{id}`
- **参数**: `id` - 文章ID
- **示例**:
  ```ts
  const response = await request.get(`/articles/1`)
  ```

#### 5. 更新文章
- **接口**: `PUT /articles/{id}`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "title": "更新后的标题",
    "content": "更新后的内容",
    "summary": "更新后的摘要",
    "coverImage": "封面图片URL (可选)",
    "categoryId": "分类ID (可选)",
    "tagNames": ["标签名数组 (可选)"]
  }
  ```
- **示例**:
  ```ts
  const response = await request.put('/articles/1', {
    title: '更新后的标题',
    content: '更新后的内容',
    summary: '更新后的摘要'
  })
  ```

#### 6. 删除文章
- **接口**: `DELETE /articles/{id}`
- **认证**: 需要 JWT Token
- **参数**: `id` - 文章ID
- **示例**:
  ```ts
  const response = await request.delete('/articles/1')
  ```

### 评论相关

#### 1. 发布评论或回复
- **接口**: `POST /comments`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "articleId": 1,
    "content": "评论内容",
    "parentId": 1  // 可选，回复某条评论时使用
  }
  ```
- **示例**:
  ```ts
  const response = await request.post('/comments', {
    articleId: 1,
    content: '这是一条评论',
    parentId: null  // 主评论不需要父评论ID
  })
  ```

#### 2. 获取文章的所有评论
- **接口**: `GET /comments/article/{articleId}`
- **认证**: 需要 JWT Token
- **参数**: `articleId` - 文章ID
- **示例**:
  ```ts
  const response = await request.get('/comments/article/1')
  ```

#### 3. 更新评论
- **接口**: `PUT /comments/{commentId}`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "content": "更新后的评论内容",
    "status": 1  // 0-待审核，1-已发布，2-已删除，3-垃圾评论
  }
  ```
- **示例**:
  ```ts
  const response = await request.put('/comments/1', {
    content: '更新后的评论内容'
  })
  ```

#### 4. 删除评论
- **接口**: `DELETE /comments/{commentId}`
- **认证**: 需要 JWT Token
- **参数**: `commentId` - 评论ID
- **示例**:
  ```ts
  const response = await request.delete('/comments/1')
  ```

### 分类相关

#### 1. 获取所有分类
- **接口**: `GET /categories`
- **示例**:
  ```ts
  const response = await request.get('/categories')
  ```

#### 2. 创建分类
- **接口**: `POST /categories`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "name": "分类名称",
    "description": "分类描述"
  }
  ```
- **示例**:
  ```ts
  const response = await request.post('/categories', {
    name: '技术分享',
    description: '技术相关内容'
  })
  ```

### 标签相关

#### 1. 获取所有标签
- **接口**: `GET /tags`
- **示例**:
  ```ts
  const response = await request.get('/tags')
  ```

#### 2. 创建标签
- **接口**: `POST /tags`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "name": "标签名称"
  }
  ```
- **示例**:
  ```ts
  const response = await request.post('/tags', {
    name: 'JavaScript'
  })
  ```

### 用户相关

#### 1. 获取当前用户信息
- **接口**: `GET /users/me`
- **认证**: 需要 JWT Token
- **示例**:
  ```ts
  const response = await request.get('/users/me')
  ```

#### 2. 更新用户信息
- **接口**: `PUT /users/{id}`
- **认证**: 需要 JWT Token
- **参数**:
  ```json
  {
    "username": "string",
    "email": "string",
    "nickname": "string",
    "avatar": "string"
  }
  ```
- **示例**:
  ```ts
  const response = await request.put('/users/1', {
    username: 'newusername',
    email: 'newemail@example.com',
    nickname: '新昵称'
  })
  ```

## 错误处理

前端对以下错误状态码进行了统一处理：

- `400`: 请求参数错误
- `401`: 登录已过期，请重新登录 (自动跳转到登录页)
- `403`: 没有权限访问
- `404`: 请求的资源不存在
- `500`: 服务器内部错误

## 环境变量

- `VITE_API_BASE_URL`: API 基础地址
- `VITE_APP_TITLE`: 应用标题

## 注意事项

1. 所有需要认证的接口都需要在请求头中自动添加 JWT Token
2. JWT Token 从 localStorage 中获取，键名为 'token'
3. 响应拦截器会自动处理统一格式的响应数据，只返回 `data` 字段的内容
4. 对于未按统一格式返回的接口，需要特别处理
5. 在开发环境中，API 地址为 `http://localhost:8080/api`
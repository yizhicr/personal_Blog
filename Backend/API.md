# 博客系统 API 文档

## 目录
- [认证接口](#认证接口)
- [文章接口](#文章接口)
- [评论接口](#评论接口)
- [分类接口](#分类接口)
- [标签接口](#标签接口)
- [文件接口](#文件接口)
- [用户接口](#用户接口)

## 认证接口

### 用户注册
- **POST** `/api/auth/register`
- **请求体**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "nickname": "string"
}
```
- **响应**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "string",
    "email": "string",
    "nickname": "string"
  },
  "message": "string"
}
```

### 用户登录
- **POST** `/api/auth/login`
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "success": true,
  "data": {
    "token": "string",
    "user": {
      "id": 1,
      "username": "string",
      "email": "string",
      "nickname": "string"
    }
  },
  "message": "string"
}
```

### 验证JWT令牌
- **GET** `/api/auth/validate`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **响应 (令牌有效)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "string",
    "email": "string",
    "nickname": "string"
  }
}
```
- **响应 (令牌无效)**:
```json
{
  "success": false,
  "message": "令牌无效或已过期"
}
```

## 文章接口

### 创建文章
- **POST** `/api/articles`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "title": "string",
  "content": "string",
  "summary": "string",
  "coverImage": "string",
  "authorId": 1,
  "categoryId": 1,
  "tagNames": ["string"]
}
```
- **响应**:
```json
{
  "success": true,
  "data": {},
  "message": "string"
}
```

### 获取文章列表
- **GET** `/api/articles`
- **参数**:
  - page: 页码 (默认: 0)
  - size: 每页数量 (默认: 10)
  - title: 文章标题 (可选)
  - categoryId: 分类ID (可选)
  - tagId: 标签ID (可选)
- **响应**:
```json
{
  "success": true,
  "data": [],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 100
  },
  "message": "string"
}
```

### 获取已发布文章列表
- **GET** `/api/articles/published`
- **参数**:
  - page: 页码 (默认: 0)
  - size: 每页数量 (默认: 10)
  - title: 文章标题 (可选)
  - categoryId: 分类ID (可选)
  - tagId: 标签ID (可选)
- **响应**:
```json
{
  "success": true,
  "data": [],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 100
  },
  "message": "string"
}
```

### 获取文章详情
- **GET** `/api/articles/{id}`
- **参数**: id - 文章ID
- **响应**:
```json
{
  "success": true,
  "data": {},
  "commentCount": 10,
  "message": "string"
}
```

### 更新文章
- **PUT** `/api/articles/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "title": "string",
  "content": "string",
  "summary": "string",
  "coverImage": "string",
  "categoryId": 1,
  "tagNames": ["string"]
}
```

### 删除文章
- **DELETE** `/api/articles/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

### 搜索文章
- **GET** `/api/articles/search`
- **参数**:
  - keyword: 搜索关键词
  - page: 页码 (默认: 0)
  - size: 每页数量 (默认: 10)

### 高级搜索
- **POST** `/api/articles/advanced-search`
- **请求体**:
```json
{
  "keyword": "string",
  "categoryId": 1,
  "tagIds": [1, 2],
  "authorId": 1,
  "page": 0,
  "size": 10
}
```

## 评论接口

### 发布评论或回复
- **POST** `/api/comments`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "articleId": 1,
  "content": "评论内容",
  "parentId": 1  // 可选，回复某条评论时使用
}
```
- **响应**:
```json
{
  "id": 1,
  "content": "评论内容",
  "articleId": 1,
  "authorId": 1,
  "authorName": "用户名",
  "authorAvatar": "头像URL",
  "parentId": 1,
  "status": 1,
  "depth": 0,
  "createdAt": "2026-01-10T12:30:00",
  "updatedAt": "2026-01-10T12:30:00"
}
```

### 获取文章的所有评论
- **GET** `/api/comments/article/{articleId}`
- **参数**:
  - status: 评论状态 (默认: 1, 已发布)
- **响应**:
```json
[
  {
    "id": 1,
    "content": "评论内容",
    "articleId": 1,
    "authorId": 1,
    "authorName": "用户名",
    "authorAvatar": "头像URL",
    "parentId": null,
    "status": 1,
    "depth": 0,
    "createdAt": "2026-01-10T12:30:00",
    "updatedAt": "2026-01-10T12:30:00"
  }
]
```

### 分页获取文章评论
- **GET** `/api/comments/article/{articleId}/page`
- **参数**:
  - page: 页码 (默认: 0)
  - size: 每页数量 (默认: 10)
  - status: 评论状态 (默认: 1, 已发布)
- **响应**:
```json
{
  "content": [],
  "pageable": {},
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {},
  "numberOfElements": 10,
  "empty": false
}
```

### 更新评论
- **PUT** `/api/comments/{commentId}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "content": "更新后的评论内容",
  "status": 1  // 0-待审核，1-已发布，2-已删除，3-垃圾评论
}
```

### 删除评论
- **DELETE** `/api/comments/{commentId}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **响应**: `true` 或 `false`

### 获取用户评论列表
- **GET** `/api/comments/user/{userId}`
- **参数**:
  - status: 评论状态 (默认: 1, 已发布)

### 统计文章评论数量
- **GET** `/api/comments/count/article/{articleId}`
- **参数**:
  - status: 评论状态 (默认: 1, 已发布)
- **响应**: 评论数量 (数字)

## 分类接口

### 获取所有分类
- **GET** `/api/categories`

### 创建分类
- **POST** `/api/categories`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "name": "分类名称",
  "description": "分类描述"
}
```

### 更新分类
- **PUT** `/api/categories/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

### 删除分类
- **DELETE** `/api/categories/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

## 标签接口

### 获取所有标签
- **GET** `/api/tags`

### 创建标签
- **POST** `/api/tags`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "name": "标签名称"
}
```

### 更新标签
- **PUT** `/api/tags/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

### 删除标签
- **DELETE** `/api/tags/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

## 文件接口

### 上传文件
- **POST** `/api/files/upload`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **表单数据**: `file` (文件字段)

### 获取文件列表
- **GET** `/api/files`

### 删除文件
- **DELETE** `/api/files/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

## 用户接口

### 获取当前用户信息
- **GET** `/api/users/me`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

### 更新用户信息
- **PUT** `/api/users/{id}`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "username": "string",
  "email": "string",
  "nickname": "string",
  "avatar": "string"
}
```

### 修改密码
- **PUT** `/api/users/{id}/password`
- **认证**: JWT Token
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

## 基础信息

- **协议**: HTTPS/HTTP
- **域名**: `localhost:8080` (开发环境)
- **Base URL**: `/api`
- **认证方式**: JWT Token
- **请求头**: `Authorization: Bearer {token}`

---

## 1. 用户认证接口

### 1.1 用户注册

- **接口路径**: `POST /api/auth/register`
- **功能描述**: 新用户注册
- **请求体**:

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "nickname": "string"
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户"
  }
}
```

### 1.2 用户登录

- **接口路径**: `POST /api/auth/login`
- **功能描述**: 用户登录获取JWT Token
- **请求体**:

```json
{
  "username": "string",
  "password": "string"
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户"
  }
}
```

### 1.3 获取当前用户信息

- **接口路径**: `GET /api/auth/me`
- **功能描述**: 获取当前登录用户信息
- **请求头**: `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户"
  }
}
```

### 1.4 注销

- **接口路径**: `POST /api/auth/logout`
- **功能描述**: 注销当前用户
- **请求头**: `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "success": true,
  "message": "注销成功"
}
```

---

## 2. 文章管理接口

### 2.1 创建文章

- **接口路径**: `POST /api/articles`
- **功能描述**: 创建新文章
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:

```json
{
  "title": "文章标题",
  "content": "文章内容",
  "summary": "文章摘要",
  "status": "draft/published",
  "coverImage": "封面图片URL",
  "authorId": 1,
  "categoryId": 1,
  "tagNames": ["标签1", "标签2"]
}
```

- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "文章标题",
    "content": "文章内容",
    "summary": "文章摘要",
    "status": "draft",
    "coverImage": "封面图片URL",
    "views": 0
  },
  "message": "文章创建成功"
}
```

### 2.2 获取所有文章

- **接口路径**: `GET /api/articles`
- **功能描述**: 获取文章列表（分页）
- **参数**:
  - `page`: 页码，默认为0
  - `size`: 每页数量，默认为10
  - `title`: 标题筛选
  - `categoryId`: 分类ID筛选
  - `tagId`: 标签ID筛选

- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "文章标题",
      "summary": "文章摘要",
      "status": "published",
      "views": 10
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 1
  },
  "message": "获取文章列表成功"
}
```

### 2.3 获取已发布文章

- **接口路径**: `GET /api/articles/published`
- **功能描述**: 获取已发布的文章列表（分页）
- **参数**:
  - `page`: 页码，默认为0
  - `size`: 每页数量，默认为10
  - `title`: 标题筛选
  - `categoryId`: 分类ID筛选
  - `tagId`: 标签ID筛选

- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "已发布文章标题",
      "summary": "已发布文章摘要",
      "status": "published",
      "views": 10
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 1
  },
  "message": "获取已发布文章列表成功"
}
```

### 2.4 获取文章详情

- **接口路径**: `GET /api/articles/{id}`
- **功能描述**: 获取指定ID的文章详情
- **参数**:
  - `id`: 文章ID
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "文章标题",
    "content": "文章内容",
    "summary": "文章摘要",
    "status": "published",
    "views": 11
  },
  "message": "获取文章成功"
}
```

> 注意：每次获取文章详情时，浏览次数会自动增加。

### 2.5 更新文章

- **接口路径**: `PUT /api/articles/{id}`
- **功能描述**: 更新指定ID的文章
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `id`: 文章ID
- **请求体**:

```json
{
  "title": "更新后的文章标题",
  "content": "更新后的文章内容",
  "summary": "更新后的文章摘要",
  "status": "draft/published",
  "coverImage": "更新后的封面图片URL",
  "categoryId": 1,
  "tagNames": ["更新后的标签1", "更新后的标签2"]
}
```

- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "更新后的文章标题",
    "content": "更新后的文章内容",
    "summary": "更新后的文章摘要",
    "status": "published",
    "views": 11
  },
  "message": "文章更新成功"
}
```

### 2.6 删除文章

- **接口路径**: `DELETE /api/articles/{id}`
- **功能描述**: 删除指定ID的文章
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `id`: 文章ID
- **响应示例**:

```json
{
  "success": true,
  "message": "文章删除成功"
}
```

### 2.7 搜索文章

- **接口路径**: `GET /api/articles/search`
- **功能描述**: 根据关键词搜索文章
- **参数**:
  - `keyword`: 搜索关键词
  - `page`: 页码，默认为0
  - `size`: 每页数量，默认为10
- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "搜索到的文章标题",
      "summary": "搜索到的文章摘要",
      "status": "published",
      "views": 11
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 1
  },
  "message": "搜索文章成功"
}
```

### 2.8 高级搜索

- **接口路径**: `POST /api/articles/advanced-search`
- **功能描述**: 高级搜索文章
- **请求体**:

```json
{
  "keyword": "搜索关键词",
  "categoryId": 1,
  "tagIds": [1, 2],
  "authorId": 1,
  "status": "published",
  "page": 0,
  "size": 10
}
```

- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "搜索到的文章标题",
      "summary": "搜索到的文章摘要",
      "status": "published",
      "views": 11
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "total": 1
  },
  "message": "高级搜索成功"
}
```

---

## 3. 分类管理接口

### 3.1 获取所有分类

- **接口路径**: `GET /api/categories`
- **功能描述**: 获取所有文章分类
- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "技术分享",
      "description": "技术相关内容"
    }
  ]
}
```

### 3.2 获取分类详情

- **接口路径**: `GET /api/categories/{id}`
- **功能描述**: 获取指定ID的分类详情
- **参数**:
  - `id`: 分类ID
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "技术分享",
    "description": "技术相关内容"
  }
}
```

### 3.3 创建分类

- **接口路径**: `POST /api/categories`
- **功能描述**: 创建新分类
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:

```json
{
  "name": "分类名称",
  "description": "分类描述"
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "分类创建成功",
  "data": {
    "id": 1,
    "name": "分类名称",
    "description": "分类描述"
  }
}
```

### 3.4 更新分类

- **接口路径**: `PUT /api/categories/{id}`
- **功能描述**: 更新指定ID的分类
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `id`: 分类ID
- **请求体**:

```json
{
  "name": "更新后的分类名称",
  "description": "更新后的分类描述"
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "分类更新成功",
  "data": {
    "id": 1,
    "name": "更新后的分类名称",
    "description": "更新后的分类描述"
  }
}
```

### 3.5 删除分类

- **接口路径**: `DELETE /api/categories/{id}`
- **功能描述**: 删除指定ID的分类
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `id`: 分类ID
- **响应示例**:

```json
{
  "success": true,
  "message": "分类删除成功"
}
```

---

## 4. 标签管理接口

### 4.1 获取所有标签

- **接口路径**: `GET /api/tags`
- **功能描述**: 获取所有标签
- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Java",
      "count": 5
    }
  ]
}
```

### 4.2 获取热门标签

- **接口路径**: `GET /api/tags/hot`
- **功能描述**: 获取热门标签
- **参数**:
  - `limit`: 返回数量，默认为10
- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Java",
      "count": 5
    }
  ]
}
```

### 4.3 创建标签

- **接口路径**: `POST /api/tags`
- **功能描述**: 创建新标签
- **请求头**: `Authorization: Bearer {token}`
- **请求体**:

```json
{
  "name": "标签名称"
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "标签创建成功",
  "data": {
    "id": 1,
    "name": "标签名称",
    "count": 0
  }
}
```

---

## 5. 文件上传接口

### 5.1 上传文件

- **接口路径**: `POST /api/files/upload`
- **功能描述**: 上传文件
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `files`: 多个文件参数
- **响应示例**:

```json
{
  "success": true,
  "message": "文件上传成功",
  "data": [
    {
      "id": 1,
      "originalName": "原始文件名.jpg",
      "fileName": "存储文件名.jpg",
      "filePath": "/path/to/file.jpg",
      "fileSize": 1024,
      "contentType": "image/jpeg",
      "uploadTime": "2023-01-01T10:00:00"
    }
  ]
}
```

### 5.2 获取所有文件

- **接口路径**: `GET /api/files`
- **功能描述**: 获取所有已上传文件列表
- **请求头**: `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "originalName": "原始文件名.jpg",
      "fileName": "存储文件名.jpg",
      "filePath": "/path/to/file.jpg",
      "fileSize": 1024,
      "contentType": "image/jpeg",
      "uploadTime": "2023-01-01T10:00:00"
    }
  ]
}
```

### 5.3 获取文件详情

- **接口路径**: `GET /api/files/{id}`
- **功能描述**: 获取指定ID的文件详情
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `id`: 文件ID
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "originalName": "原始文件名.jpg",
    "fileName": "存储文件名.jpg",
    "filePath": "/path/to/file.jpg",
    "fileSize": 1024,
    "contentType": "image/jpeg",
    "uploadTime": "2023-01-01T10:00:00"
  }
}
```

### 5.4 删除文件

- **接口路径**: `DELETE /api/files/{id}`
- **功能描述**: 删除指定ID的文件
- **请求头**: `Authorization: Bearer {token}`
- **参数**:
  - `id`: 文件ID
- **响应示例**:

```json
{
  "success": true,
  "message": "文件删除成功"
}
```

---

## 6. 用户管理接口

### 6.1 获取用户信息

- **接口路径**: `GET /api/users/{id}`
- **功能描述**: 获取指定ID的用户信息
- **参数**:
  - `id`: 用户ID
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatar": "/path/to/avatar.jpg"
  }
}
```

### 6.2 获取当前用户资料

- **接口路径**: `GET /api/users/profile`
- **功能描述**: 获取当前登录用户的详细资料
- **响应示例**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatar": "/path/to/avatar.jpg"
  }
}
```

### 6.3 更新用户信息

- **接口路径**: `PUT /api/users/{id}`
- **功能描述**: 更新指定ID的用户信息
- **参数**:
  - `id`: 用户ID
- **请求体**:

```json
{
  "nickname": "新昵称",
  "email": "newemail@example.com",
  "avatar": "/path/to/new/avatar.jpg"
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "newemail@example.com",
    "nickname": "新昵称",
    "avatar": "/path/to/new/avatar.jpg"
  }
}
```

---

## 错误码说明

| 错误码 | 描述 |
|--------|------|
| 400 | 请求参数错误或服务处理异常 |
| 401 | 未授权访问，需要登录 |
| 403 | 权限不足，禁止访问 |
| 404 | 请求的资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有需要身份验证的接口都需要在请求头中携带有效的JWT Token
2. 文件上传时，一次可以上传多个文件
3. 所有分页接口均使用page（从0开始）和size参数
4. 用户密码不会在任何响应中返回
5. 文章浏览数会在获取文章详情时自动增加
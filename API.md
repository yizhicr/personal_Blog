# 博客系统API文档

## 项目概述

这是一个基于Spring Boot和Vue.js的全栈博客系统，提供了完整的博客功能，包括用户认证、文章管理、分类标签、评论系统等。

## 技术栈

- 后端：Spring Boot、Spring Security、JWT、Redis、MySQL
- 前端：Vue.js 3、TypeScript、Vite、Element Plus
- 数据库：MySQL 8.0+
- 缓存：Redis
- 构建工具：Maven、npm

## 接口约定

### 响应格式

成功响应格式：
```json
{
  "success": true,
  "data": {},
  "message": "成功消息"
}
```

失败响应格式：
```json
{
  "success": false,
  "message": "错误消息"
}
```

### 认证方式

大部分接口需要JWT Token认证，请求头需要包含：
```
Authorization: Bearer {token}
```

## 用户认证接口

### 1. 用户注册

- URL：`POST /api/auth/register`
- 描述：用户注册
- 请求体：
```json
{
  "username": "用户名",
  "email": "邮箱地址",
  "password": "密码",
  "nickname": "昵称"
}
```
- 响应：
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱地址",
    "nickname": "昵称",
    "avatar": "头像URL",
    "createdAt": "2023-01-01T10:00:00Z",
    "updatedAt": "2023-01-01T10:00:00Z"
  }
}
```

### 2. 用户登录

- URL：`POST /api/auth/login`
- 描述：用户登录
- 请求体：
```json
{
  "username": "用户名",
  "password": "密码"
}
```
- 响应：
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "JWT令牌",
    "id": 1,
    "username": "用户名",
    "email": "邮箱地址",
    "nickname": "昵称"
  }
}
```

### 3. 获取当前用户信息

- URL：`GET /api/auth/me`
- 描述：获取当前登录用户信息
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱地址",
    "nickname": "昵称",
    "avatar": "头像URL",
    "createdAt": "2023-01-01T10:00:00Z",
    "updatedAt": "2023-01-01T10:00:00Z"
  }
}
```

### 4. 注销

- URL：`POST /api/auth/logout`
- 描述：用户注销
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "message": "注销成功"
}
```

### 5. 验证JWT令牌

- URL：`GET /api/auth/validate`
- 描述：验证JWT令牌有效性
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱地址",
    "nickname": "昵称",
    "avatar": "头像URL",
    "createdAt": "2023-01-01T10:00:00Z",
    "updatedAt": "2023-01-01T10:00:00Z"
  }
}
```

## 文章接口

### 1. 创建文章

- URL：`POST /api/articles`
- 描述：创建新文章
- 需要认证：是
- 请求体：
```json
{
  "title": "文章标题",
  "summary": "文章摘要",
  "content": "文章内容",
  "categoryId": 1,
  "tags": [{"id": 1}, {"id": 2}],
  "coverImage": "封面图片URL",
  "status": 1
}
```
- 响应：
```json
{
  "success": true,
  "message": "文章创建成功",
  "data": {
    "id": 1,
    "title": "文章标题",
    "summary": "文章摘要",
    "content": "文章内容",
    "author": {
      "id": 1,
      "username": "作者名",
      "nickname": "作者昵称"
    },
    "category": {
      "id": 1,
      "name": "分类名"
    },
    "tags": [
      {
        "id": 1,
        "name": "标签1"
      }
    ],
    "createdAt": "2023-01-01T10:00:00Z",
    "updatedAt": "2023-01-01T10:00:00Z",
    "viewCount": 0,
    "likeCount": 0,
    "status": 1
  }
}
```

### 2. 获取文章详情

- URL：`GET /api/articles/{id}`
- 描述：获取指定ID的文章详情
- 参数：id - 文章ID
- 响应：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "文章标题",
    "summary": "文章摘要",
    "content": "文章内容",
    "author": {
      "id": 1,
      "username": "作者名",
      "nickname": "作者昵称"
    },
    "category": {
      "id": 1,
      "name": "分类名"
    },
    "tags": [
      {
        "id": 1,
        "name": "标签1"
      }
    ],
    "createdAt": "2023-01-01T10:00:00Z",
    "updatedAt": "2023-01-01T10:00:00Z",
    "viewCount": 1,
    "likeCount": 0,
    "status": 1
  }
}
```

### 3. 更新文章

- URL：`PUT /api/articles/{id}`
- 描述：更新指定ID的文章
- 参数：id - 文章ID
- 需要认证：是
- 请求体：
```json
{
  "title": "新标题",
  "summary": "新摘要",
  "content": "新内容",
  "categoryId": 1,
  "tags": [{"id": 1}, {"id": 2}]
}
```
- 响应：
```json
{
  "success": true,
  "message": "文章更新成功",
  "data": {
    "id": 1,
    "title": "新标题",
    "summary": "新摘要",
    "content": "新内容",
    "author": {
      "id": 1,
      "username": "作者名",
      "nickname": "作者昵称"
    },
    "category": {
      "id": 1,
      "name": "分类名"
    },
    "tags": [
      {
        "id": 1,
        "name": "标签1"
      }
    ],
    "createdAt": "2023-01-01T10:00:00Z",
    "updatedAt": "2023-01-01T10:00:00Z",
    "viewCount": 0,
    "likeCount": 0,
    "status": 1
  }
}
```

### 4. 删除文章

- URL：`DELETE /api/articles/{id}`
- 描述：删除指定ID的文章
- 参数：id - 文章ID
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "message": "文章删除成功"
}
```

### 5. 获取所有文章

- URL：`GET /api/articles`
- 描述：获取所有文章列表
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "文章标题",
      "summary": "文章摘要",
      "author": {
        "id": 1,
        "username": "作者名",
        "nickname": "作者昵称"
      },
      "category": {
        "id": 1,
        "name": "分类名"
      },
      "tags": [
        {
          "id": 1,
          "name": "标签1"
        }
      ],
      "createdAt": "2023-01-01T10:00:00Z",
      "viewCount": 1,
      "likeCount": 0,
      "status": 1
    }
  ]
}
```

### 6. 获取已发布文章

- URL：`GET /api/articles/published`
- 描述：获取所有已发布文章
- 响应：同上

### 7. 获取最新文章

- URL：`GET /api/articles/latest`
- 描述：获取最新文章
- 响应：文章列表

## 分类接口

### 1. 创建分类

- URL：`POST /api/categories`
- 描述：创建新分类
- 需要认证：是
- 请求体：
```json
{
  "name": "分类名称",
  "slug": "分类别名",
  "description": "分类描述",
  "orderNum": 1
}
```
- 响应：
```json
{
  "success": true,
  "message": "分类创建成功",
  "data": {
    "id": 1,
    "name": "分类名称",
    "slug": "分类别名",
    "description": "分类描述",
    "articleCount": 0,
    "orderNum": 1,
    "createdAt": "2023-01-01T10:00:00Z"
  }
}
```

### 2. 获取分类列表

- URL：`GET /api/categories`
- 描述：获取所有分类
- 响应：
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "分类名称",
      "slug": "分类别名",
      "description": "分类描述",
      "articleCount": 5,
      "orderNum": 1,
      "createdAt": "2023-01-01T10:00:00Z"
    }
  ]
}
```

### 3. 获取分类详情

- URL：`GET /api/categories/{id}`
- 描述：获取指定分类详情
- 参数：id - 分类ID
- 响应：
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "分类名称",
    "slug": "分类别名",
    "description": "分类描述",
    "articleCount": 5,
    "orderNum": 1,
    "createdAt": "2023-01-01T10:00:00Z"
  }
}
```

### 4. 更新分类

- URL：`PUT /api/categories/{id}`
- 描述：更新分类信息
- 参数：id - 分类ID
- 需要认证：是
- 请求体：同创建分类
- 响应：
```json
{
  "success": true,
  "message": "分类更新成功",
  "data": {
    "id": 1,
    "name": "分类名称",
    "slug": "分类别名",
    "description": "分类描述",
    "articleCount": 5,
    "orderNum": 1,
    "createdAt": "2023-01-01T10:00:00Z"
  }
}
```

### 5. 删除分类

- URL：`DELETE /api/categories/{id}`
- 描述：删除分类
- 参数：id - 分类ID
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "message": "分类删除成功"
}
```

## 标签接口

### 1. 创建标签

- URL：`POST /api/tags`
- 描述：创建新标签
- 需要认证：是
- 请求体：
```json
{
  "name": "标签名称",
  "slug": "标签别名",
  "color": "#FF0000"
}
```
- 响应：
```json
{
  "success": true,
  "message": "标签创建成功",
  "data": {
    "id": 1,
    "name": "标签名称",
    "slug": "标签别名",
    "color": "#FF0000",
    "articleCount": 0,
    "createdAt": "2023-01-01T10:00:00Z"
  }
}
```

### 2. 获取标签列表

- URL：`GET /api/tags`
- 描述：获取所有标签
- 响应：
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "标签名称",
      "slug": "标签别名",
      "color": "#FF0000",
      "articleCount": 3,
      "createdAt": "2023-01-01T10:00:00Z"
    }
  ]
}
```

### 3. 更新标签

- URL：`PUT /api/tags/{id}`
- 描述：更新标签信息
- 参数：id - 标签ID
- 需要认证：是
- 请求体：同创建标签
- 响应：
```json
{
  "success": true,
  "message": "标签更新成功",
  "data": {
    "id": 1,
    "name": "标签名称",
    "slug": "标签别名",
    "color": "#FF0000",
    "articleCount": 3,
    "createdAt": "2023-01-01T10:00:00Z"
  }
}
```

### 4. 删除标签

- URL：`DELETE /api/tags/{id}`
- 描述：删除标签
- 参数：id - 标签ID
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "message": "标签删除成功"
}
```

## 评论接口

### 1. 创建评论

- URL：`POST /api/comments`
- 描述：创建新评论
- 需要认证：是
- 请求体：
```json
{
  "articleId": 1,
  "content": "评论内容",
  "parentId": null
}
```
- 响应：
```json
{
  "id": 1,
  "content": "评论内容",
  "article": {
    "id": 1,
    "title": "文章标题"
  },
  "user": {
    "id": 1,
    "username": "用户名",
    "nickname": "昵称"
  },
  "parent": null,
  "depth": 0,
  "status": 1,
  "createdAt": "2023-01-01T10:00:00Z"
}
```

### 2. 获取文章评论

- URL：`GET /api/comments/article/{articleId}`
- 描述：获取指定文章的所有评论
- 参数：
  - articleId - 文章ID
  - status - 评论状态（默认为1，表示已审核）
- 响应：
```json
[
  {
    "id": 1,
    "content": "评论内容",
    "user": {
      "id": 1,
      "username": "用户名",
      "nickname": "昵称"
    },
    "parent": null,
    "depth": 0,
    "status": 1,
    "createdAt": "2023-01-01T10:00:00Z"
  }
]
```

### 3. 分页获取文章评论

- URL：`GET /api/comments/article/{articleId}/page`
- 描述：分页获取指定文章的评论
- 参数：
  - articleId - 文章ID
  - page - 页码（默认为0）
  - size - 每页数量（默认为10）
  - status - 评论状态（默认为1，表示已审核）
- 响应：
```json
{
  "content": [...], // 评论列表
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

### 4. 更新评论

- URL：`PUT /api/comments/{commentId}`
- 描述：更新评论
- 参数：commentId - 评论ID
- 需要认证：是
- 请求体：
```json
{
  "content": "更新后的评论内容"
}
```
- 响应：同创建评论

### 5. 删除评论

- URL：`DELETE /api/comments/{commentId}`
- 描述：删除评论
- 参数：commentId - 评论ID
- 需要认证：是
- 响应：
```json
{
  "success": true
}
```

### 6. 获取用户评论列表

- URL：`GET /api/comments/user/{userId}`
- 描述：获取指定用户的所有评论
- 参数：
  - userId - 用户ID
  - status - 评论状态（默认为1，表示已审核）
- 响应：
```json
[
  {
    "id": 1,
    "content": "评论内容",
    "article": {
      "id": 1,
      "title": "文章标题"
    },
    "user": {
      "id": 1,
      "username": "用户名",
      "nickname": "昵称"
    },
    "parent": null,
    "depth": 0,
    "status": 1,
    "createdAt": "2023-01-01T10:00:00Z"
  }
]
```

### 7. 统计文章评论数量

- URL：`GET /api/comments/count/article/{articleId}`
- 描述：统计文章评论数量
- 参数：
  - articleId - 文章ID
  - status - 评论状态（默认为1，表示已审核）
- 响应：
```json
{
  "success": true,
  "data": 5
}
```

## 文件上传接口

### 1. 上传文件

- URL：`POST /api/files/upload`
- 描述：上传文件
- 需要认证：是
- 请求体：multipart/form-data格式
- 响应：
```json
{
  "success": true,
  "message": "文件上传成功",
  "data": {
    "id": 1,
    "fileName": "文件名",
    "originalName": "原文件名",
    "contentType": "文件类型",
    "size": 12345,
    "filePath": "文件路径",
    "url": "文件URL",
    "createdAt": "2023-01-01T10:00:00Z"
  }
}
```

## Redis测试接口

### 1. Redis连接信息

- URL：`GET /api/test/redis/connection-info`
- 描述：获取Redis连接信息
- 响应：
```json
{
  "host": "localhost",
  "port": 6451,
  "status": "configured",
  "ping_result": "PONG",
  "success": true,
  "message": "Redis连接信息获取成功"
}
```

### 2. Redis健康检查

- URL：`GET /api/test/redis/health`
- 描述：检查Redis健康状态
- 响应：
```json
{
  "status": "UP",
  "ping_result": "PONG",
  "message": "Redis正常"
}
```

### 3. Redis详细操作测试

- URL：`GET /api/test/redis/detailed`
- 描述：测试Redis各项操作
- 响应：
```json
{
  "success": true,
  "message": "所有Redis操作测试通过",
  "details": {
    "set_operation": "success",
    "get_operation": "success",
    "del_operation": "success",
    "ttl_operation": "success (59s)"
  }
}
```

## 用户接口

### 1. 获取用户列表

- URL：`GET /api/users`
- 描述：获取所有用户列表
- 需要认证：是
- 响应：
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "用户名",
      "email": "邮箱地址",
      "nickname": "昵称",
      "avatar": "头像URL",
      "createdAt": "2023-01-01T10:00:00Z",
      "updatedAt": "2023-01-01T10:00:00Z"
    }
  ]
}
```

## 错误码

- 200: 成功
- 400: 请求参数错误
- 401: 未认证
- 403: 权限不足
- 404: 资源不存在
- 500: 服务器内部错误

## 部署说明

### 后端部署

1. 确保已安装JDK 21和Maven 3.6+
2. 配置数据库连接信息在`application.properties`
3. 构建项目：`mvn clean package`
4. 启动服务：`java -jar target/my-blog-0.0.1-SNAPSHOT.jar`

### 前端部署

1. 确保已安装Node.js 16+
2. 安装依赖：`npm install`
3. 构建项目：`npm run build`
4. 部署到Web服务器（如Nginx）

### Docker部署

使用提供的`docker-compose.yml`文件可以一键部署整个应用：
```bash
docker-compose up -d
```

## 安全说明

1. 所有敏感操作都需要JWT认证
2. 密码使用BCrypt加密存储
3. 重要接口有CSRF防护
4. 输入参数经过验证和过滤
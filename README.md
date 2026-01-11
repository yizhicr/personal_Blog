# Blog项目

这是一个基于Spring Boot和Vue.js的全栈博客系统，支持文章管理、用户认证、分类与标签管理以及评论系统等功能。

## 项目结构

- **Backend**: 基于Spring Boot的后端服务，包含完整的业务逻辑和数据持久化
- **Frontend**: 基于Vue.js的前端界面，提供用户交互界面

## 功能特性

### 后端功能
- 用户注册与登录（JWT认证）
- 文章的增删改查（支持标题、内容、摘要、封面图片、分类、标签等）
- 分类和标签管理（支持创建、更新、删除分类和标签）
- 评论系统（支持对文章的评论和回复功能，带状态管理）
- 文件上传（支持多文件上传管理）
- 基于Spring Security的安全控制
- 使用JPA进行数据持久化
- 支持文章搜索和高级搜索功能

### 前端功能
- 用户认证界面（登录/注册）
- 博客文章展示与详情页
- 文章搜索功能
- 用户个人中心
- 评论功能（发表评论和回复）

## 技术栈

### 后端技术
- **框架**: Spring Boot 4.0.1
- **数据库**: MySQL
- **安全**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **其他工具**: Lombok, Apache POI

### 前端技术
- **框架**: Vue.js 3
- **路由**: Vue Router
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **类型检查**: TypeScript
- **UI组件库**: Element Plus

## API接口

### 后端API
本项目提供了全面的RESTful API接口，包括：

- **认证接口**: 用户注册、登录、验证JWT令牌
- **文章接口**: 创建、获取列表、查看详情、更新、删除、搜索等
- **评论接口**: 发布评论、获取文章评论、更新、删除等
- **分类接口**: 获取所有分类、创建、更新、删除分类
- **标签接口**: 获取所有标签、创建、更新、删除标签
- **文件接口**: 文件上传、获取列表、删除文件
- **用户接口**: 获取用户信息、更新用户资料、修改密码等

更多后端API详情请参见[Backend/API.md](./Backend/API.md)。

### 前端API
前端通过Axios与后端API进行交互，包含完整的请求和响应拦截器：

- **请求拦截器**: 自动添加JWT Token到请求头
- **响应拦截器**: 统一处理错误状态码和响应格式
- **错误处理**: 对常见错误状态码(400, 401, 403, 404, 500)进行统一处理

更多前端API使用详情请参见[Frontend/API.md](./Frontend/API.md)。

## 快速开始

### 环境要求

#### 后端
- JDK 21
- Maven 3.6+
- MySQL 8.0+

#### 前端
- Node.js 16+
- npm 或 yarn

### 启动步骤

1. 克隆项目到本地
```bash
git clone <your-repo-url>
```

2. 配置后端：
   - 在 `Backend/src/main/resources/application.properties` 中设置数据库连接参数
   - 启动MySQL服务并创建对应数据库
   - 进入Backend目录，使用Maven构建项目：
     ```bash
     cd Backend
     mvn clean install
     ```
   - 启动后端服务：
     ```bash
     mvn spring-boot:run
     ```

3. 启动前端：
   - 进入Frontend目录，安装依赖：
     ```bash
     cd Frontend
     npm install
     ```
   - 启动前端开发服务器：
     ```bash
     npm run dev
     ```

## 项目特点

- 前后端分离架构，便于独立开发和部署
- 完整的用户认证和权限管理系统
- 支持评论的多层次回复功能
- 提供丰富的文章管理功能
- 文件上传和管理功能
- 统一的错误处理和响应格式
- 完善的API文档支持

## 许可证

本项目采用 MIT 许可证。
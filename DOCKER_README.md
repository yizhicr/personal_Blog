# Blog Application Containerization

这是一个全栈博客系统的容器化部署方案，包含前端Vue.js应用和后端Spring Boot应用。

## 项目结构

- `Backend`: Spring Boot后端，提供REST API
- `Frontend`: Vue.js前端，用户界面
- `MySQL`: 数据库服务
- `Redis`: 缓存服务

## 部署方式

### 使用Docker Compose（推荐）

1. 克隆项目到本地：
   ```bash
   git clone <repository-url>
   cd Blog
   ```

2. 启动服务：
   ```bash
   docker-compose up -d
   ```

3. 访问应用：
   - 前端: [http://localhost](http://localhost)
   - 后端API: [http://localhost:8080](http://localhost:8080)

### 单独构建镜像

#### 构建后端镜像
```bash
cd Backend
docker build -t blog-backend .
```

#### 构建前端镜像
```bash
cd Frontend
docker build -t blog-frontend .
```

## 环境变量

### 后端环境变量
- `SPRING_DATASOURCE_URL`: 数据库连接URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `SPRING_REDIS_HOST`: Redis主机地址
- `SPRING_REDIS_PORT`: Redis端口
- `JWT_SECRET`: JWT密钥
- `JWT_EXPIRATION`: JWT过期时间

### 前端环境变量
- `NODE_ENV`: Node环境（production/development）

## 数据持久化

- MySQL数据存储在名为`mysql_data`的卷中
- Redis数据存储在名为`redis_data`的卷中
- 上传文件存储在名为`uploads`的卷中

## 安全注意事项

1. 生产环境中请务必修改默认密码
2. 设置安全的JWT密钥
3. 对外暴露的端口应通过防火墙限制访问
4. 定期备份数据卷

## 监控和日志

- 应用程序日志输出到容器标准输出
- 可通过 `docker logs <container-name>` 查看日志
- 后端应用包含健康检查端点 `/actuator/health`

## 扩展性

当前架构支持水平扩展：
- 前端可通过CDN分发提高访问速度
- 后端可通过增加实例并配合负载均衡器扩展
- 数据库可考虑主从复制提升读取性能
- Redis可配置集群模式支持更大规模缓存
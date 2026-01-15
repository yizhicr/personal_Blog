FROM openjdk:21-jdk-slim

LABEL maintainer="Blog Application Maintainers"

# 设置工作目录
WORKDIR /app

# 复制依赖文件
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖
RUN ./mvnw dependency:go-offline

# 复制源代码
COPY src src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 定义健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "target/my-blog-0.0.1-SNAPSHOT.jar"]
FROM node:18-alpine AS frontend-build

# 单镜像部署：前端由 Spring Boot 同源托管，API 走相对路径 /api（无需再声明 VITE_ALGORITHM_BASE_URL，
# 算法请求已改为后端代理）。如需自定义前端 API 前缀可传入 VITE_API_BASE_URL（默认 /api）。
ARG VITE_API_BASE_URL=/api

WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend-build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

# Copy frontend dist into Spring Boot static resources
COPY --from=frontend-build /app/dist ./src/main/resources/static

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=backend-build /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

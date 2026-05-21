# 前端Dockerfile - Vue3 + Vite

FROM node:18-alpine AS build

ARG VITE_API_BASE_URL
ARG VITE_ALGORITHM_BASE_URL

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm ci --no-optional

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 运行阶段 - 使用Nginx
FROM nginx:alpine

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制静态文件
COPY --from=build /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]

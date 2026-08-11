# 课堂智能考勤系统 · 本地运行指南

> 本项目已实现「可运行落地」。下面三种方式任选，从最快看到效果到真实全栈。

## 方式一：演示模式（最快，无需 JDK / MySQL / Maven）
适合快速看效果、面试演示。后端用零依赖 Node 模拟服务，返回内存示例数据。

```bash
# 1) 启动演示后端（监听 8080）
cd classroom-attendance-master
node mock-server/server.js

# 2) 另开终端，启动前端（监听 5173）
cd classroom-attendance-master/frontend
npm install        # 首次需要
npm run dev

# 3) 浏览器打开 http://localhost:5173
```

演示账号（密码任意即可）：
- `admin` → 管理员，进入后台仪表盘
- `student` → 学生，进入学生端首页

登录后可看到：仪表盘统计、考勤列表、人脸复核（F9）、课堂行为检测、课程/公告/文件等模块。
登录使用 `httpOnly Cookie` 下发 JWT（与真实后端安全设计完全一致）；算法检测走 `/api/algorithm/*` 由演示后端直接返回 mock 结果，不依赖 Python 算法服务。

> 说明：演示后端仅用于"看到成果"，数据是内存示例。真实业务逻辑请见方式二/三。

## 方式二：真实全栈（Docker Compose，推荐完整演示）
需本机已安装 Docker + Docker Compose。

```bash
cd classroom-attendance-master/docker
cp .env.example .env
# 编辑 .env，务必把 JWT_SECRET 改成 ≥32 字节的随机值（openssl rand -hex 32）

docker compose up -d
```

启动后：
- 后端 `http://localhost:8080`
- 算法服务 `http://localhost:5000`（首次启动会从 GitHub 下载 YOLOv8 / 人脸识别权重，需联网；权重未就绪时 `/health` 返回 503，属正常）
- MySQL `3306`
- 前端经 nginx 暴露（见 docker-compose 端口映射）

前端开发预览仍可用方式一的 `npm run dev` 指向 8080。

## 方式三：真实后端本地编译运行（开发者）
需 JDK 17 + Maven + MySQL。

```bash
# 1) 建库并执行初始化 SQL
mysql -u root -p
CREATE DATABASE classroom_attendance DEFAULT CHARSET utf8mb4;
# 执行 src/main/resources/sql/*.sql

# 2) 配置 src/main/resources/application.yml
#    datasource / JWT_SECRET(≥32B) / algorithm.base-url / cors.allowed-origins

# 3) 编译运行
cd classroom-attendance-master
mvn clean package -DskipTests
java -jar target/*.jar
```

## 安全设计要点（已实现并 push）
- 登录 JWT 存 `httpOnly` Cookie，前端 JS 读不到 token，防 XSS 窃取。
- 算法服务地址/密钥仅在后端，`AlgorithmProxyController` 代理前端请求，密钥不暴露浏览器。
- 已清理 git 历史中的明文 JWT 密钥与 `.env`；生产环境**务必轮换 `JWT_SECRET`**。
- `docker-compose.yml` 已补 `JWT_SECRET / JWT_COOKIE_SECURE / CORS_ALLOWED_ORIGINS / ALGORITHM_BASE_URL` 等安全变量。

## 默认演示账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 任意 |
| 学生 | student | 任意 |

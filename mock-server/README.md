# 演示后端（mock-server）

零依赖的 Node `http` 演示后端，与前端 REST 契约保持一致，用于**本地一键演示全栈交互**。

> 真实后端是 Spring Boot（已 push 至 GitHub `Alan-ke007/classroom-attendance`）。
> 本文件仅用于**沙箱 / 无 JDK + MySQL 环境**下的演示落地，不接入任何真实数据库或算法模型。

## 快速启动

```bash
# 1) 启动演示后端（默认 8080）
node mock-server/server.js
# 或自定义端口：
MOCK_PORT=9090 node mock-server/server.js

# 2) 另一个终端启动前端（Vite，默认 5173，自动反向代理 /api、/ws 到 8080）
cd frontend
npm install   # 首次需要
npm run dev
```

打开 `http://localhost:5173` 即可体验完整界面：
- 管理端账号： `admin` / 任意密码（演示不校验）
- 学生端账号： 用户名含 `student` 即走学生角色（如 `student`）

## 特性

- **零依赖**：仅用 Node 内置 `http` / `url`，无需 `npm install`。
- **httpOnly Cookie 鉴权演示**：登录下发 `Set-Cookie: token=...; HttpOnly; SameSite=Lax`，与真实后端安全策略一致。
- **CORS 已开放凭据**：`Access-Control-Allow-Credentials: true`，配合前端跨域代理。
- **端口占用自检**：端口被占用时打印可执行的排查命令后退出（不再静默失败）。
- **兜底路由**：未命中已知路由时返回空成功结构，避免前端页面因缺失接口报错白屏。

## 已实现接口（与前端契约对齐）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，下发 httpOnly Cookie |
| GET | `/api/auth/info` | 当前用户信息（读 Cookie） |
| POST | `/api/auth/logout` | 注销，清空 Cookie |
| GET | `/api/captcha/generate` | 演示验证码（固定 `8888`） |
| GET | `/api/attendance/list` | 考勤列表 |
| GET | `/api/attendance/face-review` | 人脸待复核列表 |
| GET | `/api/statistics/*` | 看板 / 待办 / 排行 / 课表 等统计 |
| GET | `/api/course/list` | 课程列表 |
| GET | `/api/behavior/list` | 行为检测列表 |
| GET | `/api/announcement/active` | 活跃公告 |
| GET | `/api/file/list` | 文件列表 |
| GET | `/api/algorithm/health` | 算法服务健康（mock） |
| POST | `/api/algorithm/detect` | 行为检测（mock 随机结果） |

> 形如 `/api/attendance/student/{id}`、`/api/statistics/student/{id}` 等带路径参数的请求，由 `prefixHandlers` 前缀路由兜底返回演示数据。

## 注意

- 演示数据在进程内存中生成，**重启即重置**，不做持久化。
- 演示后端**不实现 WebSocket**（`/ws` 升级连接会被直接关闭），避免前端挂起重连打印噪声；实时推送相关功能需在真实 Spring Boot 后端体验。
- 切勿将本文件用于生产环境——它不校验密码、不鉴权粒度、不含真实人脸/行为模型。

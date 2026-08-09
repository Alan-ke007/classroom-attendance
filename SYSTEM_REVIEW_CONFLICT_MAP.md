# 智课考勤 · 系统级 Review 与冲突地图（重写输入）

> **基准快照**：`/Users/a./Desktop/classroom-attendance-master`，mtime `2026-08-07`（你确认非最新版）。
> **目的**：为重做系统提供「问题清单 + 保留项」，作为需求分析（下一步）的输入。结论聚焦**模式/架构级问题**，不绑具体行号，可移植到最新版。
> **前端细项**见同目录 `UI_MODIFICATION_ANALYSIS.md`，本文聚焦系统级 + 设计/页面冲突汇总 + 重写取舍。

---

## 1. 系统架构总览（现状）

| 层 | 技术 | 规模 |
|----|------|------|
| 后端 | Spring Boot 3.2 / JDK17 / MyBatis-Plus / MySQL8 / JWT / WebSocket / POI | 19 个 Controller，按功能模块化 `modules/*`（controller/service/mapper/entity 齐全） |
| 算法服务 | Flask + YOLOv8（ultralytics），OpenCV | 独立进程，前端多处直连 |
| **前端 A** | Web 管理/教师端（Vue3 + Element Plus） | Dashboard + 管理视图 20+ |
| **前端 B** | Web 学生端（Vue3） | `student/*` 8 页 |
| **前端 C** | 微信小程序（uni-app：pages.json/manifest） | 含 `student/*` + `teacher/*` 两套页面 |
| 数据 | MySQL，10 张核心表 | user/student/class/course/attendance/leave/behavior/notification/chat/file |

**一句话**：1 后端 + 1 算法服务 + **3 套前端客户端**，共用同一套 API。

---

## 2. 冲突点与问题清单

### A. 设计语言冲突（已在 `UI_MODIFICATION_ANALYSIS.md` 详述）
- **三份 Token 事实来源互相矛盾**：`DESIGN.md`(Apple `#007AFF`+Mint/Sky) / `PRODUCT.md`(Apple 风/克制) / `App.vue`(cyan/magenta/yellow 霓虹+硬黑影) / `style.css`(Outfit/Work Sans)。维护者无唯一准绳。
- **双轨方向已定却未落地**：外壳(Dashboard)粗野、内容(Home)玻璃、统计(Statistics)霓虹——分配是各页面随手选的，无显式规则。
- **全局 `!important` 强覆 bug**：`Login.vue` 作者的玻璃圆角按钮被 `App.vue` `.el-button--primary` 的 `!important` 静默覆盖成粗野风。
- **品牌调性严重背离**：`PRODUCT.md` 写"类似 Apple / 深色优先 / 玻璃拟态 / 克制动效 / 不要厚重阴影"，实际实现是粗野终端 + 霓虹硬影。

### B. 页面层冲突
- 同一后台内硬边(外壳)与毛玻璃(内容)混用，认知割裂。
- `Statistics.vue` 数据页使用 `sci-fi-card` 霓虹发光边（showpiece 处理泄漏进后台）。
- 可访问性缺口：`:focus-visible` 焦点环被 `!important` 吃掉、深色底黄字对比度可能 < 4.5:1、列表按钮 < 44px。

### C. 系统级 / 范围冲突（**重做关键**）
1. **三前端冗余（最严重）**：教师功能同时存在于 Web(Dashboard) 与 小程序(`teacher/*`)；学生功能同时存在于 Web(`student/*`) 与 小程序(`student/*`)。同一后端被 3 套 UI 重复实现 → 维护成本高、三端体验不一致、改一处要改三处。
2. **小程序定位不清**：小程序内竟含 `teacher/home`、`teacher/attendance`、`teacher/behavior` 等教师页，说明它到底是"学生专用"还是"师生通用"从未定调。
3. **功能漂移**：后端有 `CaptchaController`（验证码），但 Web 登录页无验证码字段——后端能力前端未用/前后不一致。
4. **文档散落且矛盾**：设计文档在根目录(`DESIGN.md`/`PRODUCT.md`)，系统文档在 `docs/README.md`，且描述互相打架。
5. **命名不一致**：文件夹 `classroom-attendance-master` vs 文档内 `classroom-attendance/`（`-master` 疑似 git 分支快照，印证"非最新版"）。
6. **算法服务耦合**：Flask 独立服务，前端多处直连其地址；重做时需决定是否保留接口契约或收编。

---

## 3. 重做取舍：保留 vs 避免（输入给需求分析）

### ✅ 值得保留
- **后端模块化结构**（`modules/*` 按功能清晰分层）—— 可复用其 API 契约，重写前端时不用重造后端。
- **基础设施**：统一响应/异常封装、JWT 过滤、WebSocket（聊天/预警推送）、POI 导入导出。
- **数据模型**：10 张表成熟，可沿用。
- **YOLOv8 行为检测**：差异化卖点（若产品仍要 AI 行为分析）。
- **双轨设计方向决策本身合理**：后台玻璃拟态 + 大屏粗野科幻，只是没落地。

### ❌ 必须改 / 避免
- 消除三份 Token 矛盾 → 单一 Design Token 源 + 双轨变量组。
- 消除全局 `!important` 强覆 → 按轨道作用域（`.track-glass` / `.track-brutal`）。
- **解决三前端冗余** → 明确每端定位，合并或砍（见需求分析 Q2）。
- 文档合一且与实现同步（单一 `DESIGN.md` 即事实来源）。
- 补齐可访问性（WCAG AA：焦点环、4.5:1 对比度、≥44px 触控）。

---

## 4. 待需求分析确认的关键决策（下一步）
1. **重做范围**：全栈 / 仅前端三端 / 仅 UI 层（业务逻辑不变）？
2. **三端策略**：维持三套 / 合并学生端 / 砍小程序只留响应式 Web / 砍 Web 学生只留小程序？
3. **设计方向**：确认双轨 / 换全新方向 / 沿用现有粗野霓虹？
4. **算法去留**：保留 YOLOv8 / 砍掉只做基础考勤 / 保留但弱化？
5. **目标用户·设备·部署**：主要使用场景（教室大屏？管理员办公？学生手机？）、是否要响应式/小程序？

> 下一步将据此逐条与你对齐，形成《新系统需求规格 + 设计系统蓝图》。

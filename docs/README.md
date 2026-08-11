# 课堂智能考勤系统 - 文档中心

> 说明：本仓库早期生成的文档索引曾引用大量不存在的文件，已重写为与当前结构一致的真实索引。

## 文档导航

### 入口 / 运行（仓库根目录）
- [README.md](../README.md) — 项目介绍与总览
- [QUICK_START.md](../QUICK_START.md) — 快速开始
- [START_DEMO.md](../START_DEMO.md) — 演示模式 / Docker 全栈 / 本地编译 三种运行方式
- [docker/.env.example](../docker/.env.example) — 一键启动（docker-compose）环境变量模板
- [backend.env.example](../backend.env.example) — 独立运行后端的环境变量模板
- [frontend/.env.example](../frontend/.env.example) — 前端环境变量模板

### 设计 / 分析过程稿（已归档）
归档于 [`docs/design/`](design/)，为项目演进过程中的设计、需求、评审记录：
- `DESIGN.md` / `DESIGN_SYSTEM_SPEC_A.md` / `DESIGN_SYSTEM_PREVIEW_A.html` — 设计系统规范与预览
- `UI_MODIFICATION_ANALYSIS.md` / `UI_DUALTRACK_DEMO.html` — 双轨 UI 改造分析与预览
- `PRODUCT.md` / `PROJECT_INTRO.md` / `SYSTEM_REVIEW_CONFLICT_MAP.md` — 产品定义与系统评审
- `FUNCTIONAL_ANALYSIS_SCENARIO.md` / `NEW_SYSTEM_REQUIREMENTS_BLUEPRINT.md` — 功能分析与需求蓝图
- `CRITICAL_GAP_SPECS.md` — 关键缺口规格

### 部署
- `docker/docker-compose.yml` — 全栈编排（backend + mysql + algorithm + frontend）
- `Dockerfile` — Railway 单镜像部署（前端由 Spring Boot 同源托管）
- `railway.json` — Railway 部署配置

## 代码位置速查
- 后端：`src/main/java/com/classroom/attendance/`
- 前端：`frontend/src/`
- 小程序：`miniapp/`
- 算法服务：`algorithm-service/`
- 演示后端（本地可交互）：`mock-server/server.js`

---
**最后更新**：2026-08-11

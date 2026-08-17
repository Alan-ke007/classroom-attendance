# 课堂智能考勤系统 · Classroom Smart Attendance

> An AI-powered classroom attendance & behavior-analysis system built on **YOLOv8** + **Spring Boot** + **Vue3** + **uni-app**.
> 基于 YOLOv8 与 Spring Boot 的校园课堂智能考勤与行为分析系统。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Model: YOLOv8](https://img.shields.io/badge/Model-YOLOv8-orange.svg)](https://github.com/ultralytics/ultralytics)
[![Backend: Spring Boot 3.2](https://img.shields.io/badge/Backend-Spring%20Boot%203.2-brightgreen.svg)]()

## ✨ 核心亮点

- **🎯 课堂行为识别（核心创新）**：基于 YOLOv8 的 6 分类行为检测（举手 / 阅读 / 书写 / 玩手机 / 低头 / 趴桌），mAP@50 85.9%。
- **📊 课堂专注度指数 ATI**：把行为量化成可写进论文的专注度分数，输出 优 / 良 / 中 / 差 等级。
- **⚡ 双模考勤**：二维码扫码 + 人脸识别两种签到入口。
- **🔔 实时异常预警**：违纪行为通过 WebSocket 实时推送到教师端。
- **🔒 工程化安全**：JWT（httpOnly Cookie）、算法代理鉴权、越权防护、并发原子更新。

## 🎉 项目状态

**✅ 全栈可用**：后端（Spring Boot 3.2 + MyBatis-Plus）、前端管理后台（Vue3）、微信小程序（uni-app）、算法微服务（Flask + YOLOv8）四端闭环。

**🆕 核心能力**:
- ✅ **YOLOv8 行为检测模型**（mAP@50: 85.9%，训练报告值，以 `algorithm-service/evaluate_model.py` 本机复现为准）
- ✅ **智能考勤专注度算法 ATI**（后端 `AttentionService` + 前端实时监控展示）
- ✅ **双模考勤 / 实时预警 / 多维报表** 已落地

## 技术栈

### 后端
- **后端框架**: Spring Boot 3.2
- **JDK 版本**: 17
- **持久层框架**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0+
- **认证**: JWT (jjwt)
- **构建工具**: Maven

### 前端
- **框架**: Vue 3 + Vite
- **UI组件库**: Element Plus
- **HTTP客户端**: Axios
- **图表库**: ECharts
- **路由**: Vue Router 4

### 算法服务
- **框架**: Flask
- **AI模型**: YOLOv8 (ultralytics)
- **图像处理**: OpenCV
- **语言**: Python 3.8+

## 项目结构

```
classroom-attendance/
├── src/                  # Spring Boot 后端（com.classroom.attendance，按 modules 分模块）
│   ├── main/java/com/classroom/attendance/
│   │   ├── config/            # Spring 装配
│   │   ├── infrastructure/    # 跨切面：鉴权注解 / 异常 / 拦截 / 响应封装 / 工具
│   │   ├── security/          # JwtAuthFilter
│   │   └── modules/           # 业务模块（auth / attendance / behavior / face / course / student / leave / notification ...）
│   └── main/resources/        # application.yml / init_db.sql
├── frontend/             # Vue3 + Element Plus 管理后台
├── miniapp/              # uni-app 微信小程序（学生端 / 教师端）
├── algorithm-service/    # Flask + YOLOv8 算法微服务
│   ├── app.py            # 检测 / 识别接口
│   ├── evaluate_model.py # 评估（逐类 Precision/Recall、混淆矩阵）
│   └── train_new_datasets.py   # 训练脚本
├── docs/                 # 设计 / 接口文档
├── docker/               # Docker 编排（docker-compose）
├── scripts/              # 初始化 / 启动脚本
├── pom.xml               # Maven 配置
└── README.md
```

## 快速开始

### 🚀 Docker一键启动（推荐 - 新增）

```powershell
# 1. 配置环境变量
cp docker/.env.example docker/.env

# 2. 启动所有服务（数据库、后端、前端、算法）
cd docker
docker-compose up -d

# 3. 查看服务状态
docker-compose ps
```

访问 http://localhost，使用 admin/admin123 登录

### 📝 传统开发模式

#### 1. 初始化数据库

```powershell
# 方式一：使用PowerShell脚本
.\scripts\init_database.ps1

# 方式二：直接执行SQL
mysql -u root -p < scripts\database\init_database.sql
```

#### 2. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

在 MySQL 中执行以下命令创建数据库和表：

```bash
mysql -u root -p < src/main/resources/init_db.sql
```

或者手动执行 `src/main/resources/init_db.sql` 文件中的 SQL 语句。

#### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/classroom_attendance?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

#### 4. 启动服务

**后端：**

```bash
mvn spring-boot:run
```

**前端：**
```bash
cd frontend
npm run dev
```

**算法服务：**
```bash
cd algorithm-service
python app.py
```

或者使用一键启动脚本：
```powershell
.\scripts\utils\start_all.bat
```

### 5. API 接口

项目启动后，可以通过以下接口访问：

#### 班级管理接口

- **分页查询班级列表**
  ```
  GET /api/class/list?pageNum=1&pageSize=10
  ```

- **查询所有班级**
  ```
  GET /api/class/all
  ```

- **根据ID查询班级**
  ```
  GET /api/class/{id}
  ```

- **添加班级**
  ```
  POST /api/class
  Content-Type: application/json
  
  {
    "className": "计算机科学与技术3班",
    "major": "计算机科学与技术",
    "grade": "2024",
    "teacher": "刘老师",
    "studentCount": 40
  }
  ```

- **更新班级**
  ```
  PUT /api/class/{id}
  Content-Type: application/json
  
  {
    "className": "计算机科学与技术3班",
    "major": "计算机科学与技术",
    "grade": "2024",
    "teacher": "刘老师",
    "studentCount": 45
  }
  ```

- **删除班级**
  ```
  DELETE /api/class/{id}
  ```

### 6. 返回格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 功能模块

### 已实现 ✅
**后端（Spring Boot）**
- ✅ 多角色鉴权（学生 / 教师 / 管理员，JWT httpOnly Cookie）
- ✅ 课程 / 班级 / 师生管理
- ✅ 双模考勤：二维码签到 + 人脸识别签到
- ✅ 课堂行为识别（YOLOv8 六类）+ 行为记录落库
- ✅ **课堂专注度指数 ATI**（`AttentionService`，按学生 / 班级 / 课程统计）
- ✅ 异常行为实时预警（WebSocket）
- ✅ 请假 / 消息 / 公告 / 多维统计报表
- ✅ 统一返回、全局异常、CORS、逻辑删除、自动填充

**前端（Vue3 管理后台）**
- ✅ 登录 / Dashboard / 课程班级 / 考勤 / 行为监控（实时 ATI + 预警）/ 报表

**小程序（uni-app）**
- ✅ 学生端：课表 / 签到 / 行为查询；教师端：班级 / 考勤 / 搜索

**算法服务（Flask + YOLOv8）**
- ✅ 行为检测接口、模型评估（逐类 P/R、混淆矩阵）、训练脚本

### 明确不做 ❌
- ❌ 数据可视化"大屏"：本项目定位为毕业设计，核心创新在算法（YOLOv8 行为识别 + ATI），大屏非重点，故不设计。

## 注意事项

1. 确保 MySQL 服务已启动
2. 数据库用户名和密码需要与实际配置一致
3. 端口 8080 不能被占用
4. JDK 版本必须为 17 或更高

## 开发计划

1. 完成基础信息管理模块（班级、学生）
2. 集成用户权限管理系统
3. 对接 Python Flask + YOLOv8 算法服务
4. 实现智能考勤功能
5. 实现行为识别与预警
6. 开发数据可视化大屏
7. 实现报表导出功能

## 许可证

本项目基于 [MIT License](LICENSE) 开源。

## 🏷️ 推荐 GitHub Topics

在仓库 `About → Edit` 中添加以下标签，可显著提升被检索概率：
`yolov8` · `computer-vision` · `attendance` · `classroom` · `spring-boot` · `vue3` · `uni-app` · `graduation-project` · `flask` · `object-detection`

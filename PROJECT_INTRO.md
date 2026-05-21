# 智课考勤系统 — 课堂智能考勤与行为分析系统

## 项目概述

**智课考勤系统**（Classroom Attendance System）是一套面向校园场景的智能化考勤与课堂行为分析平台。系统融合了传统考勤管理、人脸识别签到、YOLOv8 行为检测、实时消息通知和师生即时通讯等能力，提供从学生签到、课堂监控到数据分析的一站式解决方案。

项目采用前后端分离架构，后端基于 Spring Boot 3 + MyBatis-Plus，前端使用 Vue 3 + Element Plus，算法层基于 Python Flask + YOLOv8，同时配套微信小程序端。

---

## 技术架构

### 后端（Java）
- **框架**: Spring Boot 3.2.0, JDK 17
- **ORM**: MyBatis-Plus 3.5.5（代码生成、分页、逻辑删除、自动填充）
- **数据库**: MySQL 8.0+
- **认证**: JWT（jjwt 0.12.3），通过过滤器拦截 `/api/*` 请求进行 Token 校验
- **实时通信**: WebSocket（聊天、行为预警推送）
- **报表**: Apache POI 5.2.5（Excel 导入导出）
- **构建**: Maven

### 前端（Vue 3）
- **框架**: Vue 3.5.34 + Vite 5.4.21
- **UI**: Element Plus 2.13.7
- **路由**: Vue Router 4
- **HTTP**: Axios（统一拦截、Token 注入）
- **图表**: ECharts
- **构建**: npm

### 算法服务（Python）
- **框架**: Flask
- **模型**: YOLOv8（ultralytics）
  - 行为检测模型：mAP@50 达 85.9%
  - 人脸检测模型
- **图像处理**: OpenCV

### 微信小程序
- 基于 uni-app 构建，支持移动端考勤操作

---

## 功能模块

### 1. 用户认证
- 登录 / 注册 / 忘记密码
- JWT Token 认证，角色区分（管理员 / 教师 / 学生）
- 个人资料编辑与密码修改

### 2. 考勤管理
- **人脸签到**：学生通过摄像头拍照或上传图片，调用算法服务进行人脸识别后完成签到
- **二维码签到**：教师生成签到二维码，学生扫码完成签到
- **考勤记录**：按课程、日期、状态（正常 / 迟到 / 缺勤）多维度查询和统计
- **考勤统计**：以图表展示出勤率、迟到率、缺勤率等关键指标

### 3. 课程与班级管理
- 班级 CRUD（专业、年级、班主任、学生人数）
- 课程 CRUD（课程名、授课教师、上课时间、起止周次）
- 学生信息管理（学号、姓名、班级、专业、入学年份）

### 4. 请假管理
- **学生端**：提交请假申请（事假 / 病假），选择日期范围，填写理由
- **管理端**：审批/驳回待办申请，填写审批意见
- 请假记录自动关联考勤状态

### 5. 行为监测
- 基于 YOLOv8 的课堂行为实时检测（举手、写字、睡觉、玩手机等）
- 异常行为预警，通过 WebSocket 实时推送给教师
- 行为记录查询与统计

### 6. 消息通知
- 按类型分类：考勤通知、行为预警、系统消息、请假审批
- 未读标记、全部已读、单条已读
- 通知列表分页展示

### 7. 即时通讯
- 学生与教师之间的实时聊天（WebSocket）
- 新对话发起（搜索用户）
- 消息发送状态（发送中 / 已发送 / 失败）
- 未读消息计数

### 8. 数据统计与可视化
- 全局看板：出勤率趋势、行为统计、班级排名等
- ECharts 图表展示
- 校园数字孪生大屏

### 9. 系统管理
- 用户管理
- 文件管理（上传、下载）
- 数据导入（Excel）

---

## 项目结构

```
classroom-attendance/
├── src/                                    # Spring Boot 后端
│   └── main/java/com/classroom/attendance/
│       ├── controller/                     # 14 个控制器（REST API）
│       ├── service/                        # 业务逻辑层（含 impl）
│       ├── mapper/                         # MyBatis-Plus 数据访问
│       ├── model/                          # 10 个实体类
│       ├── filter/                         # JWT 认证过滤器
│       ├── websocket/                      # WebSocket 处理器
│       ├── config/                         # 配置类（CORS、MP分页等）
│       ├── common/                         # 统一响应封装
│       ├── utils/                          # 工具类
│       └── dto/vo/                         # 数据传输对象
├── frontend/                               # Vue 3 前端
│   └── src/
│       ├── views/                          # 20+ 页面组件
│       │   ├── Dashboard.vue               # 管理后台主界面
│       │   ├── student/                    # 学生端页面（8个）
│       │   ├── chat/                       # 聊天面板
│       │   └── ...                         # 功能页面
│       ├── router/                         # 路由配置
│       ├── api/                            # API 接口封装
│       └── components/                     # 公共组件
├── algorithm-service/                      # Python 算法服务（Flask + YOLOv8）
├── miniapp/                                # 微信小程序（uni-app）
├── docker/                                 # Docker 编排
└── docs/                                   # 项目文档
```

---

## 数据库设计

系统包含 10 张核心数据表：

| 表名 | 说明 |
|------|------|
| user | 用户表（含角色、人脸特征向量） |
| student | 学生信息表 |
| class_info | 班级表 |
| course | 课程表（含起止周次） |
| attendance | 考勤记录表 |
| leave_request | 请假申请表 |
| behavior_record | 行为记录表 |
| notification | 消息通知表 |
| chat_message | 聊天消息表 |
| file_record | 文件记录表 |

---

## 部署方式

### Docker 一键部署（推荐）
```bash
cd docker
docker-compose up -d
```

### 传统部署
```bash
# 后端
mvn spring-boot:run          # 默认 http://localhost:8080

# 前端
cd frontend && npm run dev   # 默认 http://localhost:5173

# 算法服务
cd algorithm-service && python app.py
```

---

## 适用场景

- 高校课堂考勤管理
- 智慧校园建设
- 课堂行为分析与教学质量评估
- 师生即时通讯与通知推送
- 校园数字化管理转型

---

## 技术亮点

- **AI 驱动的考勤**：基于 YOLOv8 的人脸检测与行为识别，无需专用硬件
- **实时推送**：WebSocket 实现消息、聊天、预警的毫秒级推送
- **全端覆盖**：Web 管理后台 + 学生端 + 微信小程序
- **统一 API**：标准 RESTful 接口，统一返回格式，统一异常处理
- **容器化部署**：Docker Compose 一键启动所有服务

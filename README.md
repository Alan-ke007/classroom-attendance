# 课堂智能考勤系统

基于 YOLOv8 与 Spring Boot 的校园课堂智能考勤与行为分析系统。

## 🎉 项目状态

**✅ 基础框架已完成！** 包含后端、前端、算法服务三部分，登录功能已实现并可运行。

**🆕 最新进展 (2026-05-09)**:
- ✅ **算法模型训练完成** - YOLOv8行为检测模型（mAP@50: 85.9%）
- ✅ **前端API集成完成** - 人脸识别和行为检测功能已接入真实算法服务
- ✅ **测试工具就绪** - 提供专用测试页面和自动化测试脚本

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
├── src/                          # Spring Boot后端
│   ├── main/java/com/classroom/attendance/
│   │   ├── controller/          # 控制器（ClassController, AuthController）
│   │   ├── service/             # 服务层
│   │   ├── mapper/              # 数据访问层
│   │   ├── model/               # 实体类（ClassInfo, User）
│   │   └── utils/               # 工具类（JwtUtil）
│   └── main/resources/
│       ├── application.yml      # 配置文件
│       └── init_db.sql          # 数据库初始化脚本（6张表）
├── frontend/                     # Vue3前端
│   ├── src/
│   │   ├── api/                 # API接口封装
│   │   │   ├── auth.js          # 认证相关API
│   │   │   └── algorithm.js     # 算法服务API（新增）
│   │   ├── views/               # 页面组件
│   │   │   ├── FaceRecognition.vue      # 人脸识别（已集成API）
│   │   │   ├── BehaviorMonitor.vue      # 行为监控（已集成API）
│   │   │   └── AlgorithmTest.vue        # 算法测试（新增）
│   │   ├── router/              # 路由配置
│   │   └── utils/               # 工具函数（axios封装）
│   └── package.json
├── algorithm-service/            # Python算法服务
│   ├── app.py                   # Flask主应用
│   ├── models/                  # 训练好的YOLOv8模型
│   │   ├── behavior_best.pt     # 行为检测模型（新训练）
│   │   └── face_detection.pt    # 人脸检测模型
│   ├── runs/train/              # 训练结果和日志
│   └── requirements.txt         # Python依赖
├── pom.xml                       # Maven配置
├── start_all.bat                # 一键启动脚本
├── test_algorithm_integration.bat  # 算法集成测试脚本（新增）
├── START_GUIDE.md               # 完整启动指南
├── FRONTEND_ALGORITHM_INTEGRATION.md  # 前端API集成指南（新增）
└── INTEGRATION_COMPLETION_REPORT.md   # 集成完成报告（新增）
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
- ✅ 用户认证系统（JWT）
- ✅ 用户登录/注册
- ✅ 班级管理 CRUD
- ✅ MyBatis-Plus 分页查询
- ✅ 统一返回结果封装
- ✅ 全局异常处理
- ✅ CORS跨域配置
- ✅ 逻辑删除
- ✅ 自动填充时间字段
- ✅ Vue3前端登录页面
- ✅ Dashboard主页
- ✅ Python算法服务框架
- ✅ **YOLOv8模型训练** - 行为检测模型（mAP@50: 85.9%）
- ✅ **前端API集成** - 人脸识别和行为检测功能
- ✅ **算法测试工具** - 专用测试页面和自动化脚本

### 待实现 ⏳
- ⏳ 学生信息管理
- ⏳ 课程管理
- ⏳ 考勤记录保存（将识别结果写入数据库）
- ⏳ 学生人脸信息录入与匹配
- ⏳ 实时预警（WebSocket）
- ⏳ 数据可视化（ECharts）
- ⏳ 报表导出（Excel）

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

MIT License

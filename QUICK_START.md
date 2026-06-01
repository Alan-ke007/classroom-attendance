# 快速启动指南

## 🚀 方式一：Docker部署（推荐）

最简单的方式，一键启动所有服务！

### 1. 安装Docker
- 下载并安装 [Docker Desktop](https://www.docker.com/products/docker-desktop)
- 确保Docker正在运行

### 2. 配置环境变量
```powershell
cd docker
Copy-Item .env.example .env
```

### 3. 启动所有服务
```powershell
docker-compose up -d
```

### 4. 访问系统
- 前端: http://localhost
- 后端API: http://localhost:8080/api
- 算法服务: http://localhost:5000

### 5. 登录系统
- 用户名: `admin`
- 密码: `admin123`

### 常用命令
```powershell
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 清理并重新构建
docker-compose down -v
docker-compose up -d --build
```

---

## 💻 方式二：本地开发模式

适合开发者进行代码调试和修改。

### 前置要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
- Python 3.8+
- MySQL 8.0+

### 步骤1：初始化数据库

在 MySQL 中创建数据库并导入初始化脚本：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS classroom_attendance DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p classroom_attendance < src/main/resources/init_db.sql
```

### 步骤2：配置数据库连接

编辑 `src/main/resources/application.yml`，或通过环境变量配置：

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/classroom_attendance?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&useUnicode=true&connectionCollation=utf8mb4_unicode_ci&allowPublicKeyRetrieval=true
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_password
```

### 步骤3：启动后端服务

```bash
cd e:/classroom-attendance
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 步骤4：启动前端服务

打开新终端：

```bash
cd e:/classroom-attendance/frontend
npm install  # 首次需要安装依赖
npm run dev
```

前端将在 http://localhost:5173 启动（已配置 /api 代理到后端 8080）

### 步骤5：启动算法服务（可选）

打开新终端：

```bash
cd e:/classroom-attendance/algorithm-service

# 创建虚拟环境（首次）
python -m venv venv
source venv/Scripts/activate  # Windows Git Bash
# 或 venv\Scripts\Activate.ps1  # Windows PowerShell

# 安装依赖（首次）
pip install -r requirements.txt

# 启动服务
python app.py
```

算法服务将在 http://localhost:5000 启动

### 步骤6：访问系统

浏览器打开: http://localhost:5173
- 用户名: `admin`
- 密码: `admin123`

---

## 🔧 方式三：使用启动脚本

项目根目录提供了便捷脚本：

| 脚本 | 用途 |
|------|------|
| `algorithm-service/run.bat` | 启动算法服务 |
| `CreateTable.java` | 数据库建表辅助类 |

---

## ✅ 验证安装

### 1. 检查后端
```bash
curl http://localhost:8080/api/classmgmt/all
```
应返回 JSON 数据

### 2. 检查前端
浏览器访问 http://localhost:5173，应看到登录页面

### 3. 检查算法服务
```bash
curl http://localhost:5000/health
```
应返回 `{"status": "ok"}`

### 4. 检查数据库
```bash
mysql -u root -p -e "USE classroom_attendance; SHOW TABLES;"
```

---

## 🐛 常见问题

### Q1: Docker启动失败
**解决**: 
- 确保Docker Desktop正在运行
- 检查端口是否被占用（80, 3306, 5000, 8080）
- 查看日志: `docker-compose logs`

### Q2: 前端无法连接后端
**解决**:
- 检查后端是否正常运行
- 确认 `frontend/.env.development` 中的API地址正确
- 检查浏览器控制台是否有CORS错误

### Q3: 算法服务启动失败
**解决**:
- 确保已安装Python依赖: `pip install -r requirements.txt`
- 检查模型文件是否存在: `algorithm-service/models/*.pt`
- 确认5000端口未被占用

### Q4: 数据库连接失败
**解决**:
- 确认MySQL服务已启动
- 检查 `application.yml` 中的用户名和密码
- 确认数据库已创建: `classroom_attendance`

### Q5: 端口被占用
**解决**:
```powershell
# 查看端口占用
netstat -ano | findstr :8080

# 结束进程
taskkill /F /PID <进程ID>
```

---

## 📊 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 (Nginx) | 80 | Docker模式 |
| 前端 (Vite) | 5173 | 开发模式 |
| 后端 (Spring Boot) | 8080 | REST API |
| 算法 (Flask) | 5000 | AI推理服务 |
| 数据库 (MySQL) | 3306 | 数据存储 |

---

## 🎯 下一步

启动成功后，您可以：

1. **浏览系统** - 熟悉各个功能模块
2. **查看文档** - `docs/README.md` 了解详细功能
3. **测试API** - 使用Postman或curl测试接口
4. **开始开发** - 根据需求修改代码
5. **查看日志** - 监控服务运行状态

---

**最后更新**: 2026-05-09  
**版本**: v2.0

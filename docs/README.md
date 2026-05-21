# 课堂智能考勤系统 - 文档中心

## 📚 文档导航

### 🚀 快速开始
- [项目概述](../README.md) - 项目介绍和快速开始
- [环境配置](../ENVIRONMENT_SETUP.md) - 开发环境搭建
- [启动指南](../START_GUIDE.md) - 如何启动项目

### 📐 架构设计
- [系统架构](architecture/SYSTEM_ARCHITECTURE.md) - 整体架构设计
- [数据库设计](architecture/DATABASE_DESIGN.md) - 数据库表结构
- [项目结构优化](PROJECT_STRUCTURE_OPTIMIZATION.md) - 最新的项目结构说明

### 🔌 API文档
- [API参考手册](api/API_REFERENCE.md) - 完整的API接口文档
- [算法服务API](api/ALGORITHM_API.md) - Flask算法服务接口

### 💻 开发指南
- [开发规范](development/CODING_STANDARDS.md) - 编码规范和最佳实践
- [Git工作流](development/GIT_WORKFLOW.md) - 版本控制和协作流程
- [开发环境搭建](development/DEVELOPMENT_GUIDE.md) - IDE配置和调试

### 🚢 部署文档
- [Docker部署](deployment/DOCKER_DEPLOYMENT.md) - 容器化部署指南
- [生产部署](deployment/PRODUCTION_DEPLOYMENT.md) - 生产环境部署
- [运维手册](deployment/OPERATIONS.md) - 日常运维操作

### 📊 其他文档
- [前端算法集成](../FRONTEND_ALGORITHM_INTEGRATION.md) - 前端与AI服务集成
- [集成完成报告](../INTEGRATION_COMPLETION_REPORT.md) - API集成总结
- [优化总结](OPTIMIZATION_SUMMARY.md) - 项目结构优化完成情况

## 🎯 常用链接

### 开发相关
- **后端代码**: `src/main/java/com/classroom/attendance/`
- **前端代码**: `frontend/src/`
- **算法服务**: `algorithm-service/`
- **配置文件**: `src/main/resources/application.yml`

### 脚本工具
- **一键启动**: `../start_all.bat`
- **环境检查**: `../check_env.bat`
- **数据库初始化**: `../init_database.ps1`
- **算法测试**: `../test_algorithm_integration.bat`

### Docker部署
- **编排文件**: `docker/docker-compose.yml`
- **环境变量**: `docker/.env.example`
- **后端镜像**: `docker/backend/Dockerfile`
- **前端镜像**: `docker/frontend/Dockerfile`

## 📖 推荐阅读顺序

### 新手入门
1. [项目概述](../README.md)
2. [环境配置](../ENVIRONMENT_SETUP.md)
3. [启动指南](../START_GUIDE.md)
4. [开发规范](development/CODING_STANDARDS.md)

### 开发者
1. [系统架构](architecture/SYSTEM_ARCHITECTURE.md)
2. [数据库设计](architecture/DATABASE_DESIGN.md)
3. [API参考](api/API_REFERENCE.md)
4. [开发指南](development/DEVELOPMENT_GUIDE.md)

### 运维人员
1. [Docker部署](deployment/DOCKER_DEPLOYMENT.md)
2. [生产部署](deployment/PRODUCTION_DEPLOYMENT.md)
3. [运维手册](deployment/OPERATIONS.md)

## 🔍 快速查找

### 我想...

**了解项目结构**
→ 查看 [项目结构优化方案](PROJECT_STRUCTURE_OPTIMIZATION.md)

**调用API接口**
→ 查看 [API参考手册](api/API_REFERENCE.md)

**部署到服务器**
→ 查看 [Docker部署指南](deployment/DOCKER_DEPLOYMENT.md)

**学习编码规范**
→ 查看 [开发规范](development/CODING_STANDARDS.md)

**集成算法服务**
→ 查看 [前端算法集成](../FRONTEND_ALGORITHM_INTEGRATION.md)

**排查问题**
→ 查看 [常见问题](development/FAQ.md)（待创建）

## 📝 文档维护

### 贡献文档
欢迎提交文档改进建议或补充：
1. Fork项目
2. 创建文档分支
3. 提交修改
4. 发起Pull Request

### 文档规范
- 使用Markdown格式
- 保持中英文混排的规范性
- 添加适当的代码示例
- 包含截图或图表（如需要）
- 更新文档索引

### 更新日志
- **2026-05-09**: 创建文档中心，整理项目结构优化文档
- **2026-05-09**: 添加Docker部署配置
- **2026-05-09**: 完善API集成文档

---

**最后更新**: 2026-05-09  
**维护者**: Classroom Attendance Team  
**反馈**: 请在GitHub Issues中提出文档相关问题

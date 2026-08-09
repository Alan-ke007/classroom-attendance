# 脚本目录

本目录包含项目的所有脚本文件。

## 目录结构

- **build/** - 构建脚本
- **deploy/** - 部署脚本
- **database/** - 数据库脚本
- **utils/** - 工具脚本

## 常用脚本

### 启动脚本
- `utils/start.bat` - Windows启动脚本
- `utils/start_all.bat` - 一键启动所有服务

### 初始化脚本
- `utils/init_database.ps1` - 数据库初始化
- `utils/check_env.bat` - 环境检查

### 测试脚本
- `utils/test_algorithm_integration.bat` - 算法集成测试

## 使用说明

### Windows用户
直接双击 `.bat` 或 `.ps1` 文件运行

### Linux/Mac用户
```bash
chmod +x *.sh
./script-name.sh
```

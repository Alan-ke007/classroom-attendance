@echo off
chcp 65001 >nul
title 课堂智能考勤系统 - 一键启动

color 0A
echo.
echo ========================================
echo   课堂智能考勤系统 - 一键启动
echo ========================================
echo.

REM 检查Java
echo [检查] Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo [错误] 未检测到Java，请先安装JDK 17+
    echo 下载地址: https://adoptium.net/
    pause
    exit /b 1
)
echo [成功] Java环境正常
echo.

REM 检查Node.js
echo [检查] Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo [错误] 未检测到Node.js，请先安装Node.js 16+
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)
echo [成功] Node.js环境正常
echo.

echo ========================================
echo   开始启动服务
echo ========================================
echo.

echo [提示] 即将启动后端和前端服务...
echo [提示] 请确保MySQL已启动且数据库已初始化
echo [提示] 按任意键继续...
pause >nul
echo.

echo [1/2] 正在启动后端服务（端口8080）...
echo       首次启动需要下载依赖，请耐心等待...
start "后端服务 - 端口8080" cmd /k "cd /d %~dp0.. && color 0B && echo 后端服务启动中... && mvn spring-boot:run"
timeout /t 3 /nobreak >nul
echo [成功] 后端服务窗口已打开
echo.

echo [2/2] 正在启动前端服务（端口5173）...
echo       首次启动需要安装依赖，请耐心等待...
start "前端服务 - 端口5173" cmd /k "cd /d %~dp0..\frontend && color 0E && echo 前端服务启动中... && npm run dev"
timeout /t 3 /nobreak >nul
echo [成功] 前端服务窗口已打开
echo.

echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 服务窗口说明：
echo   - 后端服务窗口（蓝色）- 端口8080
echo   - 前端服务窗口（黄色）- 端口5173
echo.
echo 等待时间：
echo   - 后端首次启动: 2-5分钟
echo   - 前端首次启动: 1-3分钟
echo.
echo 访问地址：
echo   - 前端: http://localhost:5173
echo   - 后端API: http://localhost:8080/api
echo.
echo 登录信息：
echo   - 用户名: admin
echo   - 密码: admin123
echo.
echo 提示：
echo   - 请等待服务完全启动后再访问
echo   - 查看窗口输出了解启动进度
echo   - 关闭窗口即可停止服务
echo.
echo ========================================
echo.

pause

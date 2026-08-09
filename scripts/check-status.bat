@echo off
chcp 65001 >nul
title 服务启动状态监控

echo.
echo ========================================
echo   课堂智能考勤系统 - 服务启动监控
echo ========================================
echo.
echo 正在检查服务启动状态...
echo 请等待30秒让服务完全启动...
echo.

timeout /t 30 /nobreak >nul

echo [1/3] 检查后端服务 (http://localhost:8080)...
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo     [✓] 后端服务已启动
) else (
    echo     [×] 后端服务未就绪（可能还在启动中）
)

echo.
echo [2/3] 检查前端服务 (http://localhost:5173)...
curl -s http://localhost:5173/ >nul 2>&1
if %errorlevel% equ 0 (
    echo     [✓] 前端服务已启动
) else (
    echo     [×] 前端服务未就绪（可能还在启动中）
)

echo.
echo [3/3] 检查算法服务 (http://localhost:5000)...
curl -s http://localhost:5000/health >nul 2>&1
if %errorlevel% equ 0 (
    echo     [✓] 算法服务已启动
) else (
    echo     [×] 算法服务未就绪（可能未启动或还在启动中）
)

echo.
echo ========================================
echo   检查完成
echo ========================================
echo.
echo 如果看到 [✓] 表示服务已就绪，可以访问系统
echo 如果看到 [×] 请再等待一会儿后重新运行此脚本
echo.
echo 访问地址：http://localhost:5173
echo 登录信息：admin / admin123
echo.

pause

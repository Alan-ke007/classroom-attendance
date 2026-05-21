@echo off
chcp 65001 >nul
echo ========================================
echo   获取本机IP地址（用于手机访问）
echo ========================================
echo.

echo 正在获取IP地址...
echo.

for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set IP=%%a
    goto :found
)

:found
echo 你的IP地址是:%IP%
echo.
echo ========================================
echo 手机浏览器访问地址：
echo http:%IP%:5173
echo ========================================
echo.
echo 提示：
echo 1. 确保手机和电脑在同一WiFi网络
echo 2. 在Vite配置中已设置 host: '0.0.0.0'
echo 3. 前端服务需要正在运行
echo.
pause

@echo off
chcp 65001 >nul
echo ========================================
echo   Python算法服务 - 环境检查与启动
echo ========================================
echo.

echo [1/3] 检查Python环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo [失败] Python未安装或未配置环境变量
    pause
    exit /b
) else (
    echo [成功] Python已安装
    python --version
)
echo.

echo [2/3] 检查虚拟环境...
if exist "venv\Scripts\activate.bat" (
    echo [成功] 虚拟环境已存在
) else (
    echo [提示] 创建虚拟环境...
    python -m venv venv
    echo [成功] 虚拟环境创建完成
)
echo.

echo [3/3] 激活虚拟环境并安装依赖...
call venv\Scripts\activate.bat

if not exist "venv\Lib\site-packages\flask" (
    echo [提示] 安装依赖包...
    pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
    echo [成功] 依赖安装完成
) else (
    echo [成功] 依赖已安装
)
echo.

echo ========================================
echo   启动Flask服务...
echo   访问: http://localhost:5000
echo   健康检查: http://localhost:5000/health
echo ========================================
echo.

python app.py

pause

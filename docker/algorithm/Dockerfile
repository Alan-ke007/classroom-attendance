# 算法服务Dockerfile - Flask + YOLOv8

FROM python:3.10-slim

WORKDIR /app

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# 复制依赖文件
COPY requirements.txt .

# 安装Python依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制应用代码
COPY . .

# 创建模型目录
RUN mkdir -p models

# 暴露端口
EXPOSE 5000

# 健康检查：判端口存活（模型权重未下载时 /health 返回 503，但进程仍在，不影响其他服务启动）
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD python -c "import socket; s=socket.socket(); s.settimeout(3); s.connect(('localhost',5000)); s.close()" || exit 1

# 启动应用
CMD ["python", "app.py"]

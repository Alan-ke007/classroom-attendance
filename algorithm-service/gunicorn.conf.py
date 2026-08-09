# 生产 WSGI 服务器配置 [审计报告 C2-#1]
# 多 worker 各自独立加载模型（app.py 在 import 期调用 load_models）
bind = "0.0.0.0:5000"
workers = int(__import__("os").environ.get("GUNICORN_WORKERS", "4"))
worker_class = "sync"
timeout = 120
graceful_timeout = 30
keepalive = 5
# 日志输出到 stderr，便于容器 / 平台采集
accesslog = "-"
errorlog = "-"
loglevel = "info"

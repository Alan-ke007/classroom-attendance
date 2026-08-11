from flask import Flask, request, jsonify
from flask_cors import CORS
import cv2
import numpy as np
from ultralytics import YOLO
import base64
import os
from datetime import datetime

app = Flask(__name__)

# ========== P0 安全配置 [审计报告 C2] ==========

# [C2-#4] CORS 白名单：仅放行受信来源，禁止 "*"
# 来源：环境变量 ALGORITHM_CORS_ORIGINS（逗号分隔），默认空（不向任何跨域来源放行）
ALGORITHM_CORS_ORIGINS = [
    o.strip() for o in os.environ.get('ALGORITHM_CORS_ORIGINS', '').split(',') if o.strip()
]
CORS(app, origins=ALGORITHM_CORS_ORIGINS, supports_credentials=False)

# [C2-#2] API Key 鉴权：从环境变量读取，缺失仅告警、不阻断启动
ALGORITHM_API_KEY = os.environ.get('ALGORITHM_API_KEY')
if not ALGORITHM_API_KEY:
    app.logger.warning(
        "⚠ ALGORITHM_API_KEY 未设置！所有受保护接口将拒绝访问(401)。"
        "请通过环境变量注入密钥，切勿硬编码到代码中。"
    )

# [C2-#5] 推理设备：仅 CUDA 启用 FP16，避免 CPU 部署因 half 推理直接 500
DEVICE = os.environ.get('DEVICE', 'cpu').lower()
USE_FP16 = (DEVICE == 'cuda')

# [C2-#3] 上传限制：默认 50MB 上限（可用 ALGORITHM_MAX_UPLOAD_MB 调整）
app.config['MAX_CONTENT_LENGTH'] = int(os.environ.get('ALGORITHM_MAX_UPLOAD_MB', '50')) * 1024 * 1024

# [C2-#3] 运行时模型上传开关：默认关闭。
# 生产环境模型应通过 CI 构建 / 只读挂载注入容器，不应开放运行时上传端点（否则攻击者可上传恶意
# .pt 触发 pickle 反序列化导致 RCE）。仅当显式设置为 TRUE/true/1/YES 时开启；未设置、空、或
# FALSE/false/0 一律视为关闭。
_ALGORITHM_ENABLE_UPLOAD_RAW = os.environ.get('ALGORITHM_ENABLE_UPLOAD', '').strip().upper()
ALGORITHM_ENABLE_UPLOAD = _ALGORITHM_ENABLE_UPLOAD_RAW in {'TRUE', '1', 'YES'}

# [C2-#6] 图片解码像素面积上限（防内存 DoS）：默认约 2500 万像素（5000x5000），
# 可用 ALGORITHM_MAX_IMAGE_PIXELS 覆盖。超过则在推理前返回 400/413。
MAX_IMAGE_PIXELS = int(os.environ.get('ALGORITHM_MAX_IMAGE_PIXELS', '25000000'))

# [C2-#3] 强制 torch.load 安全加载：默认 weights_only=True，阻断 pickle 任意代码执行
# （torch>=2.6 原生默认即 True；此处显式兜底，兼容旧版本）
try:
    import torch
    _orig_torch_load = torch.load

    def _safe_torch_load(*args, **kwargs):
        kwargs.setdefault('weights_only', True)
        return _orig_torch_load(*args, **kwargs)

    torch.load = _safe_torch_load
except Exception:
    pass  # torch 不可用时跳过（YOLO 加载阶段会自行报错）

# ========== 业务参数配置 ==========
BEHAVIOR_CONF_THRESHOLD = 0.20  # 行为检测置信度阈值
IOU_THRESHOLD = 0.5             # NMS IOU 阈值
BEHAVIOR_IMGSZ = 640            # 行为检测输入分辨率
TTA_ENABLED = False             # TTA 增强

# 模型文件路径
BEHAVIOR_MODEL_PATH = 'models/behavior_best.pt'
YOLO_BEHAVIOR_FALLBACK = 'yolov8n.pt'    # 备用行为检测

# =============================


# ========== 鉴权中间件 [C2-#2] ==========
def _check_auth():
    """校验请求头中的 API Key：Authorization: Bearer <key> 或 X-API-Key: <key>"""
    auth_header = request.headers.get('Authorization', '')
    if auth_header.startswith('Bearer '):
        token = auth_header[len('Bearer '):].strip()
        if token and token == ALGORITHM_API_KEY:
            return True
    x_key = request.headers.get('X-API-Key', '').strip()
    if x_key and x_key == ALGORITHM_API_KEY:
        return True
    return False


@app.before_request
def _require_auth():
    # 免鉴权：健康检查 & CORS 预检请求
    if request.path == '/health' or request.method == 'OPTIONS':
        return None
    # 未配置密钥或密钥无效，一律 401（空密钥不可被绕过）
    if not ALGORITHM_API_KEY or not _check_auth():
        return jsonify({'code': 401, 'message': '未授权：缺少或无效的 API Key'}), 401

# 全局变量存储模型
behavior_model = None
face_analyzer = None   # InsightFace buffalo_l（人脸特征提取，512 维 embedding）[P1/F1]

# 行为类别映射
BEHAVIOR_CLASSES = {
    0: 'raising_hand',    # 举手
    1: 'reading',         # 阅读
    2: 'writing',         # 书写
    3: 'using_phone',     # 使用手机
    4: 'bowing_head',     # 低头
    5: 'leaning_over'     # 趴桌
}

def load_model_with_fallback(name, primary_path, fallback_name):
    """加载单个模型，主模型失败则尝试备用"""
    import warnings
    model = None
    if os.path.exists(primary_path):
        try:
            import torch
            with warnings.catch_warnings():
                warnings.simplefilter("ignore")
                model = YOLO(primary_path, task='detect')
            print(f"✓ {name}模型加载成功: {primary_path} ({os.path.getsize(primary_path)/1024:.0f}KB)")
        except Exception as e:
            print(f"⚠ {name}模型加载失败({e})，尝试备用 {fallback_name}...")

    if model is None:
        try:
            model = YOLO(fallback_name, task='detect')
            print(f"✓ {name}备用模型加载成功: {fallback_name}")
        except Exception as e:
            print(f"✗ {name}备用模型也加载失败: {e}")

    return model


def load_face_models():
    """加载 InsightFace buffalo_l（CPU / ONNXRuntime），产出 512 维 L2 归一化 embedding [P1/F1]"""
    global face_analyzer
    try:
        from insightface.app import FaceAnalysis
        face_analyzer = FaceAnalysis(
            name='buffalo_l',
            providers=['CPUExecutionProvider'],
        )
        # ctx_id=-1 表示 CPU；det_size / det_thresh 严格按 PRD §8.2
        ctx_id = 0 if DEVICE == 'cuda' else -1
        face_analyzer.prepare(ctx_id=ctx_id, det_size=(640, 640), det_thresh=0.5)
        print("✓ 人脸模型(buffalo_l)加载成功")

        # 预热：用 dummy 图填充模型，避免首请求冷启（对应 PRD §8.3）
        try:
            face_analyzer.get(np.zeros((640, 640, 3), dtype=np.uint8))
        except Exception as _warm_e:
            print(f"⚠ 人脸模型预热无结果(可忽略): {_warm_e}")
        print("✓ 人脸模型预热完成")
    except Exception as e:
        # 模型未就绪(如首次下载受网络限制)不应阻断进程启动，仅置 None 由 /health 反映 503
        print(f"✗ 人脸模型(buffalo_l)加载失败: {e}")
        face_analyzer = None


def load_models():
    """加载YOLO行为模型 + InsightFace人脸模型（含备用降级方案）"""
    global behavior_model
    print("\n加载模型中...")

    behavior_model = load_model_with_fallback("行为检测", BEHAVIOR_MODEL_PATH, YOLO_BEHAVIOR_FALLBACK)

    if behavior_model:
        print(f"✓ 行为检测模型就绪")
    else:
        print(f"✗ 所有模型加载失败")

    load_face_models()

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口 [C2-#7] 如实反映模型就绪状态，未就绪返回 503"""
    ready = (behavior_model is not None) and (face_analyzer is not None)
    return jsonify({
        'status': 'ok' if ready else 'degraded',
        'model_loaded': ready,
        'message': 'Algorithm service is running' if ready else 'Model not loaded yet',
        'timestamp': datetime.now().isoformat()
    }), 200 if ready else 503

@app.route('/api/behavior/detect', methods=['POST'])
def detect_behavior():
    """
    行为检测接口
    检测学生行为（举手、阅读、书写、玩手机、低头、趴桌）
    """
    try:
        if behavior_model is None:
            return jsonify({
                'code': 500,
                'message': '行为检测模型未加载'
            }), 500

        # [C2-#6] 输入校验：必须是 JSON 且 body 非空
        if not request.is_json:
            return jsonify({'code': 400, 'message': '请求必须为 JSON'}), 400
        data = request.get_json(silent=True)
        if not isinstance(data, dict) or not data:
            return jsonify({'code': 400, 'message': '请求体为空或格式错误'}), 400

        # 获取 base64 编码的图片
        image_base64 = data.get('image', '')

        if not image_base64:
            return jsonify({
                'code': 400,
                'message': '未提供图片数据'
            }), 400

        # [C2-#6] 兼容可能的 data URL 前缀
        raw = image_base64
        if isinstance(raw, str) and raw.startswith('data:'):
            try:
                raw = raw.split(',', 1)[1]
            except Exception:
                raw = ''

        # [C2-#6] base64 解码单独 try，失败返 400（不向上层泄露内部错误）
        try:
            image_data = base64.b64decode(raw, validate=True)
        except Exception:
            return jsonify({'code': 400, 'message': '图片 base64 解码失败'}), 400

        nparr = np.frombuffer(image_data, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            return jsonify({
                'code': 400,
                'message': '图片解码失败'
            }), 400

        # [C2-#6] 限制输入尺寸，防止超大图耗尽内存/算力
        h, w = image.shape[:2]
        if w <= 0 or h <= 0 or max(w, h) > 4000:
            return jsonify({'code': 400, 'message': '图片尺寸超出允许范围(最大 4000px)'}), 400

        # [C2-#6] 像素面积上限（防内存 DoS）：宽*高超过 MAX_IMAGE_PIXELS 直接拒绝，避免超大图撑爆内存
        if w * h > MAX_IMAGE_PIXELS:
            app.logger.warning(
                f"detect_behavior rejected: image area {w}x{h}={w*h} exceeds limit {MAX_IMAGE_PIXELS}"
            )
            return jsonify({
                'code': 413,
                'message': '图片像素面积超出上限，请压缩后重试'
            }), 413

        # 图像预处理：CLAHE 增强低光照
        lab = cv2.cvtColor(image, cv2.COLOR_BGR2LAB)
        l, a, b = cv2.split(lab)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        l = clahe.apply(l)
        enhanced = cv2.merge([l, a, b])
        enhanced = cv2.cvtColor(enhanced, cv2.COLOR_LAB2BGR)

        # 使用YOLO进行行为检测
        predict_kwargs = dict(
            imgsz=BEHAVIOR_IMGSZ,
            conf=BEHAVIOR_CONF_THRESHOLD,
            iou=IOU_THRESHOLD,
            verbose=False,
            half=USE_FP16,
        )
        if TTA_ENABLED:
            predict_kwargs['augment'] = True

        results = behavior_model(enhanced, **predict_kwargs)

        # 解析检测结果
        behaviors = []
        for result in results:
            boxes = result.boxes
            if boxes is not None:
                dets = boxes.xyxy.cpu().numpy()
                confs = boxes.conf.cpu().numpy()
                cls_ids = boxes.cls.cpu().numpy().astype(int)

                for i in range(len(dets)):
                    x1, y1, x2, y2 = dets[i]
                    confidence = float(confs[i])
                    class_id = cls_ids[i]

                    # 获取行为类型
                    behavior_type = BEHAVIOR_CLASSES.get(class_id, 'unknown')

                    # 中文描述
                    description_map = {
                        'raising_hand': '举手',
                        'reading': '阅读',
                        'writing': '书写',
                        'using_phone': '使用手机',
                        'bowing_head': '低头',
                        'leaning_over': '趴桌'
                    }

                    behaviors.append({
                        'behaviorId': f'behavior_{i}',
                        'type': behavior_type,
                        'description': description_map.get(behavior_type, '未知行为'),
                        'confidence': round(confidence, 4),
                        'boundingBox': [
                            int(x1),
                            int(y1),
                            int(x2 - x1),  # width
                            int(y2 - y1)   # height
                        ]
                    })

        result = {
            'code': 200,
            'message': '检测成功',
            'data': {
                'behaviors': behaviors,
                'totalCount': len(behaviors),
                'timestamp': datetime.now().isoformat()
            }
        }

        return jsonify(result)

    except Exception as e:
        # [C2-#6] 5xx 仅返回通用信息并记录日志，禁止泄露内部路径/堆栈
        app.logger.error(f"detect_behavior failed: {e}")
        return jsonify({'code': 500, 'message': '检测失败，请稍后重试'}), 500

@app.route('/api/model/upload', methods=['POST'])
def upload_model():
    """
    上传训练好的模型 [C2-#3]
    注意：生产环境应关闭此运行时上传端点，模型经 CI 构建 / 只读挂载注入容器内，
    避免任意模型上传导致 RCE 风险。当前已加鉴权 + 大小 + 后缀守卫作为兜底。
    """
    try:
        # [C2-#3] 运行时上传开关：生产环境默认关闭。模型应由 CI 构建 / 只读挂载注入容器，
        # 开放运行时上传等同于给任意 .pt 上传留口子（可触发 pickle 反序列化 RCE）。
        if not ALGORITHM_ENABLE_UPLOAD:
            app.logger.warning("upload_model rejected: ALGORITHM_ENABLE_UPLOAD is disabled")
            return jsonify({'code': 403, 'message': '运行时模型上传已禁用'}), 403

        if 'model' not in request.files:
            return jsonify({
                'code': 400,
                'message': '未提供模型文件'
            }), 400

        model_file = request.files['model']
        model_type = request.form.get('type', 'behavior')  # behavior / face

        # [C2-#3] 白名单校验：仅允许已知类型，否则 400；杜绝 type 被用于构造恶意路径
        if model_type not in ('behavior', 'face'):
            return jsonify({'code': 400, 'message': '不支持的模型类型'}), 400

        # [C2-#3] 基础净化：剔除路径分隔符与上级目录引用，确保 model_type 仅作为纯文件名片段，
        # 阻断路径穿越（如 type=../../etc/passwd 之类写入 models 目录之外）
        model_type = model_type.replace('/', '').replace('\\', '').replace('..', '')

        # [C2-#3] 仅允许 .pt 后缀，拒绝其他可执行/脚本文件
        original = model_file.filename or ''
        if not original.lower().endswith('.pt'):
            return jsonify({'code': 400, 'message': '仅允许上传 .pt 模型文件'}), 400

        # 保存模型文件
        save_dir = 'models'
        os.makedirs(save_dir, exist_ok=True)

        filename = f"{model_type}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.pt"
        filepath = os.path.join(save_dir, filename)
        model_file.save(filepath)

        return jsonify({
            'code': 200,
            'message': '模型上传成功',
            'data': {
                'filename': filename,
                'path': filepath
            }
        })

    except Exception as e:
        # [C2-#6] 5xx 仅返回通用信息，禁止泄露内部路径
        app.logger.error(f"upload_model failed: {e}")
        return jsonify({'code': 500, 'message': '上传失败，请稍后重试'}), 500

@app.route('/api/face/extract', methods=['POST'])
def extract_face():
    """
    人脸特征提取接口 [P1/F1]
    输入图片(base64) -> 512 维 L2 归一化 embedding（InsightFace buffalo_l）。
    算法服务无状态、不持有 gallery；多脸直接报错，不返回计数。
    输入校验与错误码风格复用 /api/behavior/detect 模板。
    """
    try:
        # 模型未加载（如首次下载受网络限制）-> 500，不泄露内部堆栈
        if face_analyzer is None:
            return jsonify({'code': 500, 'message': '人脸模型未加载'}), 500

        # [C2-#6] 输入校验：必须是 JSON 且 body 非空
        if not request.is_json:
            return jsonify({'code': 400, 'message': '请求必须为 JSON'}), 400
        data = request.get_json(silent=True)
        if not isinstance(data, dict) or not data:
            return jsonify({'code': 400, 'message': '请求体为空或格式错误'}), 400

        # 获取 base64 编码的图片
        image_base64 = data.get('image', '')
        if not image_base64:
            return jsonify({'code': 400, 'message': '未提供图片数据'}), 400

        # [C2-#6] 兼容 data:image/...;base64, 前缀
        raw = image_base64
        if isinstance(raw, str) and raw.startswith('data:'):
            try:
                raw = raw.split(',', 1)[1]
            except Exception:
                raw = ''

        # [C2-#6] base64 解码单独 try，失败返 400（不向上层泄露内部错误）
        try:
            image_data = base64.b64decode(raw, validate=True)
        except Exception:
            return jsonify({'code': 400, 'message': '图片 base64 解码失败'}), 400

        nparr = np.frombuffer(image_data, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if image is None:
            return jsonify({'code': 400, 'message': '图片解码失败'}), 400

        # [C2-#6] 限制输入尺寸，防止超大图耗尽内存/算力；宽或高 >4000px 返回 400
        h, w = image.shape[:2]
        if w <= 0 or h <= 0 or max(w, h) > 4000:
            return jsonify({'code': 400, 'message': '图片尺寸超出允许范围(最大 4000px)'}), 400

        # [C2-#6] 像素面积上限（防内存 DoS）：宽*高超过 MAX_IMAGE_PIXELS 直接拒绝
        if w * h > MAX_IMAGE_PIXELS:
            app.logger.warning(
                f"extract_face rejected: image area {w}x{h}={w*h} exceeds limit {MAX_IMAGE_PIXELS}"
            )
            return jsonify({
                'code': 413,
                'message': '图片像素面积超出上限，请压缩后重试'
            }), 413

        # InsightFace 检测 + 识别（不限制 max_num，以便准确统计人脸数）
        faces = face_analyzer.get(image)
        face_count = len(faces)

        if face_count == 0:
            return jsonify({'code': 40010, 'message': 'NO_FACE_DETECTED'}), 400
        if face_count > 1:
            return jsonify({'code': 40011, 'message': 'MULTI_FACE_DETECTED'}), 400

        # 取唯一人脸的 512 维 embedding，强制 L2 归一化（idempotent）
        emb = faces[0].embedding.astype(np.float32)
        norm = np.linalg.norm(emb)
        if norm > 0:
            emb = emb / norm

        result = {
            'code': 200,
            'message': '提取成功',
            'data': {
                'embedding': emb.tolist(),
                'faceCount': 1,
            }
        }
        return jsonify(result)

    except Exception as e:
        # [C2-#6] 5xx 仅返回通用信息并记录日志，禁止泄露内部路径/堆栈
        app.logger.error(f"extract_face failed: {e}")
        return jsonify({'code': 500, 'message': '检测失败，请稍后重试'}), 500


# [C2-#1] 模块导入即加载模型：gunicorn 多 worker 各自独立加载（python app.py 同样生效）
load_models()


# [C2-#3] 上传超 MAX_CONTENT_LENGTH 时返回统一信封（默认 413）
@app.errorhandler(413)
def _request_too_large(e):
    return jsonify({'code': 413, 'message': '上传文件超出大小限制'}), 413


if __name__ == '__main__':
    # [C2-#1] 生产请用 gunicorn 启动（见 Procfile / gunicorn.conf.py）：
    #   gunicorn -c gunicorn.conf.py app:app
    # 以下仅保留本地开发入口，debug 必须 False（绝不可为 True 暴露 Werkzeug 调试器）。
    app.run(host='0.0.0.0', port=5000, debug=False)

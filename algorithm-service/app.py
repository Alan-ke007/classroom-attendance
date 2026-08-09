from flask import Flask, request, jsonify
from flask_cors import CORS
import cv2
import numpy as np
from ultralytics import YOLO
import base64
import os
from datetime import datetime

app = Flask(__name__)
CORS(app)

# ========== 参数配置 ==========
BEHAVIOR_CONF_THRESHOLD = 0.20  # 行为检测置信度阈值
IOU_THRESHOLD = 0.5             # NMS IOU阈值
BEHAVIOR_IMGSZ = 640            # 行为检测输入分辨率
USE_FP16 = True                 # 半精度推理 (提速)
TTA_ENABLED = False             # TTA增强

# 模型文件路径
BEHAVIOR_MODEL_PATH = 'models/behavior_best.pt'
YOLO_BEHAVIOR_FALLBACK = 'yolov8n.pt'    # 备用行为检测

# =============================

# 全局变量存储模型
behavior_model = None

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


def load_models():
    """加载YOLO模型（含备用降级方案）"""
    global behavior_model
    print("\n加载模型中...")

    behavior_model = load_model_with_fallback("行为检测", BEHAVIOR_MODEL_PATH, YOLO_BEHAVIOR_FALLBACK)

    if behavior_model:
        print(f"✓ 行为检测模型就绪")
    else:
        print(f"✗ 所有模型加载失败")

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    return jsonify({
        'status': 'ok',
        'message': 'Algorithm service is running',
        'timestamp': datetime.now().isoformat()
    })

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

        data = request.json

        # 获取base64编码的图片
        image_base64 = data.get('image', '')

        if not image_base64:
            return jsonify({
                'code': 400,
                'message': '未提供图片数据'
            }), 400

        # 解码图片
        image_data = base64.b64decode(image_base64)
        nparr = np.frombuffer(image_data, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            return jsonify({
                'code': 400,
                'message': '图片解码失败'
            }), 400

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
        return jsonify({
            'code': 500,
            'message': f'检测失败: {str(e)}'
        }), 500

@app.route('/api/model/upload', methods=['POST'])
def upload_model():
    """
    上传训练好的模型
    """
    try:
        if 'model' not in request.files:
            return jsonify({
                'code': 400,
                'message': '未提供模型文件'
            }), 400

        model_file = request.files['model']
        model_type = request.form.get('type', 'behavior')  # behavior only

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
        return jsonify({
            'code': 500,
            'message': f'上传失败: {str(e)}'
        }), 500

if __name__ == '__main__':
    # 启动时加载模型
    load_models()

    # 运行Flask应用
    app.run(host='0.0.0.0', port=5000, debug=True)

# 课堂考勤算法服务

基于YOLOv8的课堂行为检测和人脸识别算法服务，提供RESTful API接口。

## 🎯 功能特性

- ✅ **行为检测**: 检测6种课堂行为（举手、阅读、书写、玩手机、低头、趴桌）
- ✅ **人脸检测**: 高精度人脸检测用于考勤
- ✅ **RESTful API**: 提供标准的HTTP接口
- ✅ **GPU加速**: 支持NVIDIA GPU加速推理
- ✅ **高性能**: mAP50达到85.9%（行为检测）和93.3%（人脸检测）

## 📋 系统要求

- Python 3.8+
- NVIDIA GPU (推荐RTX 3060或更高)
- CUDA 11.8+
- 8GB+ RAM

## 🚀 快速开始

### 1. 安装依赖

```bash
# 激活虚拟环境（如果使用）
venv\Scripts\activate

# 安装依赖包
pip install -r requirements.txt
```

### 2. 启动服务

```bash
python app.py
```

服务将在 `http://localhost:5000` 启动。

### 3. 测试API

```bash
# 健康检查
curl http://localhost:5000/health

# 行为检测
curl -X POST http://localhost:5000/api/behavior/detect \
  -H "Content-Type: application/json" \
  -d '{"image": "base64_encoded_image_data"}'
```

## 📊 模型性能

### 行为检测模型
- **数据集**: 6021张图片（合并三个数据集）
- **mAP50**: 85.9%
- **mAP50-95**: 65.0%
- **推理速度**: 1.1ms/图 (RTX 3060)

| 行为类别 | mAP50 |
|---------|-------|
| 玩手机 | 95.7% ⭐ |
| 趴桌 | 97.8% ⭐⭐ |
| 低头 | 87.7% ⭐ |
| 阅读 | 80.5% |
| 书写 | 77.2% |
| 举手 | 76.9% |

### 人脸检测模型
- **数据集**: SCUT-HEAD
- **mAP50**: 93.3%
- **Precision**: 91.6%
- **Recall**: 88.0%

## 📁 项目结构

详见 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

```
algorithm-service/
├── app.py                      # Flask API服务
├── train_new_datasets.py       # 训练脚本
├── merged_dataset.yaml         # 数据集配置
├── models/                     # 训练好的模型
│   ├── behavior_best.pt        # 行为检测模型
│   └── face_detection.pt       # 人脸检测模型
├── datasets/
│   └── merged_dataset/         # 合并数据集
├── runs/train/                 # 训练结果
│   ├── behavior_merged_dataset/ # 行为检测结果
│   └── face_detection2/        # 人脸检测结果
└── requirements.txt            # 依赖包
```

## 🔧 API接口

### 健康检查
```
GET /health
```

### 行为检测
```
POST /api/behavior/detect
Content-Type: application/json

{
  "image": "base64_encoded_image"
}
```

### 人脸识别考勤
```
POST /api/attendance/recognize
Content-Type: application/json

{
  "image": "base64_encoded_image"
}
```

### 上传模型
```
POST /api/model/upload
Content-Type: multipart/form-data

model: <file>
type: behavior|face
```

## 🏃 训练新模型

### 使用合并数据集训练（推荐）

```bash
python train_new_datasets.py --dataset merged --epochs 100 --batch 16
```

### 其他数据集选项

```bash
# 小数据集 (335张)
python train_new_datasets.py --dataset 0.355k

# 中等数据集 (671张)
python train_new_datasets.py --dataset 0.671k

# 大数据集 (5015张)
python train_new_datasets.py --dataset 5k

# 训练所有数据集
python train_new_datasets.py --dataset all
```

## 📈 性能优化建议

1. **使用更大的批次**: 如果显存充足，可以增加batch size
2. **增加训练轮数**: 对于更大数据集，可以尝试150-200 epochs
3. **数据增强**: 已启用Mosaic、翻转、HSV增强等
4. **模型选择**: 当前使用YOLOv8n，如需更高精度可尝试YOLOv8s/m

## 🛠️ 项目维护

### 清理临时文件

运行清理脚本删除不需要的文件：

```bash
cleanup_project.bat
```

### 备份模型

定期备份 `models/` 目录下的模型文件。

## 📝 开发计划

- [ ] 集成真实模型到API服务
- [ ] 添加视频流处理支持
- [ ] 实现批量推理优化
- [ ] 添加TensorRT加速
- [ ] 完善单元测试
- [ ] 添加Docker部署支持

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request！

---

**最后更新**: 2026-05-09

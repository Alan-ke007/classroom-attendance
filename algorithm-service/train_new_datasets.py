"""
训练脚本 - 使用新的大学行为数据集
支持 0.355k、0.671k 和 5k HRW 数据集

使用建议（精度从低到高）:
  YOLOv8n < YOLOv8s < YOLOv8m < YOLOv8l < YOLOv8x
  模型越大精度越高，但推理速度越慢。
  教室场景建议用 YOLOv8m 或 YOLOv8l 平衡速度与精度。
"""

from ultralytics import YOLO
import os
import torch
import argparse


def train_with_dataset(dataset_config, dataset_name, epochs=100, batch_size=16, model_size='m'):
    """使用指定数据集训练模型

    Args:
        model_size: 模型规格 (n/s/m/l/x)，越大越准越慢
    """

    print("=" * 60)
    print(f"开始训练: {dataset_name}")
    print(f"模型规格: YOLOv8{model_size}")
    print("=" * 60)

    # 检查 GPU 可用性
    if torch.cuda.is_available():
        print(f"检测到 GPU: {torch.cuda.get_device_name(0)}")
        print(f"显存: {torch.cuda.get_device_properties(0).total_memory / 1024**3:.1f} GB")
        device = '0'
    else:
        print("未检测到 GPU，使用 CPU 训练（速度较慢）")
        device = 'cpu'

    # 加载预训练模型
    model_name = f'yolov8{model_size}.pt'
    print(f"\n加载 {model_name} 预训练权重...")
    model = YOLO(model_name)

    # 训练配置
    print(f"\n开始训练 {dataset_name}...")
    results = model.train(
        data=dataset_config,           # 数据集配置文件
        epochs=epochs,                 # 训练轮数
        imgsz=640,                     # 图片尺寸
        batch=batch_size,              # 批次大小
        device=device,                 # 设备
        patience=20,                   # 早停耐心值
        save=True,                     # 保存模型
        save_period=10,                # 每10个epoch保存一次
        name=f'behavior_{dataset_name}',  # 实验名称
        project='runs/train',          # 项目目录

        # 数据增强配置（增加多样性提高泛化）
        mosaic=1.0,                    # Mosaic 增强概率
        mixup=0.2,                     # MixUp 增强 (v8新增)
        copy_paste=0.1,                # Copy-Paste 增强
        flipud=0.5,                    # 上下翻转概率
        fliplr=0.5,                    # 左右翻转概率
        hsv_h=0.02,                    # HSV 色调增强 (0.015→0.02)
        hsv_s=0.8,                     # HSV 饱和度增强 (0.7→0.8)
        hsv_v=0.5,                     # HSV 亮度增强 (0.4→0.5)
        degrees=10.0,                  # 旋转增强 (新增)
        translate=0.1,                 # 平移增强 (新增)
        scale=0.5,                     # 缩放增强 (新增)
        shear=2.0,                     # 剪切增强 (新增)

        # 优化器配置
        optimizer='AdamW',             # 改用 AdamW (比 SGD 收敛更快，尤其小数据集)
        lr0=0.001,                     # 初始学习率 (AdamW 用较小的 lr)
        lrf=0.01,                      # 最终学习率 (lr0 * lrf)
        momentum=0.937,                # SGD momentum (AdamW 也有用)
        weight_decay=0.0005,           # 权重衰减
        warmup_epochs=3,               # 预热轮数

        # Loss 加权（提升小目标/低频类效果）
        # cls_pw=1.0,                  # 分类 loss 权重

        # 其他配置
        workers=8,                     # 数据加载线程数
        verbose=True,                  # 详细日志
    )

    print("\n" + "=" * 60)
    print(f"{dataset_name} 训练完成！")
    print("=" * 60)
    print(f"最佳模型保存在: runs/train/behavior_{dataset_name}/weights/best.pt")
    print(f"最后模型保存在: runs/train/behavior_{dataset_name}/weights/last.pt")

    return results


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='训练行为检测模型')
    parser.add_argument('--dataset', type=str, default='all',
                        choices=['0.355k', '0.671k', '5k', 'merged', 'all'],
                        help='选择要训练的数据集')
    parser.add_argument('--epochs', type=int, default=100,
                        help='训练轮数')
    parser.add_argument('--batch', type=int, default=16,
                        help='批次大小')
    parser.add_argument('--model', type=str, default='m',
                        choices=['n', 's', 'm', 'l', 'x'],
                        help='模型规格: n(ano) s(mall) m(edium) l(arge) x(xtra large)')

    args = parser.parse_args()

    # 创建必要的目录
    os.makedirs('runs/train', exist_ok=True)
    os.makedirs('datasets', exist_ok=True)

    # 定义数据集配置
    datasets = {
        '0.355k': {
            'config': 'university_0.355k_dataset.yaml',
            'name': 'university_0.355k'
        },
        '0.671k': {
            'config': 'university_0.671k_dataset.yaml',
            'name': 'university_0.671k'
        },
        '5k': {
            'config': 'hrw_5k_dataset.yaml',
            'name': 'hrw_5k'
        },
        'merged': {
            'config': 'merged_dataset.yaml',
            'name': 'merged_dataset'
        }
    }

    # 根据参数选择要训练的数据集
    if args.dataset == 'all':
        for key, dataset_info in datasets.items():
            results = train_with_dataset(
                dataset_info['config'],
                dataset_info['name'],
                epochs=args.epochs,
                batch_size=args.batch,
                model_size=args.model
            )
            print(f"\n\n{'='*80}")
            print(f"完成 {key} 数据集训练")
            print(f"{'='*80}\n")
    else:
        dataset_info = datasets[args.dataset]
        results = train_with_dataset(
            dataset_info['config'],
            dataset_info['name'],
            epochs=args.epochs,
            batch_size=args.batch,
            model_size=args.model
        )

        # 打印训练结果摘要
        print("\n训练结果摘要:")
        print(f"总训练时间: {results.results_dict.get('metrics/train_time', 'N/A')}")
        print(f"验证 mAP@0.5: {results.results_dict.get('metrics/mAP50(B)', 'N/A')}")
        print(f"验证 mAP@0.5:0.95: {results.results_dict.get('metrics/mAP50-95(B)', 'N/A')}")

"""
模型评估报告生成脚本
计算 mAP@0.5、Recall 等指标并生成评估报告
"""

from ultralytics import YOLO
import os
import json
from datetime import datetime

def evaluate_model(model_path, data_yaml, model_name="model"):
    """
    评估训练好的模型
    
    Args:
        model_path: 模型文件路径 (best.pt)
        data_yaml: 数据集配置文件路径
        model_name: 模型名称（用于报告）
    """
    
    print("=" * 60)
    print(f"开始评估模型: {model_name}")
    print("=" * 60)
    
    # 加载模型
    print(f"\n加载模型: {model_path}")
    model = YOLO(model_path)
    
    # 在验证集上进行评估
    print("\n开始在验证集上评估...")
    results = model.val(
        data=data_yaml,
        imgsz=640,
        batch=16,
        verbose=True
    )
    
    # 提取关键指标
    metrics = {
        'model_name': model_name,
        'model_path': model_path,
        'evaluation_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        'mAP50': float(results.box.map50),           # mAP@0.5
        'mAP50_95': float(results.box.map),          # mAP@0.5:0.95
        'precision': float(results.box.mp),          # 精确率
        'recall': float(results.box.mr),             # 召回率
        'f1_score': float(2 * results.box.mp * results.box.mr / 
                         (results.box.mp + results.box.mr + 1e-8)),  # F1分数
    }
    
    # 打印评估结果
    print("\n" + "=" * 60)
    print("评估结果摘要")
    print("=" * 60)
    print(f"模型名称: {metrics['model_name']}")
    print(f"评估时间: {metrics['evaluation_time']}")
    print(f"mAP@0.5:    {metrics['mAP50']:.4f} ({metrics['mAP50']*100:.2f}%)")
    print(f"mAP@0.5:0.95: {metrics['mAP50_95']:.4f} ({metrics['mAP50_95']*100:.2f}%)")
    print(f"Precision:  {metrics['precision']:.4f} ({metrics['precision']*100:.2f}%)")
    print(f"Recall:     {metrics['recall']:.4f} ({metrics['recall']*100:.2f}%)")
    print(f"F1 Score:   {metrics['f1_score']:.4f} ({metrics['f1_score']*100:.2f}%)")
    print("=" * 60)
    
    # 保存评估报告
    report_dir = 'runs/evaluation'
    os.makedirs(report_dir, exist_ok=True)
    
    report_file = os.path.join(report_dir, f'{model_name}_evaluation.json')
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(metrics, f, indent=2, ensure_ascii=False)
    
    print(f"\n评估报告已保存到: {report_file}")
    
    # 生成文本格式的详细报告
    txt_report = os.path.join(report_dir, f'{model_name}_report.txt')
    with open(txt_report, 'w', encoding='utf-8') as f:
        f.write("=" * 60 + "\n")
        f.write(f"模型评估报告 - {model_name}\n")
        f.write("=" * 60 + "\n\n")
        f.write(f"评估时间: {metrics['evaluation_time']}\n")
        f.write(f"模型路径: {metrics['model_path']}\n\n")
        f.write("性能指标:\n")
        f.write(f"  mAP@0.5:      {metrics['mAP50']:.4f} ({metrics['mAP50']*100:.2f}%)\n")
        f.write(f"  mAP@0.5:0.95: {metrics['mAP50_95']:.4f} ({metrics['mAP50_95']*100:.2f}%)\n")
        f.write(f"  Precision:    {metrics['precision']:.4f} ({metrics['precision']*100:.2f}%)\n")
        f.write(f"  Recall:       {metrics['recall']:.4f} ({metrics['recall']*100:.2f}%)\n")
        f.write(f"  F1 Score:     {metrics['f1_score']:.4f} ({metrics['f1_score']*100:.2f}%)\n\n")
        
        # 添加各类别的详细指标
        if hasattr(results, 'box') and hasattr(results.box, 'maps'):
            f.write("各类别 mAP@0.5:\n")
            for i, map50 in enumerate(results.box.maps):
                f.write(f"  类别 {i}: {map50:.4f} ({map50*100:.2f}%)\n")
        
        f.write("\n" + "=" * 60 + "\n")
        f.write("评估完成\n")
        f.write("=" * 60 + "\n")
    
    print(f"详细报告已保存到: {txt_report}")
    
    return metrics


if __name__ == '__main__':
    # 评估行为识别模型
    print("\n" + "=" * 60)
    print("评估课堂行为识别模型")
    print("=" * 60)
    behavior_metrics = evaluate_model(
        model_path='runs/train/behavior_detection/weights/best.pt',
        data_yaml='scb_dataset.yaml',
        model_name='behavior_detection'
    )
    
    # 评估人脸检测模型
    print("\n" + "=" * 60)
    print("评估人脸检测模型")
    print("=" * 60)
    face_metrics = evaluate_model(
        model_path='runs/train/face_detection/weights/best.pt',
        data_yaml='scut_head.yaml',
        model_name='face_detection'
    )
    
    # 生成综合对比报告
    print("\n" + "=" * 60)
    print("模型性能对比")
    print("=" * 60)
    print(f"\n{'指标':<20} {'行为识别':<15} {'人脸检测':<15}")
    print("-" * 60)
    print(f"{'mAP@0.5':<20} {behavior_metrics['mAP50']*100:<15.2f}% {face_metrics['mAP50']*100:<15.2f}%")
    print(f"{'mAP@0.5:0.95':<20} {behavior_metrics['mAP50_95']*100:<15.2f}% {face_metrics['mAP50_95']*100:<15.2f}%")
    print(f"{'Precision':<20} {behavior_metrics['precision']*100:<15.2f}% {face_metrics['precision']*100:<15.2f}%")
    print(f"{'Recall':<20} {behavior_metrics['recall']*100:<15.2f}% {face_metrics['recall']*100:<15.2f}%")
    print(f"{'F1 Score':<20} {behavior_metrics['f1_score']*100:<15.2f}% {face_metrics['f1_score']*100:<15.2f}%")
    print("=" * 60)

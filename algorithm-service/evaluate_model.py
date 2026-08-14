"""
课堂行为识别模型评估报告生成脚本
================================

计算 mAP@0.5、mAP@0.5:0.95、Precision、Recall、F1，并输出：
  - 整体指标
  - 各类别 mAP@0.5 / Precision / Recall（若 ultralytics 版本支持则输出）
  - 混淆矩阵图（model.val(plots=True) 自动生成，即「预测类别 vs 人工标注类别」对比）
  - 预测 vs 人工标注对比样本图（可选，见 predict_samples）

【复现说明 / 重要】
  * 本脚本需在具备 GPU 的环境运行。“真实 mAP”必须由你在本地 / 实验室 GPU 上
    跑出，不能凭空填写到论文中（否则属于学术不端）。
  * 依赖：ultralytics、PyTorch(GPU)、已标注验证集。
  * 数据集配置请使用 merged_dataset.yaml（与算法训练保持一致），并确认其中
    path 指向你本地的数据集根目录。
  * 模型权重路径 MODEL_PATH 请指向训练产出的 best.pt。

运行示例：
  python evaluate_model.py
"""

from ultralytics import YOLO
import os
import json
from datetime import datetime


def evaluate_model(model_path, data_yaml, model_name="model", plots=True):
    """
    评估训练好的行为识别模型。

    Args:
        model_path: 模型文件路径 (best.pt)
        data_yaml:  数据集配置文件路径（建议 merged_dataset.yaml）
        model_name: 模型名称（用于报告与输出目录）
        plots:      是否生成混淆矩阵等可视化
    """
    print("=" * 60)
    print(f"开始评估模型: {model_name}")
    print("=" * 60)

    model = YOLO(model_path)

    print(f"\n加载模型: {model_path}")
    print("\n开始在验证集上评估（与人工标注比对）...")
    results = model.val(
        data=data_yaml,
        imgsz=640,
        batch=16,
        verbose=True,
        plots=plots,                        # 生成混淆矩阵等可视化
        project='runs/evaluation',
        name=model_name,
        exist_ok=True,
    )

    # 整体指标
    metrics = {
        'model_name': model_name,
        'model_path': model_path,
        'data_yaml': data_yaml,
        'evaluation_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        'mAP50': float(results.box.map50),
        'mAP50_95': float(results.box.map),
        'precision': float(results.box.mp),
        'recall': float(results.box.mr),
        'f1_score': float(2 * results.box.mp * results.box.mr /
                          (results.box.mp + results.box.mr + 1e-8)),
    }

    # 各类别指标（稳健提取：版本差异时跳过而非报错）
    per_class = {}
    try:
        names = model.names                      # {0: 'raising_hand', ...}
        class_names = [names[i] for i in sorted(names.keys())]
        maps = list(results.box.maps)            # 逐类 mAP@0.5
        p_list = list(getattr(results.box, 'p', []) or [])   # 逐类 Precision
        r_list = list(getattr(results.box, 'r', []) or [])   # 逐类 Recall
        for i, cname in enumerate(class_names):
            per_class[cname] = {
                'mAP50': round(float(maps[i]) * 100, 2) if i < len(maps) else None,
                'precision': round(float(p_list[i]) * 100, 2) if i < len(p_list) else None,
                'recall': round(float(r_list[i]) * 100, 2) if i < len(r_list) else None,
            }
        metrics['per_class'] = per_class
    except Exception as e:
        print(f"[warn] 逐类指标提取失败（不影响整体 mAP）：{e}")

    # 打印评估结果
    print("\n" + "=" * 60)
    print("评估结果摘要")
    print("=" * 60)
    print(f"模型名称: {metrics['model_name']}")
    print(f"评估时间: {metrics['evaluation_time']}")
    print(f"mAP@0.5:      {metrics['mAP50']:.4f} ({metrics['mAP50']*100:.2f}%)")
    print(f"mAP@0.5:0.95: {metrics['mAP50_95']:.4f} ({metrics['mAP50_95']*100:.2f}%)")
    print(f"Precision:    {metrics['precision']:.4f} ({metrics['precision']*100:.2f}%)")
    print(f"Recall:       {metrics['recall']:.4f} ({metrics['recall']*100:.2f}%)")
    print(f"F1 Score:     {metrics['f1_score']:.4f} ({metrics['f1_score']*100:.2f}%)")

    if per_class:
        print("\n各类别指标 (mAP@0.5 / Precision / Recall):")
        for cname, m in per_class.items():
            print(f"  {cname:<14} mAP50={m['mAP50']}%  P={m['precision']}%  R={m['recall']}%")

    print("=" * 60)

    # 保存评估报告（JSON）
    report_dir = 'runs/evaluation'
    os.makedirs(report_dir, exist_ok=True)
    report_file = os.path.join(report_dir, f'{model_name}_evaluation.json')
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(metrics, f, indent=2, ensure_ascii=False)
    print(f"\n评估报告(JSON)已保存到: {report_file}")

    # 生成文本格式的详细报告
    txt_report = os.path.join(report_dir, f'{model_name}_report.txt')
    with open(txt_report, 'w', encoding='utf-8') as f:
        f.write("=" * 60 + "\n")
        f.write(f"模型评估报告 - {model_name}\n")
        f.write("=" * 60 + "\n\n")
        f.write(f"评估时间:   {metrics['evaluation_time']}\n")
        f.write(f"模型路径:   {metrics['model_path']}\n")
        f.write(f"数据集配置: {metrics['data_yaml']}\n\n")
        f.write("整体性能指标:\n")
        f.write(f"  mAP@0.5:      {metrics['mAP50']:.4f} ({metrics['mAP50']*100:.2f}%)\n")
        f.write(f"  mAP@0.5:0.95: {metrics['mAP50_95']:.4f} ({metrics['mAP50_95']*100:.2f}%)\n")
        f.write(f"  Precision:    {metrics['precision']:.4f} ({metrics['precision']*100:.2f}%)\n")
        f.write(f"  Recall:       {metrics['recall']:.4f} ({metrics['recall']*100:.2f}%)\n")
        f.write(f"  F1 Score:     {metrics['f1_score']:.4f} ({metrics['f1_score']*100:.2f}%)\n\n")

        if per_class:
            f.write("各类别指标 (mAP@0.5 / Precision / Recall):\n")
            for cname, m in per_class.items():
                f.write(f"  {cname:<14} mAP50={m['mAP50']}%  P={m['precision']}%  R={m['recall']}%\n")
            f.write("\n")

    # 混淆矩阵图位置提示（即「预测类别 vs 人工标注类别」对比）
    cm_path = os.path.join(report_dir, model_name, 'confusion_matrix.png')
    if os.path.exists(cm_path):
        print(f"混淆矩阵图已生成: {cm_path}（即「模型预测类别 vs 人工标注类别」对比）")
        with open(txt_report, 'a', encoding='utf-8') as f:
            f.write(f"混淆矩阵图(预测 vs 标注): {cm_path}\n")
    else:
        print(f"[提示] 未找到混淆矩阵图（可能因 ultralytics 版本未生成 plots，"
              f"或请确认 runs/evaluation/{model_name}/ 下是否产出）。")

    print(f"详细报告(TXT)已保存到: {txt_report}")
    return metrics


def predict_samples(model_path, source_dir, out_dir='runs/evaluation/predictions', n=None):
    """
    对验证图片目录推理并保存带预测框的图（「预测 vs 人工标注」对比样本）。
    图中框为模型预测，人工标注见数据集 labels/ 目录。
    """
    if not source_dir or not os.path.isdir(source_dir):
        print(f"[skip] 未提供有效的验证图片目录，跳过预测样本图生成：{source_dir}")
        return
    model = YOLO(model_path)
    model.predict(source=source_dir, save=True, project=out_dir, name='samples',
                  exist_ok=True, imgsz=640, conf=0.2)
    msg = f"预测样本图已保存到: {out_dir}/samples"
    if n:
        msg += f"（取前 {n} 张，图中框为模型预测，人工标注见数据集 labels/）"
    print(msg)


if __name__ == '__main__':
    # ===== 请根据你的环境修改以下路径 =====
    MODEL_PATH = 'runs/train/behavior_detection/weights/best.pt'
    DATA_YAML = 'merged_dataset.yaml'     # 与训练保持一致；其内 path 指向本地数据集根目录
    # 验证图片目录（用于生成「预测 vs 标注」对比样本图）；不需要可置为 None
    VAL_IMAGES_DIR = None                 # 例如 'datasets/merged_dataset/images/val'

    behavior_metrics = evaluate_model(
        model_path=MODEL_PATH,
        data_yaml=DATA_YAML,
        model_name='behavior_detection'
    )

    if VAL_IMAGES_DIR:
        predict_samples(MODEL_PATH, VAL_IMAGES_DIR)

    # 打印评估结果摘要
    print("\n" + "=" * 60)
    print("模型性能")
    print("=" * 60)
    print(f"\n{'指标':<20} {'行为识别':<15}")
    print("-" * 60)
    print(f"{'mAP@0.5':<20} {behavior_metrics['mAP50']*100:<15.2f}%")
    print(f"{'mAP@0.5:0.95':<20} {behavior_metrics['mAP50_95']*100:<15.2f}%")
    print(f"{'Precision':<20} {behavior_metrics['precision']*100:<15.2f}%")
    print(f"{'Recall':<20} {behavior_metrics['recall']*100:<15.2f}%")
    print(f"{'F1 Score':<20} {behavior_metrics['f1_score']*100:<15.2f}%")
    print("=" * 60)

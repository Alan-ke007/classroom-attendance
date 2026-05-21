"""
数据集验证脚本
检查 SCB-Dataset3 和 SCUT-HEAD 数据集是否正确配置
"""

import yaml
from pathlib import Path


def check_dataset(dataset_name, config_file):
    """
    检查数据集配置和结构
    
    Args:
        dataset_name: 数据集名称
        config_file: 配置文件路径
    """
    print("\n" + "=" * 60)
    print(f"检查 {dataset_name} 数据集")
    print("=" * 60)
    
    # 加载配置文件
    config_path = Path(config_file)
    if not config_path.exists():
        print(f"✗ 配置文件不存在: {config_file}")
        return False
    
    with open(config_path, 'r', encoding='utf-8') as f:
        config = yaml.safe_load(f)
    
    print(f"\n配置信息:")
    print(f"  数据集路径: {config.get('path', 'N/A')}")
    print(f"  训练集: {config.get('train', 'N/A')}")
    print(f"  验证集: {config.get('val', 'N/A')}")
    print(f"  类别数量: {config.get('nc', 'N/A')}")
    print(f"  类别名称: {config.get('names', 'N/A')}")
    
    # 检查路径是否存在
    data_path = Path(config['path'])
    if not data_path.exists():
        print(f"\n✗ 错误: 数据集路径不存在: {data_path}")
        print(f"  请修改 {config_file} 中的 path 为正确的绝对路径")
        return False
    
    print(f"\n✓ 数据集根目录存在: {data_path}")
    
    # 检查训练集
    train_img_dir = data_path / config['train']
    train_label_dir = data_path / 'labels' / Path(config['train']).name
    
    if train_img_dir.exists():
        img_files = list(train_img_dir.glob('*.jpg')) + list(train_img_dir.glob('*.png'))
        print(f"✓ 训练集图片目录: {len(img_files)} 张图片")
        
        if train_label_dir.exists():
            label_files = list(train_label_dir.glob('*.txt'))
            print(f"✓ 训练集标注目录: {len(label_files)} 个标注文件")
            
            # 检查图片和标注是否匹配
            img_names = {f.stem for f in img_files}
            label_names = {f.stem for f in label_files}
            
            missing_labels = img_names - label_names
            missing_images = label_names - img_names
            
            if missing_labels:
                print(f"  ⚠ 警告: {len(missing_labels)} 张图片缺少标注")
                if len(missing_labels) <= 5:
                    print(f"    示例: {list(missing_labels)[:5]}")
            
            if missing_images:
                print(f"  ⚠ 警告: {len(missing_images)} 个标注缺少图片")
                
        else:
            print(f"✗ 训练集标注目录不存在: {train_label_dir}")
            return False
    else:
        print(f"✗ 训练集图片目录不存在: {train_img_dir}")
        return False
    
    # 检查验证集
    val_img_dir = data_path / config['val']
    val_label_dir = data_path / 'labels' / Path(config['val']).name
    
    if val_img_dir.exists():
        img_files = list(val_img_dir.glob('*.jpg')) + list(val_img_dir.glob('*.png'))
        print(f"✓ 验证集图片目录: {len(img_files)} 张图片")
        
        if val_label_dir.exists():
            label_files = list(val_label_dir.glob('*.txt'))
            print(f"✓ 验证集标注目录: {len(label_files)} 个标注文件")
        else:
            print(f"✗ 验证集标注目录不存在: {val_label_dir}")
            return False
    else:
        print(f"✗ 验证集图片目录不存在: {val_img_dir}")
        return False
    
    # 检查标注格式
    print(f"\n检查标注格式...")
    sample_label = None
    for label_file in label_files[:1]:  # 检查第一个标注文件
        with open(label_file, 'r') as f:
            lines = f.readlines()
            if lines:
                sample_label = lines[0].strip()
                parts = sample_label.split()
                
                if len(parts) == 5:
                    try:
                        class_id = int(parts[0])
                        coords = [float(x) for x in parts[1:]]
                        
                        if all(0 <= x <= 1 for x in coords):
                            print(f"✓ 标注格式正确 (YOLO 格式)")
                            print(f"  示例: {sample_label}")
                        else:
                            print(f"✗ 标注坐标未归一化 (应该在 0-1 之间)")
                            return False
                    except ValueError:
                        print(f"✗ 标注格式错误: {sample_label}")
                        return False
                else:
                    print(f"✗ 标注格式错误 (应该有5个值): {sample_label}")
                    return False
    
    print(f"\n✓ {dataset_name} 数据集检查通过！")
    return True


def print_next_steps(scb_ok, scut_ok):
    """打印下一步操作建议"""
    print("\n" + "=" * 60)
    print("下一步操作")
    print("=" * 60)
    
    if scb_ok and scut_ok:
        print("\n🎉 所有数据集检查通过！可以开始训练了！\n")
        print("训练命令:")
        print("  cd algorithm-service")
        print("  python train_behavior.py    # 训练行为识别模型")
        print("  python train_face.py        # 训练人脸检测模型")
        print("\n评估命令:")
        print("  python evaluate_model.py    # 评估模型性能")
    else:
        print("\n⚠️  数据集检查存在问题，请先解决上述错误。\n")
        
        if not scb_ok:
            print("SCB-Dataset3 问题排查:")
            print("  1. 确认已下载数据集")
            print("  2. 检查 scb_dataset.yaml 中的 path 是否为正确的绝对路径")
            print("  3. 确认目录结构符合要求 (images/train, images/val, labels/train, labels/val)")
            print("  4. 确认标注文件格式为 YOLO 格式 (.txt)")
            print()
        
        if not scut_ok:
            print("SCUT-HEAD 问题排查:")
            print("  1. 确认已下载 SCUT-HEAD Part A 数据集")
            print("  2. 检查 scut_head.yaml 中的 path 是否为正确的绝对路径")
            print("  3. 如果标注是 XML 格式，需要先转换为 YOLO 格式")
            print("  4. 参考 DATASET_GUIDE.md 中的转换脚本")
            print()


if __name__ == "__main__":
    print("=" * 60)
    print("课堂考勤系统 - 数据集验证工具")
    print("=" * 60)
    
    # 检查 SCB-Dataset3
    scb_ok = check_dataset(
        "SCB-Dataset3",
        "scb_dataset.yaml"
    )
    
    # 检查 SCUT-HEAD
    scut_ok = check_dataset(
        "SCUT-HEAD Part A",
        "scut_head.yaml"
    )
    
    # 打印总结
    print("\n" + "=" * 60)
    print("检查结果总结")
    print("=" * 60)
    print(f"SCB-Dataset3:     {'✓ 通过' if scb_ok else '✗ 失败'}")
    print(f"SCUT-HEAD Part A: {'✓ 通过' if scut_ok else '✗ 失败'}")
    
    # 打印下一步建议
    print_next_steps(scb_ok, scut_ok)

"""
API测试脚本
测试行为检测和人脸检测接口
"""
import requests
import base64
import json
from PIL import Image
import io

# API基础URL
BASE_URL = "http://localhost:5000"

def test_health():
    """测试健康检查接口"""
    print("=" * 60)
    print("测试1: 健康检查")
    print("=" * 60)
    
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"状态码: {response.status_code}")
        print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        print("✓ 健康检查通过\n")
        return True
    except Exception as e:
        print(f"✗ 健康检查失败: {e}\n")
        return False

def test_behavior_detection(image_path=None):
    """测试行为检测接口"""
    print("=" * 60)
    print("测试2: 行为检测")
    print("=" * 60)
    
    try:
        # 如果没有提供图片，创建一个测试图片
        if image_path is None:
            print("使用测试图片...")
            img = Image.new('RGB', (640, 480), color='white')
            buffer = io.BytesIO()
            img.save(buffer, format='JPEG')
            image_base64 = base64.b64encode(buffer.getvalue()).decode('utf-8')
        else:
            # 读取图片并转换为base64
            with open(image_path, 'rb') as f:
                image_base64 = base64.b64encode(f.read()).decode('utf-8')
        
        # 发送请求
        payload = {
            'image': image_base64
        }
        
        response = requests.post(
            f"{BASE_URL}/api/behavior/detect",
            json=payload,
            headers={'Content-Type': 'application/json'}
        )
        
        print(f"状态码: {response.status_code}")
        result = response.json()
        print(f"响应: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
        if result['code'] == 200:
            behaviors = result['data']['behaviors']
            print(f"\n检测到 {len(behaviors)} 个行为:")
            for behavior in behaviors:
                print(f"  - {behavior['description']} (置信度: {behavior['confidence']:.2%})")
            print("✓ 行为检测测试通过\n")
            return True
        else:
            print(f"✗ 行为检测失败: {result['message']}\n")
            return False
            
    except Exception as e:
        print(f"✗ 行为检测异常: {e}\n")
        return False

def test_face_detection(image_path=None):
    """测试人脸检测接口"""
    print("=" * 60)
    print("测试3: 人脸检测")
    print("=" * 60)
    
    try:
        # 如果没有提供图片，创建一个测试图片
        if image_path is None:
            print("使用测试图片...")
            img = Image.new('RGB', (640, 480), color='white')
            buffer = io.BytesIO()
            img.save(buffer, format='JPEG')
            image_base64 = base64.b64encode(buffer.getvalue()).decode('utf-8')
        else:
            # 读取图片并转换为base64
            with open(image_path, 'rb') as f:
                image_base64 = base64.b64encode(f.read()).decode('utf-8')
        
        # 发送请求
        payload = {
            'image': image_base64
        }
        
        response = requests.post(
            f"{BASE_URL}/api/attendance/recognize",
            json=payload,
            headers={'Content-Type': 'application/json'}
        )
        
        print(f"状态码: {response.status_code}")
        result = response.json()
        print(f"响应: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
        if result['code'] == 200:
            faces = result['data']['faces']
            print(f"\n检测到 {len(faces)} 个人脸:")
            for face in faces:
                print(f"  - 置信度: {face['confidence']:.2%}, 位置: {face['boundingBox']}")
            print("✓ 人脸检测测试通过\n")
            return True
        else:
            print(f"✗ 人脸检测失败: {result['message']}\n")
            return False
            
    except Exception as e:
        print(f"✗ 人脸检测异常: {e}\n")
        return False

if __name__ == '__main__':
    print("\n" + "=" * 60)
    print("课堂考勤算法服务 - API测试")
    print("=" * 60 + "\n")
    
    # 运行测试
    results = []
    
    # 测试1: 健康检查
    results.append(("健康检查", test_health()))
    
    # 测试2: 行为检测（使用空白图片）
    results.append(("行为检测", test_behavior_detection()))
    
    # 测试3: 人脸检测（使用空白图片）
    results.append(("人脸检测", test_face_detection()))
    
    # 汇总结果
    print("=" * 60)
    print("测试结果汇总")
    print("=" * 60)
    for test_name, passed in results:
        status = "✓ 通过" if passed else "✗ 失败"
        print(f"{test_name}: {status}")
    
    total = len(results)
    passed = sum(1 for _, p in results if p)
    print(f"\n总计: {passed}/{total} 测试通过")
    print("=" * 60)

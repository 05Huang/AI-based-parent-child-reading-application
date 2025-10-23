#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
环境检查脚本
用于检查MinIO、MySQL、API配置是否正确
"""

import sys
import os

print("=" * 60)
print(" " * 15 + "环境检查工具")
print("=" * 60)
print()

# 检查Python版本
print("[1/5] 检查Python版本...")
if sys.version_info < (3, 7):
    print("  ❌ Python版本过低，需要3.7+")
    sys.exit(1)
print(f"  ✓ Python {sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}")
print()

# 检查依赖包
print("[2/5] 检查依赖包...")
required_packages = {
    'requests': 'requests',
    'pymysql': 'PyMySQL',
    'minio': 'minio'
}

missing_packages = []
for module_name, package_name in required_packages.items():
    try:
        __import__(module_name)
        print(f"  ✓ {package_name}")
    except ImportError:
        print(f"  ❌ {package_name} 未安装")
        missing_packages.append(package_name)

if missing_packages:
    print()
    print(f"  缺少依赖包，请运行: pip install {' '.join(missing_packages)}")
    sys.exit(1)
print()

# 检查MySQL连接
print("[3/5] 检查MySQL连接...")
try:
    import pymysql
    conn = pymysql.connect(
        host='localhost',
        port=3306,
        user='root',
        password='123456',
        database='qz_sns',
        charset='utf8mb4'
    )
    cursor = conn.cursor()
    
    # 检查必要的表是否存在
    cursor.execute("SHOW TABLES LIKE 'content'")
    if cursor.fetchone():
        print("  ✓ MySQL连接成功")
        
        # 检查分类数据
        cursor.execute("SELECT COUNT(*) FROM content_category WHERE status = 1")
        category_count = cursor.fetchone()[0]
        print(f"  ✓ 找到 {category_count} 个分类")
    else:
        print("  ⚠️  MySQL连接成功，但未找到content表，请先导入数据库结构")
    
    cursor.close()
    conn.close()
except Exception as e:
    print(f"  ❌ MySQL连接失败: {e}")
    print("  提示: 请检查MySQL服务是否启动，用户名密码是否正确")
print()

# 检查MinIO连接
print("[4/5] 检查MinIO连接...")
try:
    from minio import Minio
    from minio.error import S3Error
    
    client = Minio(
        '127.0.0.1:9000',
        access_key='root',
        secret_key='12345678',
        secure=False
    )
    
    # 检查bucket
    if client.bucket_exists('qz-sns'):
        print("  ✓ MinIO连接成功")
        print("  ✓ Bucket 'qz-sns' 已存在")
    else:
        print("  ✓ MinIO连接成功")
        print("  ⚠️  Bucket 'qz-sns' 不存在，脚本运行时会自动创建")
        
except Exception as e:
    print(f"  ❌ MinIO连接失败: {e}")
    print("  提示: 请检查MinIO服务是否启动 (http://127.0.0.1:9000)")
print()

# 检查API配置
print("[5/5] 检查API配置...")
import json

api_configured = False

# 检查环境变量
unsplash_key = os.getenv('UNSPLASH_ACCESS_KEY')
pexels_key = os.getenv('PEXELS_API_KEY')

if unsplash_key:
    print(f"  ✓ 环境变量 UNSPLASH_ACCESS_KEY 已配置")
    api_configured = True
else:
    print(f"  ⚠️  环境变量 UNSPLASH_ACCESS_KEY 未配置")

if pexels_key:
    print(f"  ✓ 环境变量 PEXELS_API_KEY 已配置")
    api_configured = True
else:
    print(f"  ⚠️  环境变量 PEXELS_API_KEY 未配置")

# 检查配置文件
if os.path.exists('config.json'):
    try:
        with open('config.json', 'r', encoding='utf-8') as f:
            config = json.load(f)
            api_keys = config.get('api_keys', {})
            
            if api_keys.get('unsplash'):
                print(f"  ✓ config.json中配置了Unsplash API Key")
                api_configured = True
            
            if api_keys.get('pexels'):
                print(f"  ✓ config.json中配置了Pexels API Key")
                api_configured = True
    except:
        pass
else:
    print(f"  ℹ️  未找到config.json配置文件")

if not api_configured:
    print()
    print("  提示: 建议配置API密钥以获取更多素材")
    print("  - Unsplash: https://unsplash.com/developers")
    print("  - Pexels: https://www.pexels.com/api/")
print()

print("=" * 60)
print(" " * 15 + "✓ 环境检查完成")
print("=" * 60)
print()
print("如果所有检查都通过，你可以运行:")
print("  python generate_test_content.py")
print()


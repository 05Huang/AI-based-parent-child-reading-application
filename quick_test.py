#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
快速测试脚本
自动生成少量测试数据（5篇文章、2个视频），用于验证环境配置
"""

import sys
import os

# 添加当前目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from generate_test_content import TestContentGenerator, load_config
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def main():
    print("=" * 70)
    print(" " * 20 + "快速测试模式")
    print("=" * 70)
    print("\n本脚本将自动生成少量测试数据：")
    print("  - 5篇图文文章（每篇2张图片）")
    print("  - 2个视频内容")
    print("\n预计耗时：5-10分钟")
    print("=" * 70)
    
    confirm = input("\n是否继续？(y/n): ").strip().lower()
    if confirm != 'y':
        print("已取消")
        return
    
    print("\n开始生成测试数据...\n")
    
    try:
        # 加载配置
        config = load_config()
        
        # 创建生成器
        generator = TestContentGenerator(config)
        
        # 生成5篇文章
        logger.info("生成文章...")
        generator.generate_articles(count=5, images_per_article=2)
        
        # 生成2个视频
        logger.info("生成视频...")
        generator.generate_videos(count=2)
        
        # 关闭连接
        generator.close()
        
        print("\n" + "=" * 70)
        print(" " * 20 + "✓ 测试数据生成完成！")
        print("=" * 70)
        print("\n你可以通过以下方式查看生成的数据：")
        print("1. MinIO管理界面: http://127.0.0.1:9000")
        print("2. 数据库查询: SELECT * FROM content ORDER BY id DESC LIMIT 10;")
        print("\n如需生成更多数据，请运行: python generate_test_content.py")
        
    except Exception as e:
        logger.error(f"测试数据生成失败: {e}")
        import traceback
        traceback.print_exc()
        print("\n请检查：")
        print("1. MinIO服务是否启动")
        print("2. MySQL服务是否启动")
        print("3. 运行 python check_environment.py 检查环境")

if __name__ == '__main__':
    main()


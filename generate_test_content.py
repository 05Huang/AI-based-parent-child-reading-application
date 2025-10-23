#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
亲子阅读APP测试数据生成脚本
功能：
1. 从开源平台爬取图片和视频
2. 上传到MinIO
3. 生成测试文章数据并插入MySQL数据库
"""

import os
import random
import time
import requests
import pymysql
from datetime import datetime, timedelta
from minio import Minio
from minio.error import S3Error
from io import BytesIO
import json
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class TestContentGenerator:
    """测试内容生成器"""
    
    def __init__(self, config):
        """初始化配置"""
        self.config = config
        
        # MinIO客户端
        logger.info(f"正在连接MinIO服务器: {config['minio']['endpoint']}")
        self.minio_client = Minio(
            config['minio']['endpoint'].replace('http://', '').replace('https://', ''),
            access_key=config['minio']['access_key'],
            secret_key=config['minio']['secret_key'],
            secure=False
        )
        
        # 检查并创建bucket
        self._ensure_bucket_exists()
        
        # MySQL连接
        logger.info(f"正在连接MySQL数据库: {config['mysql']['host']}:{config['mysql']['port']}")
        self.db_connection = pymysql.connect(
            host=config['mysql']['host'],
            port=config['mysql']['port'],
            user=config['mysql']['user'],
            password=config['mysql']['password'],
            database=config['mysql']['database'],
            charset='utf8mb4'
        )
        self.cursor = self.db_connection.cursor()
        
        # API密钥（从配置读取或环境变量）
        self.unsplash_key = config.get('api_keys', {}).get('unsplash', os.getenv('UNSPLASH_ACCESS_KEY'))
        self.pexels_key = config.get('api_keys', {}).get('pexels', os.getenv('PEXELS_API_KEY'))
        
        logger.info("初始化完成！")
    
    def _ensure_bucket_exists(self):
        """确保MinIO bucket存在"""
        bucket_name = self.config['minio']['bucket_name']
        try:
            if not self.minio_client.bucket_exists(bucket_name):
                logger.info(f"创建MinIO bucket: {bucket_name}")
                self.minio_client.make_bucket(bucket_name)
                logger.info(f"Bucket {bucket_name} 创建成功")
            else:
                logger.info(f"Bucket {bucket_name} 已存在")
        except S3Error as e:
            logger.error(f"检查/创建bucket失败: {e}")
            raise
    
    def fetch_unsplash_images(self, count=10, query='children reading', width=1920, height=1080):
        """从Unsplash获取图片"""
        if not self.unsplash_key:
            logger.warning("未配置Unsplash API Key，跳过Unsplash图片获取")
            return []
        
        logger.info(f"开始从Unsplash获取{count}张图片，关键词：{query}")
        images = []
        
        try:
            url = "https://api.unsplash.com/search/photos"
            headers = {"Authorization": f"Client-ID {self.unsplash_key}"}
            
            page = 1
            while len(images) < count:
                params = {
                    "query": query,
                    "page": page,
                    "per_page": min(30, count - len(images)),
                    "orientation": "landscape"
                }
                
                logger.info(f"正在请求Unsplash API - 页码: {page}")
                response = requests.get(url, headers=headers, params=params, timeout=10)
                
                if response.status_code == 200:
                    data = response.json()
                    results = data.get('results', [])
                    
                    if not results:
                        logger.warning(f"Unsplash返回空结果，已获取{len(images)}张图片")
                        break
                    
                    for img in results:
                        download_url = img['urls'].get('raw', img['urls']['full'])
                        # 添加尺寸参数
                        download_url = f"{download_url}&w={width}&h={height}&fit=crop"
                        
                        images.append({
                            'url': download_url,
                            'description': img.get('description', img.get('alt_description', 'Beautiful image')),
                            'author': img['user']['name'],
                            'source': 'Unsplash'
                        })
                        
                        if len(images) >= count:
                            break
                    
                    page += 1
                    time.sleep(0.5)  # 避免请求过快
                else:
                    logger.error(f"Unsplash API请求失败: {response.status_code} - {response.text}")
                    break
                    
        except Exception as e:
            logger.error(f"从Unsplash获取图片失败: {e}")
        
        logger.info(f"从Unsplash成功获取{len(images)}张图片")
        return images
    
    def fetch_pexels_images(self, count=10, query='family reading', width=1920, height=1080):
        """从Pexels获取图片"""
        if not self.pexels_key:
            logger.warning("未配置Pexels API Key，跳过Pexels图片获取")
            return []
        
        logger.info(f"开始从Pexels获取{count}张图片，关键词：{query}")
        images = []
        
        try:
            url = "https://api.pexels.com/v1/search"
            headers = {"Authorization": self.pexels_key}
            
            page = 1
            while len(images) < count:
                params = {
                    "query": query,
                    "page": page,
                    "per_page": min(80, count - len(images)),
                    "orientation": "landscape"
                }
                
                logger.info(f"正在请求Pexels API - 页码: {page}")
                response = requests.get(url, headers=headers, params=params, timeout=10)
                
                if response.status_code == 200:
                    data = response.json()
                    photos = data.get('photos', [])
                    
                    if not photos:
                        logger.warning(f"Pexels返回空结果，已获取{len(images)}张图片")
                        break
                    
                    for photo in photos:
                        # 选择合适的尺寸
                        src = photo['src']
                        download_url = src.get('original', src.get('large2x', src['large']))
                        
                        images.append({
                            'url': download_url,
                            'description': photo.get('alt', 'Beautiful image'),
                            'author': photo['photographer'],
                            'source': 'Pexels'
                        })
                        
                        if len(images) >= count:
                            break
                    
                    page += 1
                    time.sleep(0.5)
                else:
                    logger.error(f"Pexels API请求失败: {response.status_code} - {response.text}")
                    break
                    
        except Exception as e:
            logger.error(f"从Pexels获取图片失败: {e}")
        
        logger.info(f"从Pexels成功获取{len(images)}张图片")
        return images
    
    def fetch_pexels_videos(self, count=5, query='children learning', min_width=1280, min_height=720):
        """从Pexels获取视频"""
        if not self.pexels_key:
            logger.warning("未配置Pexels API Key，跳过Pexels视频获取")
            return []
        
        logger.info(f"开始从Pexels获取{count}个视频，关键词：{query}")
        videos = []
        
        try:
            url = "https://api.pexels.com/videos/search"
            headers = {"Authorization": self.pexels_key}
            
            page = 1
            while len(videos) < count:
                params = {
                    "query": query,
                    "page": page,
                    "per_page": min(80, count - len(videos)),
                    "orientation": "landscape"
                }
                
                logger.info(f"正在请求Pexels Videos API - 页码: {page}")
                response = requests.get(url, headers=headers, params=params, timeout=10)
                
                if response.status_code == 200:
                    data = response.json()
                    video_list = data.get('videos', [])
                    
                    if not video_list:
                        logger.warning(f"Pexels返回空视频结果，已获取{len(videos)}个视频")
                        break
                    
                    for video in video_list:
                        # 选择合适的视频质量
                        video_files = video.get('video_files', [])
                        if not video_files:
                            continue
                        
                        # 优先选择HD或更高质量
                        selected_file = None
                        for vf in video_files:
                            if vf.get('width', 0) >= min_width and vf.get('height', 0) >= min_height:
                                selected_file = vf
                                break
                        
                        if not selected_file and video_files:
                            selected_file = video_files[0]
                        
                        if selected_file:
                            # 获取视频封面
                            cover_url = video.get('image', '')
                            
                            videos.append({
                                'video_url': selected_file['link'],
                                'cover_url': cover_url,
                                'description': video.get('url', 'Educational video'),
                                'author': video.get('user', {}).get('name', 'Unknown'),
                                'duration': video.get('duration', 0),
                                'width': selected_file.get('width', 1920),
                                'height': selected_file.get('height', 1080),
                                'source': 'Pexels'
                            })
                            
                            if len(videos) >= count:
                                break
                    
                    page += 1
                    time.sleep(0.5)
                else:
                    logger.error(f"Pexels Videos API请求失败: {response.status_code} - {response.text}")
                    break
                    
        except Exception as e:
            logger.error(f"从Pexels获取视频失败: {e}")
        
        logger.info(f"从Pexels成功获取{len(videos)}个视频")
        return videos
    
    def download_and_upload_image(self, image_info, content_id, image_type='cover'):
        """下载图片并上传到MinIO"""
        try:
            logger.info(f"正在下载图片: {image_info['url'][:100]}...")
            response = requests.get(image_info['url'], timeout=30, stream=True)
            
            if response.status_code == 200:
                # 读取图片数据
                image_data = BytesIO(response.content)
                content_length = len(response.content)
                
                # 构建MinIO路径
                if image_type == 'cover':
                    object_name = f"image/article/{content_id}/article_cover.jpg"
                else:
                    object_name = f"image/article/{content_id}/article_image_{image_type}.jpg"
                
                logger.info(f"正在上传图片到MinIO: {object_name}")
                
                # 上传到MinIO
                self.minio_client.put_object(
                    bucket_name=self.config['minio']['bucket_name'],
                    object_name=object_name,
                    data=image_data,
                    length=content_length,
                    content_type='image/jpeg'
                )
                
                # 生成访问URL
                url = f"{self.config['minio']['domain']}/{self.config['minio']['bucket_name']}/{object_name}"
                logger.info(f"图片上传成功: {url}")
                return url
            else:
                logger.error(f"下载图片失败: HTTP {response.status_code}")
                return None
                
        except Exception as e:
            logger.error(f"下载/上传图片失败: {e}")
            return None
    
    def download_and_upload_video(self, video_info, content_id):
        """下载视频及封面并上传到MinIO"""
        urls = {}
        
        try:
            # 下载并上传视频封面
            if video_info.get('cover_url'):
                logger.info(f"正在下载视频封面: {video_info['cover_url'][:100]}...")
                response = requests.get(video_info['cover_url'], timeout=30, stream=True)
                
                if response.status_code == 200:
                    image_data = BytesIO(response.content)
                    content_length = len(response.content)
                    
                    object_name = f"video/resource/{content_id}/video_cover.jpg"
                    
                    logger.info(f"正在上传视频封面到MinIO: {object_name}")
                    self.minio_client.put_object(
                        bucket_name=self.config['minio']['bucket_name'],
                        object_name=object_name,
                        data=image_data,
                        length=content_length,
                        content_type='image/jpeg'
                    )
                    
                    urls['cover_url'] = f"{self.config['minio']['domain']}/{self.config['minio']['bucket_name']}/{object_name}"
                    logger.info(f"视频封面上传成功: {urls['cover_url']}")
            
            # 下载并上传视频文件
            logger.info(f"正在下载视频文件: {video_info['video_url'][:100]}...")
            logger.info(f"视频大小可能较大，请耐心等待...")
            
            response = requests.get(video_info['video_url'], timeout=120, stream=True)
            
            if response.status_code == 200:
                video_data = BytesIO()
                downloaded = 0
                
                # 分块下载
                for chunk in response.iter_content(chunk_size=1024*1024):  # 1MB chunks
                    if chunk:
                        video_data.write(chunk)
                        downloaded += len(chunk)
                        if downloaded % (5*1024*1024) == 0:  # 每5MB打印一次
                            logger.info(f"已下载 {downloaded / (1024*1024):.2f} MB")
                
                video_data.seek(0)
                content_length = len(video_data.getvalue())
                
                logger.info(f"视频下载完成，总大小: {content_length / (1024*1024):.2f} MB")
                
                object_name = f"video/resource/{content_id}/video.mp4"
                
                logger.info(f"正在上传视频到MinIO: {object_name}")
                self.minio_client.put_object(
                    bucket_name=self.config['minio']['bucket_name'],
                    object_name=object_name,
                    data=video_data,
                    length=content_length,
                    content_type='video/mp4'
                )
                
                urls['media_url'] = f"{self.config['minio']['domain']}/{self.config['minio']['bucket_name']}/{object_name}"
                logger.info(f"视频上传成功: {urls['media_url']}")
            
            return urls
            
        except Exception as e:
            logger.error(f"下载/上传视频失败: {e}")
            return urls
    
    def generate_article_content(self, title, images=[]):
        """生成文章富文本内容"""
        # 文章段落模板
        paragraphs = [
            f"<h2>{title}</h2>",
            "<p>在这个快节奏的时代，亲子阅读成为了家庭教育中不可或缺的一环。通过阅读，孩子们不仅能够学习知识，更能与父母建立深厚的情感纽带。</p>",
            "<p>研究表明，从小培养孩子的阅读习惯，对其语言能力、思维能力和想象力的发展都有着深远的影响。每天坚持半小时的亲子阅读时间，将成为孩子一生的财富。</p>",
            "<p>选择适合孩子年龄段的读物至关重要。对于学龄前儿童，绘本是最好的选择；对于小学生，可以逐步引入儿童文学作品；而对于青少年，则可以尝试更加多元化的阅读材料。</p>",
            "<p>在阅读过程中，家长的陪伴和引导起着关键作用。不仅要读给孩子听，更要鼓励孩子表达自己的想法和感受，培养其独立思考的能力。</p>",
            "<p>营造良好的阅读氛围同样重要。一个安静舒适的阅读角落，充足的光线，以及丰富的图书资源，都能激发孩子的阅读兴趣。</p>",
            "<p>让我们一起行动起来，用阅读点亮孩子的未来，用书香浸润家庭的每一个角落。从今天开始，每天陪孩子读一本好书吧！</p>"
        ]
        
        # 随机选择段落
        selected_paragraphs = random.sample(paragraphs[1:], random.randint(3, len(paragraphs)-1))
        selected_paragraphs.insert(0, paragraphs[0])  # 标题始终在开头
        
        content_html = ""
        
        # 组合段落和图片
        for i, para in enumerate(selected_paragraphs):
            content_html += para + "\n"
            
            # 在某些段落后插入图片
            if i > 0 and i < len(selected_paragraphs) - 1 and images and i % 2 == 0:
                img_idx = (i // 2) % len(images)
                if img_idx < len(images):
                    content_html += f'<p><img src="{images[img_idx]}" alt="配图" style="max-width:100%;height:auto;"/></p>\n'
        
        return content_html
    
    def generate_title(self, category_name):
        """根据分类生成标题"""
        title_templates = {
            '科技前沿': [
                '探索未来：{topic}的最新发展',
                '科技改变生活：{topic}的创新应用',
                '{topic}：开启智能新时代',
                '深度解析：{topic}背后的技术原理'
            ],
            '教育': [
                '{topic}：培养孩子的关键能力',
                '家庭教育指南：如何引导孩子{topic}',
                '{topic}在儿童成长中的重要性',
                '亲子共读：{topic}主题书单推荐'
            ],
            '旅游': [
                '亲子旅行推荐：{topic}的美好时光',
                '带孩子去{topic}：难忘的家庭旅行',
                '{topic}旅游攻略：适合全家出游',
                '探索世界：{topic}的人文风景'
            ],
            '美食': [
                '营养美味：{topic}的家庭做法',
                '亲子厨房：和孩子一起做{topic}',
                '健康饮食：{topic}的营养价值',
                '{topic}食谱：简单又美味'
            ],
            '教育培训': [
                '{topic}课程：激发孩子潜能',
                '在线学习：{topic}技能提升指南',
                '{topic}培训：让孩子赢在起跑线',
                '兴趣培养：{topic}入门教程'
            ]
        }
        
        topics = [
            '阅读习惯', '思维训练', '创造力', '艺术启蒙', '科学实验',
            '数学思维', '英语启蒙', '音乐欣赏', '户外探索', '手工制作',
            '编程思维', '情商培养', '专注力', '记忆力', '表达能力'
        ]
        
        # 如果分类不在模板中，使用通用模板
        if category_name not in title_templates:
            category_name = random.choice(list(title_templates.keys()))
        
        template = random.choice(title_templates[category_name])
        topic = random.choice(topics)
        
        return template.format(topic=topic)
    
    def generate_tags(self, category_name):
        """根据分类生成标签"""
        tag_pool = {
            '科技前沿': ['人工智能', '编程教育', '科技创新', '未来教育', '智能设备'],
            '教育': ['亲子阅读', '家庭教育', '儿童成长', '学习方法', '阅读习惯'],
            '旅游': ['亲子旅行', '家庭出游', '旅行攻略', '文化体验', '自然探索'],
            '美食': ['儿童营养', '健康饮食', '亲子厨房', '美食制作', '食谱分享'],
            '教育培训': ['在线教育', '技能培训', '兴趣培养', '课程推荐', '学习资源'],
            '科技资讯': ['科技动态', '产品评测', '行业资讯', '技术趋势', '创新科技'],
            '升学': ['升学指导', '考试技巧', '院校选择', '学业规划', '教育资讯'],
            '在线学习': ['网课推荐', '学习平台', '教学资源', '远程教育', '自主学习'],
            '技能提升': ['能力培养', '技能学习', '个人成长', '职业发展', '综合素质']
        }
        
        # 通用标签
        common_tags = ['亲子', '教育', '成长', '学习', '家庭']
        
        # 获取分类特定标签
        category_tags = tag_pool.get(category_name, common_tags)
        
        # 随机选择3-5个标签
        selected = random.sample(category_tags, min(random.randint(3, 5), len(category_tags)))
        
        # 可能添加一个通用标签
        if random.random() > 0.5:
            selected.append(random.choice(common_tags))
        
        return ','.join(selected)
    
    def get_categories(self):
        """获取数据库中的分类列表"""
        try:
            self.cursor.execute("SELECT id, name FROM content_category WHERE status = 1")
            categories = self.cursor.fetchall()
            logger.info(f"获取到{len(categories)}个分类")
            return [(cat[0], cat[1]) for cat in categories]
        except Exception as e:
            logger.error(f"获取分类失败: {e}")
            return []
    
    def insert_article_content(self, title, content, category_id, tags, cover_url, 
                              content_images=[], creator_id=-999):
        """插入图文内容到数据库"""
        try:
            # 计算内容长度（字数）
            import re
            text_only = re.sub(r'<[^>]+>', '', content)
            content_length = len(text_only.strip())
            
            # 生成随机的统计数据
            view_count = random.randint(100, 5000)
            like_count = random.randint(10, int(view_count * 0.2))
            comment_count = random.randint(5, int(view_count * 0.1))
            share_count = random.randint(0, int(view_count * 0.05))
            
            # 计算推荐相关指标
            click_through_rate = round(random.uniform(0.05, 0.25), 4)
            completion_rate = round(random.uniform(0.4, 0.9), 4)
            popularity_score = round(
                (like_count * 2 + comment_count * 3 + share_count * 5) / max(view_count, 1),
                4
            )
            recommendation_score = round(
                (click_through_rate * 0.3 + completion_rate * 0.3 + popularity_score * 0.4),
                4
            )
            
            # 随机生成最后推荐时间（最近7天内）
            last_recommended = datetime.now() - timedelta(days=random.randint(0, 7))
            
            sql = """
            INSERT INTO content (
                title, content, type, cover_url, category_id, tags,
                view_count, like_count, comment_count, share_count,
                status, creator_id, click_through_rate, completion_rate,
                content_length, last_recommended, recommendation_score,
                popularity_score, recommended_count
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
            """
            
            values = (
                title, content, 1, cover_url, category_id, tags,
                view_count, like_count, comment_count, share_count,
                1, creator_id, click_through_rate, completion_rate,
                content_length, last_recommended, recommendation_score,
                popularity_score, random.randint(10, 500)
            )
            
            logger.info(f"正在插入文章: {title}")
            self.cursor.execute(sql, values)
            content_id = self.cursor.lastrowid
            
            # 插入文章图片关联
            if content_images:
                for idx, img_url in enumerate(content_images):
                    img_sql = """
                    INSERT INTO content_image (content_id, image_url, sort_order)
                    VALUES (%s, %s, %s)
                    """
                    self.cursor.execute(img_sql, (content_id, img_url, idx))
                logger.info(f"插入了{len(content_images)}张文章图片")
            
            self.db_connection.commit()
            logger.info(f"文章插入成功，ID: {content_id}")
            return content_id
            
        except Exception as e:
            self.db_connection.rollback()
            logger.error(f"插入文章失败: {e}")
            return None
    
    def insert_video_content(self, title, description, category_id, tags, 
                           cover_url, media_url, duration, creator_id=-999):
        """插入视频内容到数据库"""
        try:
            # 生成随机的统计数据
            view_count = random.randint(200, 10000)
            like_count = random.randint(20, int(view_count * 0.25))
            comment_count = random.randint(10, int(view_count * 0.15))
            share_count = random.randint(5, int(view_count * 0.08))
            
            # 计算推荐相关指标
            click_through_rate = round(random.uniform(0.08, 0.35), 4)
            avg_view_duration = int(duration * random.uniform(0.5, 0.95))
            completion_rate = round(avg_view_duration / max(duration, 1), 4)
            popularity_score = round(
                (like_count * 2 + comment_count * 3 + share_count * 5) / max(view_count, 1),
                4
            )
            recommendation_score = round(
                (click_through_rate * 0.3 + completion_rate * 0.4 + popularity_score * 0.3),
                4
            )
            
            # 随机生成最后推荐时间
            last_recommended = datetime.now() - timedelta(days=random.randint(0, 7))
            
            sql = """
            INSERT INTO content (
                title, content, type, cover_url, media_url, category_id, tags,
                view_count, like_count, comment_count, share_count,
                status, creator_id, click_through_rate, avg_view_duration,
                completion_rate, content_length, last_recommended,
                recommendation_score, popularity_score, recommended_count
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
            """
            
            values = (
                title, description, 2, cover_url, media_url, category_id, tags,
                view_count, like_count, comment_count, share_count,
                1, creator_id, click_through_rate, avg_view_duration,
                completion_rate, duration, last_recommended,
                recommendation_score, popularity_score, random.randint(20, 800)
            )
            
            logger.info(f"正在插入视频: {title}")
            self.cursor.execute(sql, values)
            content_id = self.cursor.lastrowid
            
            self.db_connection.commit()
            logger.info(f"视频插入成功，ID: {content_id}")
            return content_id
            
        except Exception as e:
            self.db_connection.rollback()
            logger.error(f"插入视频失败: {e}")
            return None
    
    def generate_articles(self, count=10, images_per_article=2):
        """生成图文文章"""
        logger.info(f"=" * 60)
        logger.info(f"开始生成{count}篇图文文章")
        logger.info(f"=" * 60)
        
        categories = self.get_categories()
        if not categories:
            logger.error("未找到可用分类，无法生成文章")
            return
        
        # 获取图片资源
        total_images_needed = count * (images_per_article + 1)  # +1 for cover
        logger.info(f"需要获取约{total_images_needed}张图片")
        
        # 从多个源获取图片
        all_images = []
        
        # 亲子阅读相关关键词
        keywords = [
            'children reading', 'family reading', 'kids learning',
            'education', 'books children', 'parent child',
            'story time', 'library kids', 'study children'
        ]
        
        for keyword in keywords[:3]:  # 使用前3个关键词
            if len(all_images) >= total_images_needed:
                break
            
            # 从Unsplash获取
            images = self.fetch_unsplash_images(
                count=total_images_needed // 3,
                query=keyword
            )
            all_images.extend(images)
            
            # 从Pexels获取
            images = self.fetch_pexels_images(
                count=total_images_needed // 3,
                query=keyword
            )
            all_images.extend(images)
        
        logger.info(f"总共获取了{len(all_images)}张图片")
        
        if len(all_images) < count:
            logger.warning(f"图片数量不足，仅能生成{len(all_images)}篇文章")
            count = len(all_images)
        
        # 生成文章
        success_count = 0
        for i in range(count):
            try:
                logger.info(f"\n--- 生成第{i+1}/{count}篇文章 ---")
                
                # 随机选择分类
                category_id, category_name = random.choice(categories)
                
                # 生成标题和标签
                title = self.generate_title(category_name)
                tags = self.generate_tags(category_name)
                
                # 为这篇文章分配图片
                start_idx = i * (images_per_article + 1)
                article_images_info = all_images[start_idx:start_idx + images_per_article + 1]
                
                if not article_images_info:
                    logger.warning(f"第{i+1}篇文章没有可用图片，跳过")
                    continue
                
                # 预先生成content_id（用于构建MinIO路径）
                # 获取当前最大ID
                self.cursor.execute("SELECT MAX(id) FROM content")
                result = self.cursor.fetchone()
                next_id = (result[0] or 0) + 1
                
                # 上传封面图
                cover_info = article_images_info[0]
                cover_url = self.download_and_upload_image(cover_info, next_id, 'cover')
                
                if not cover_url:
                    logger.warning(f"第{i+1}篇文章封面上传失败，跳过")
                    continue
                
                # 上传文章内容图片
                content_image_urls = []
                for idx, img_info in enumerate(article_images_info[1:], start=1):
                    img_url = self.download_and_upload_image(img_info, next_id, idx)
                    if img_url:
                        content_image_urls.append(img_url)
                
                # 生成文章内容
                content_html = self.generate_article_content(title, content_image_urls)
                
                # 插入数据库
                content_id = self.insert_article_content(
                    title=title,
                    content=content_html,
                    category_id=category_id,
                    tags=tags,
                    cover_url=cover_url,
                    content_images=content_image_urls
                )
                
                if content_id:
                    success_count += 1
                    logger.info(f"✓ 第{i+1}篇文章生成成功")
                
                # 避免请求过快
                time.sleep(1)
                
            except Exception as e:
                logger.error(f"生成第{i+1}篇文章失败: {e}")
                continue
        
        logger.info(f"\n" + "=" * 60)
        logger.info(f"文章生成完成！成功: {success_count}/{count}")
        logger.info(f"=" * 60)
    
    def generate_videos(self, count=5):
        """生成视频内容"""
        logger.info(f"=" * 60)
        logger.info(f"开始生成{count}个视频内容")
        logger.info(f"=" * 60)
        
        categories = self.get_categories()
        if not categories:
            logger.error("未找到可用分类，无法生成视频")
            return
        
        # 教育相关关键词
        keywords = [
            'children learning', 'kids education', 'family activities',
            'children playing', 'learning fun', 'educational activities'
        ]
        
        # 获取视频资源
        all_videos = []
        for keyword in keywords:
            if len(all_videos) >= count:
                break
            
            videos = self.fetch_pexels_videos(
                count=count - len(all_videos),
                query=keyword
            )
            all_videos.extend(videos)
        
        logger.info(f"总共获取了{len(all_videos)}个视频")
        
        if len(all_videos) < count:
            logger.warning(f"视频数量不足，仅能生成{len(all_videos)}个视频内容")
            count = len(all_videos)
        
        # 生成视频内容
        success_count = 0
        for i in range(count):
            try:
                logger.info(f"\n--- 生成第{i+1}/{count}个视频 ---")
                
                # 随机选择分类
                category_id, category_name = random.choice(categories)
                
                # 生成标题和标签
                title = self.generate_title(category_name) + " - 视频教程"
                tags = self.generate_tags(category_name)
                description = f"<p>这是一个关于{category_name}的精彩视频内容，适合家长和孩子一起观看学习。</p>"
                
                video_info = all_videos[i]
                
                # 预先生成content_id
                self.cursor.execute("SELECT MAX(id) FROM content")
                result = self.cursor.fetchone()
                next_id = (result[0] or 0) + 1
                
                # 下载并上传视频及封面
                urls = self.download_and_upload_video(video_info, next_id)
                
                if not urls.get('media_url'):
                    logger.warning(f"第{i+1}个视频上传失败，跳过")
                    continue
                
                # 插入数据库
                content_id = self.insert_video_content(
                    title=title,
                    description=description,
                    category_id=category_id,
                    tags=tags,
                    cover_url=urls.get('cover_url', ''),
                    media_url=urls['media_url'],
                    duration=video_info.get('duration', 60)
                )
                
                if content_id:
                    success_count += 1
                    logger.info(f"✓ 第{i+1}个视频生成成功")
                
                # 避免请求过快
                time.sleep(2)
                
            except Exception as e:
                logger.error(f"生成第{i+1}个视频失败: {e}")
                continue
        
        logger.info(f"\n" + "=" * 60)
        logger.info(f"视频生成完成！成功: {success_count}/{count}")
        logger.info(f"=" * 60)
    
    def close(self):
        """关闭数据库连接"""
        if self.cursor:
            self.cursor.close()
        if self.db_connection:
            self.db_connection.close()
        logger.info("数据库连接已关闭")


def load_config(config_file='config.json'):
    """加载配置文件"""
    # 默认配置
    default_config = {
        'minio': {
            'endpoint': 'http://127.0.0.1:9000',
            'domain': 'http://127.0.0.1:9000',
            'access_key': 'root',
            'secret_key': '12345678',
            'bucket_name': 'qz-sns'
        },
        'mysql': {
            'host': 'localhost',
            'port': 3306,
            'user': 'root',
            'password': '123456',
            'database': 'qz_sns'
        },
        'api_keys': {
            'unsplash': '',
            'pexels': ''
        }
    }
    
    # 尝试加载配置文件
    if os.path.exists(config_file):
        try:
            with open(config_file, 'r', encoding='utf-8') as f:
                file_config = json.load(f)
                # 合并配置
                for key in file_config:
                    if key in default_config and isinstance(default_config[key], dict):
                        default_config[key].update(file_config[key])
                    else:
                        default_config[key] = file_config[key]
            logger.info(f"已加载配置文件: {config_file}")
        except Exception as e:
            logger.warning(f"加载配置文件失败，使用默认配置: {e}")
    else:
        logger.info("未找到配置文件，使用默认配置")
    
    return default_config


def main():
    """主函数"""
    # 加载配置
    config = load_config()
    
    print("=" * 70)
    print(" " * 15 + "亲子阅读APP测试数据生成工具")
    print("=" * 70)
    print("\n⚠️  注意事项：")
    print("1. 请确保MinIO服务已启动（http://127.0.0.1:9000）")
    print("2. 请确保MySQL数据库已启动并导入了数据库表结构")
    print("3. 建议配置Unsplash和Pexels的API密钥以获取更多素材")
    print("4. 视频文件较大，下载和上传需要较长时间，请耐心等待")
    print("5. 本脚本仅用于测试，请遵守API使用条款和版权规定")
    print("\n💡 提示：")
    print("- 首次使用建议生成少量数据（5篇文章、2个视频）")
    print("- 如未配置API密钥，可能获取的素材数量有限")
    print("- 可以创建config.json文件配置API密钥和数据库连接")
    print("- 查看详细说明: 测试数据生成工具使用说明.txt")
    print("\n" + "=" * 70)
    
    # 获取用户输入
    try:
        article_count = input("\n请输入要生成的文章数量（默认10）: ").strip()
        article_count = int(article_count) if article_count else 10
        
        images_per_article = input("请输入每篇文章的图片数量（默认2）: ").strip()
        images_per_article = int(images_per_article) if images_per_article else 2
        
        video_count = input("请输入要生成的视频数量（默认3）: ").strip()
        video_count = int(video_count) if video_count else 3
        
        print("\n" + "=" * 70)
        print(f"配置确认：")
        print(f"  - 文章数量: {article_count}")
        print(f"  - 每篇文章图片数: {images_per_article}")
        print(f"  - 视频数量: {video_count}")
        print("=" * 70)
        
        confirm = input("\n确认开始生成？(y/n): ").strip().lower()
        if confirm != 'y':
            print("已取消")
            return
        
    except ValueError:
        print("输入无效，使用默认配置")
        article_count = 10
        images_per_article = 2
        video_count = 3
    except KeyboardInterrupt:
        print("\n\n已取消")
        return
    
    # 创建生成器
    try:
        generator = TestContentGenerator(config)
        
        # 生成文章
        if article_count > 0:
            generator.generate_articles(
                count=article_count,
                images_per_article=images_per_article
            )
        
        # 生成视频
        if video_count > 0:
            generator.generate_videos(count=video_count)
        
        generator.close()
        
        print("\n" + "=" * 70)
        print(" " * 20 + "✓ 全部任务完成！")
        print("=" * 70)
        print("\n提示：你可以登录系统查看生成的测试内容")
        
    except Exception as e:
        logger.error(f"程序执行失败: {e}")
        import traceback
        traceback.print_exc()


if __name__ == '__main__':
    main()


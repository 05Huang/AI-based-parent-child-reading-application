from flask import Flask, request, jsonify
import jieba
import jieba.analyse
from bs4 import BeautifulSoup
import numpy as np
import re
import logging
from typing import List, Dict, Tuple
import pymysql
from datetime import datetime
import torch
from transformers import BertTokenizer, BertModel
from sklearn.metrics.pairwise import cosine_similarity
import redis
import json
import time
from concurrent.futures import ThreadPoolExecutor

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

# 配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '123456',
    'database': 'qz_sns',
    'charset': 'utf8mb4'
}

# Redis配置
REDIS_CONFIG = {
    'host': '114.55.233.139',
    'password': 'Lb040212',
    'port': 6379,
    'db': 2,
    'decode_responses': False  # 存储二进制数据
}

# 模型路径
MODEL_PATH = './models/bert-base-chinese'


class DatabaseManager:
    """数据库管理器"""

    @staticmethod
    def get_recent_recommended_contents(user_id: int, hours: int = 24, content_type: int = None) -> set:
        """获取用户最近推荐过的内容ID
        content_type: 1=图文, 2=视频, None=全部
        """
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                if content_type:
                    sql = """
                        SELECT DISTINCT rl.content_id
                        FROM recommendation_log rl
                        JOIN content c ON rl.content_id = c.id
                        WHERE rl.user_id = %s
                        AND rl.created_time > DATE_SUB(NOW(), INTERVAL %s HOUR)
                        AND c.type = %s
                    """
                    cursor.execute(sql, (user_id, hours, content_type))
                else:
                    sql = """
                        SELECT DISTINCT content_id
                        FROM recommendation_log
                        WHERE user_id = %s
                        AND created_time > DATE_SUB(NOW(), INTERVAL %s HOUR)
                    """
                    cursor.execute(sql, (user_id, hours))
                return {row[0] for row in cursor.fetchall()}
        finally:
            conn.close()

    @staticmethod
    def save_recommendation_log(user_id: int, recommendations: List[Tuple[int, float, str]]):
        """保存推荐记录
        recommendations: [(content_id, score, strategy), ...]
        """
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                values = []
                for position, (content_id, score, strategy) in enumerate(recommendations, 1):
                    # 限制score在0-10范围内，避免数据库字段溢出
                    # decimal(6,4) 范围是 -99.9999 到 99.9999
                    normalized_score = max(0.0, min(10.0, float(score)))
                    values.append((user_id, content_id, strategy, normalized_score, position))
                    logging.debug(f"Saving log: user={user_id}, content={content_id}, strategy={strategy}, "
                                f"original_score={score}, normalized_score={normalized_score}, position={position}")

                sql = """
                       INSERT INTO recommendation_log 
                       (user_id, content_id, strategy, score, position)
                       VALUES (%s, %s, %s, %s, %s)
                   """
                cursor.executemany(sql, values)
                conn.commit()
                logging.info(f"Saved {len(values)} recommendation logs for user {user_id}")
        except Exception as e:
            logging.error(f"Error saving recommendation log: {str(e)}")
            logging.error(f"Failed values: {values}")
            conn.rollback()
        finally:
            conn.close()

    @staticmethod
    def get_family_relatives(user_id: int) -> List[int]:
        """获取用户的亲属ID"""
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                sql = """
                       SELECT relative_id
                       FROM family_relation
                       WHERE user_id = %s
                   """
                cursor.execute(sql, (user_id,))
                return [row[0] for row in cursor.fetchall()]
        finally:
            conn.close()

    @staticmethod
    def get_user_viewed_contents(user_id: int, content_type: int = None) -> set:
        """获取用户已查看的内容
        content_type: 1=图文, 2=视频, None=全部
        """
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                if content_type:
                    sql = """
                        SELECT DISTINCT uvh.content_id
                        FROM user_view_history uvh
                        JOIN content c ON uvh.content_id = c.id
                        WHERE uvh.user_id = %s
                        AND c.type = %s
                    """
                    cursor.execute(sql, (user_id, content_type))
                else:
                    sql = """
                        SELECT DISTINCT content_id
                        FROM user_view_history
                        WHERE user_id = %s
                    """
                    cursor.execute(sql, (user_id,))
                return {row[0] for row in cursor.fetchall()}
        finally:
            conn.close()

    @staticmethod
    def get_relatives_recent_viewed_contents(relative_ids: List[int], days: int, limit: int = 50,
                                             content_type: int = None) -> List[int]:
        """获取亲属最近查看的内容（按时间排序）
        content_type: 1=图文, 2=视频, None=全部
        """
        if not relative_ids:
            return []

        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                placeholders = ','.join(['%s'] * len(relative_ids))
                if content_type:
                    sql = f"""
                        SELECT DISTINCT uvh.content_id, MAX(uvh.view_time) as latest_view
                        FROM user_view_history uvh
                        JOIN content c ON uvh.content_id = c.id
                        WHERE uvh.user_id IN ({placeholders})
                        AND uvh.view_time > DATE_SUB(NOW(), INTERVAL %s DAY)
                        AND c.type = %s
                        GROUP BY uvh.content_id
                        ORDER BY latest_view DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, relative_ids + [days, content_type, limit])
                else:
                    sql = f"""
                        SELECT DISTINCT content_id, MAX(view_time) as latest_view
                        FROM user_view_history
                        WHERE user_id IN ({placeholders})
                        AND view_time > DATE_SUB(NOW(), INTERVAL %s DAY)
                        GROUP BY content_id
                        ORDER BY latest_view DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, relative_ids + [days, limit])
                return [row[0] for row in cursor.fetchall()]
        finally:
            conn.close()

    @staticmethod
    def get_connection():
        return pymysql.connect(**DB_CONFIG)

    @staticmethod
    def save_content_features(content_id: int, text_vector: List[float], keyword_vector: Dict[str, float]):
        """保存内容特征到数据库"""
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                sql = """
                    INSERT INTO content_feature (content_id, text_vector, keyword_vector, extracted_time)
                    VALUES (%s, %s, %s, NOW())
                    ON DUPLICATE KEY UPDATE
                        text_vector = %s,
                        keyword_vector = %s,
                        extracted_time = NOW()
                """
                cursor.execute(sql, (content_id, json.dumps(text_vector), json.dumps(keyword_vector),
                                     json.dumps(text_vector), json.dumps(keyword_vector)))
                conn.commit()
                logging.info(f"Saved features for content_id {content_id}")
        except Exception as e:
            logging.error(f"Error saving features for content_id {content_id}: {str(e)}")
            conn.rollback()
        finally:
            conn.close()

    @staticmethod
    def get_content_features(content_ids: List[int]) -> Dict:
        """批量获取内容特征"""
        if not content_ids:
            return {}

        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor(pymysql.cursors.DictCursor) as cursor:
                placeholders = ','.join(['%s'] * len(content_ids))
                sql = f"""
                    SELECT cf.content_id, cf.text_vector, cf.keyword_vector,
                           c.category_id, c.tags, c.type
                    FROM content_feature cf
                    JOIN content c ON cf.content_id = c.id
                    WHERE cf.content_id IN ({placeholders})
                """
                cursor.execute(sql, content_ids)

                results = {}
                for row in cursor.fetchall():
                    results[row['content_id']] = {
                        'text_vector': json.loads(row['text_vector']) if row['text_vector'] else None,
                        'keywords': json.loads(row['keyword_vector']) if row['keyword_vector'] else {},
                        'category_id': row['category_id'],
                        'tags': row['tags'],
                        'type': row['type']
                    }
                return results
        finally:
            conn.close()

    @staticmethod
    def get_user_behavior_history(user_id: int, days: int = 30, content_type: int = None) -> List[Dict]:
        """获取用户行为历史
        content_type: 1=图文, 2=视频, None=全部
        """
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor(pymysql.cursors.DictCursor) as cursor:
                if content_type:
                    sql = """
                        SELECT ub.content_id, ub.behavior_type, ub.created_time,
                               c.category_id, c.type
                        FROM user_behavior ub
                        JOIN content c ON ub.content_id = c.id
                        WHERE ub.user_id = %s 
                        AND ub.created_time > DATE_SUB(NOW(), INTERVAL %s DAY)
                        AND c.type = %s
                        ORDER BY ub.created_time DESC
                    """
                    cursor.execute(sql, (user_id, days, content_type))
                else:
                    sql = """
                        SELECT ub.content_id, ub.behavior_type, ub.created_time,
                               c.category_id, c.type
                        FROM user_behavior ub
                        JOIN content c ON ub.content_id = c.id
                        WHERE ub.user_id = %s 
                        AND ub.created_time > DATE_SUB(NOW(), INTERVAL %s DAY)
                        ORDER BY ub.created_time DESC
                    """
                    cursor.execute(sql, (user_id, days))
                return cursor.fetchall()
        finally:
            conn.close()

    @staticmethod
    def get_similar_users(user_id: int, limit: int = 50, content_type: int = None) -> List[int]:
        """获取相似用户（基于共同行为）
        content_type: 1=图文, 2=视频, None=全部
        """
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                if content_type:
                    sql = """
                        SELECT ub2.user_id, COUNT(DISTINCT ub2.content_id) as common_count
                        FROM user_behavior ub1
                        JOIN user_behavior ub2 ON ub1.content_id = ub2.content_id
                        JOIN content c ON ub1.content_id = c.id
                        WHERE ub1.user_id = %s 
                        AND ub2.user_id != %s
                        AND ub1.behavior_type IN ('like', 'collect', 'share')
                        AND ub2.behavior_type IN ('like', 'collect', 'share')
                        AND c.type = %s
                        GROUP BY ub2.user_id
                        ORDER BY common_count DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, (user_id, user_id, content_type, limit))
                else:
                    sql = """
                        SELECT ub2.user_id, COUNT(DISTINCT ub2.content_id) as common_count
                        FROM user_behavior ub1
                        JOIN user_behavior ub2 ON ub1.content_id = ub2.content_id
                        WHERE ub1.user_id = %s 
                        AND ub2.user_id != %s
                        AND ub1.behavior_type IN ('like', 'collect', 'share')
                        AND ub2.behavior_type IN ('like', 'collect', 'share')
                        GROUP BY ub2.user_id
                        ORDER BY common_count DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, (user_id, user_id, limit))
                return [row[0] for row in cursor.fetchall()]
        finally:
            conn.close()


class RecommendationEngine:
    """推荐引擎"""

    def __init__(self, bert_embedder, redis_client):
        self.bert_embedder = bert_embedder
        self.redis_client = redis_client
        self.db_manager = DatabaseManager()
        self.executor = ThreadPoolExecutor(max_workers=4)

    def reduce_dimensions(self, vector: np.ndarray, target_dim: int) -> np.ndarray:
        """简单的降维方法（平均池化）"""
        if len(vector) <= target_dim:
            return vector

        # 简单的平均池化降维
        chunk_size = len(vector) // target_dim
        reduced = []
        for i in range(target_dim):
            start = i * chunk_size
            end = start + chunk_size if i < target_dim - 1 else len(vector)
            reduced.append(np.mean(vector[start:end]))

        return np.array(reduced)

    def get_recommendations(self, user_id: int, size: int = 20, content_type: int = None) -> List[int]:
        """获取推荐内容ID列表，使用分层过滤策略
        content_type: 1=图文, 2=视频, None=全部
        """
        logging.info(f"======== 开始生成推荐 ========")
        logging.info(f"用户ID: {user_id}, 请求数量: {size}, 内容类型: {content_type}")

        # 获取用户已查看的内容
        viewed_ids = self.db_manager.get_user_viewed_contents(user_id, content_type)
        logging.info(f"📊 用户已浏览内容数: {len(viewed_ids)}")
        print(f"User {user_id} has viewed {len(viewed_ids)} contents of type {content_type}.")

        # 使用分层的时间窗口获取已推荐内容
        # 第一层：最近6小时（严格避免重复）
        very_recent_recommended = self.db_manager.get_recent_recommended_contents(user_id, hours=6,
                                                                                  content_type=content_type)
        # 第二层：6-24小时（可以考虑重新推荐）
        recent_recommended = self.db_manager.get_recent_recommended_contents(user_id, hours=24,
                                                                             content_type=content_type)

        # 检查缓存
        cache_key = f"recommendations:user:{user_id}:type:{content_type}:v3"
        cached = self.redis_client.get(cache_key)

        if cached:
            all_recommendations = json.loads(cached)
            # 过滤已查看和最近6小时推荐的内容
            filtered_recommendations = [
                id for id in all_recommendations
                if id not in viewed_ids and id not in very_recent_recommended
            ]

            if len(filtered_recommendations) >= size:
                selected = filtered_recommendations[:size]

                # 记录推荐日志
                recommendations_for_log = []
                for content_id in selected:
                    recommendations_for_log.append((content_id, 0.0, 'cached'))

                if recommendations_for_log:
                    self.db_manager.save_recommendation_log(user_id, recommendations_for_log)

                # 更新缓存
                remaining = filtered_recommendations[size:]
                if remaining:
                    self.redis_client.setex(cache_key, 300, json.dumps(remaining))
                else:
                    self.redis_client.delete(cache_key)

                return selected

        # 获取各种推荐策略的结果（不过滤）
        logging.info(f"🔄 开始多策略推荐...")
        recommendations_with_strategy = []

        # 1. 基于用户历史的推荐（动态）
        logging.info(f"策略1: 基于用户历史...")
        user_based = self.get_user_based_recommendations(user_id, size * 3, content_type)
        logging.info(f"  ✓ 用户历史推荐数: {len(user_based)}")
        if len(user_based) == 0:
            logging.warning(f"  ⚠️ 用户历史推荐为空！")
        for content_id in user_based:
            recommendations_with_strategy.append((content_id, 0.3, 'user_based'))
            print(f"User {user_id} user_based recommendation: {content_id}")

        # 2. 基于内容的推荐（半动态）
        logging.info(f"策略2: 基于内容相似度...")
        content_based = self.get_content_based_recommendations(user_id, size * 3, content_type)
        logging.info(f"  ✓ 内容相似度推荐数: {len(content_based)}")
        if len(content_based) == 0:
            logging.warning(f"  ⚠️ 内容相似度推荐为空！")
        for content_id in content_based:
            recommendations_with_strategy.append((content_id, 0.5, 'content_based'))
            print(f"User {user_id} content_based recommendation: {content_id}")

        # 3. 协同过滤推荐（动态）
        logging.info(f"策略3: 协同过滤...")
        cf_based = self.get_collaborative_filtering_recommendations(user_id, size * 3, content_type)
        logging.info(f"  ✓ 协同过滤推荐数: {len(cf_based)}")
        if len(cf_based) == 0:
            logging.warning(f"  ⚠️ 协同过滤推荐为空！")
        for content_id in cf_based:
            recommendations_with_strategy.append((content_id, 0.2, 'cf'))
            print(f"User {user_id} cf_based recommendation: {content_id}")
        
        logging.info(f"📦 所有策略推荐总数: {len(recommendations_with_strategy)}")

        # 合并和计算综合得分
        content_scores = {}
        content_strategies = {}

        for content_id, weight, strategy in recommendations_with_strategy:
            if content_id not in content_scores:
                content_scores[content_id] = 0
                content_strategies[content_id] = []
            content_scores[content_id] += weight
            content_strategies[content_id].append(strategy)

        # 排序
        sorted_contents = sorted(content_scores.items(), key=lambda x: x[1], reverse=True)

        # 分层过滤
        logging.info(f"🎯 开始分层过滤...")
        priority_1 = []  # 从未推荐过的
        priority_2 = []  # 6-24小时前推荐的
        priority_3 = []  # 最近6小时推荐的（备选）
        
        filtered_viewed = 0
        filtered_recent = 0

        for content_id, score in sorted_contents:
            if content_id in viewed_ids:
                filtered_viewed += 1
                continue

            if content_id not in recent_recommended:
                priority_1.append((content_id, score))
            elif content_id not in very_recent_recommended:
                priority_2.append((content_id, score))
            else:
                priority_3.append((content_id, score))
                filtered_recent += 1
        
        logging.info(f"  过滤统计:")
        logging.info(f"    - 已浏览过滤: {filtered_viewed} 条")
        logging.info(f"    - 最近推荐过滤: {filtered_recent} 条")
        logging.info(f"  优先级分布:")
        logging.info(f"    - P1(从未推荐): {len(priority_1)} 条")
        logging.info(f"    - P2(6-24h推荐): {len(priority_2)} 条")
        logging.info(f"    - P3(6h内推荐): {len(priority_3)} 条")

        # 组装最终推荐
        final_recommendations = []
        recommendations_for_log = []

        # 优先级1：从未推荐的
        for content_id, score in priority_1[:size]:
            final_recommendations.append(content_id)
            strategy = 'mixed' if len(content_strategies[content_id]) > 1 else content_strategies[content_id][0]
            recommendations_for_log.append((content_id, float(score), strategy))

        # 优先级2：较早推荐的
        if len(final_recommendations) < size:
            remaining = size - len(final_recommendations)
            for content_id, score in priority_2[:remaining]:
                final_recommendations.append(content_id)
                strategy = 'mixed' if len(content_strategies[content_id]) > 1 else content_strategies[content_id][0]
                recommendations_for_log.append((content_id, float(score), strategy + '_repeat'))

        # 优先级3：如果还不够，考虑最近推荐的
        if len(final_recommendations) < size:
            remaining = size - len(final_recommendations)
            for content_id, score in priority_3[:remaining]:
                final_recommendations.append(content_id)
                strategy = 'mixed' if len(content_strategies[content_id]) > 1 else content_strategies[content_id][0]
                recommendations_for_log.append((content_id, float(score), strategy + '_recent_repeat'))

        # 保存推荐记录
        if recommendations_for_log:
            self.db_manager.save_recommendation_log(user_id, recommendations_for_log)

        # 缓存候选内容
        all_candidates = []
        for content_id, _ in priority_1 + priority_2:
            if content_id not in final_recommendations:
                all_candidates.append(content_id)

        if all_candidates:
            self.redis_client.setex(cache_key, 300, json.dumps(all_candidates[:size * 3]))

        logging.info(f"======== 推荐生成完成 ========")
        logging.info(f"✅ 最终推荐数量: {len(final_recommendations)}")
        print(f"Generated {len(final_recommendations)} recommendations for user {user_id} of type {content_type}")
        print(
            f"Priority distribution: P1={len([r for r in final_recommendations if r in [c[0] for c in priority_1]])}, "
            f"P2={len([r for r in final_recommendations if r in [c[0] for c in priority_2]])}, "
            f"P3={len([r for r in final_recommendations if r in [c[0] for c in priority_3]])}")

        # 如果推荐结果为空，使用热门内容作为降级方案
        if len(final_recommendations) == 0:
            logging.warning(f"⚠️ 所有推荐策略均未返回结果，使用热门内容降级方案")
            try:
                hot_contents = self.get_hot_contents_with_filter(size=size, content_type=content_type, user_id=user_id)
                logging.info(f"  ✓ 热门降级返回 {len(hot_contents)} 条内容")
                return hot_contents
            except Exception as e:
                logging.error(f"  ❌ 热门降级也失败了: {str(e)}")
                return []

        return final_recommendations

    def get_user_based_recommendations(self, user_id: int, size: int, content_type: int = None) -> List[int]:
        """基于用户历史行为的推荐"""
        behaviors = self.db_manager.get_user_behavior_history(user_id, days=400, content_type=content_type)
        if not behaviors:
            return []

        # 统计用户偏好
        recent_contents = []
        for behavior in behaviors[:20]:  # 最近20个行为
            recent_contents.append(behavior['content_id'])

        # 获取相似内容
        similar_contents = []
        for content_id in recent_contents[:5]:  # 基于最近5个内容
            similar = self.get_similar_contents_from_db(content_id, 10, content_type)
            similar_contents.extend(similar)

        # 去重并返回
        seen = set()
        result = []
        for content_id in similar_contents:
            if content_id not in seen and content_id not in recent_contents:
                seen.add(content_id)
                result.append(content_id)
                if len(result) >= size:
                    break

        return result

    def get_content_based_recommendations(self, user_id: int, size: int, content_type: int = None) -> List[int]:
        """基于内容相似度的推荐"""
        behaviors = self.db_manager.get_user_behavior_history(user_id, days=400, content_type=content_type)

        if not behaviors:
            return []

        # 获取高权重行为的内容
        seed_contents = []
        for behavior in behaviors:
            if behavior['behavior_type'] in ['like', 'collect', 'share']:
                seed_contents.append(behavior['content_id'])

        if not seed_contents:
            seed_contents = [b['content_id'] for b in behaviors[:5]]

        # 获取相似内容
        recommendations = []
        for content_id in seed_contents[:3]:
            similar = self.get_similar_contents_from_db(content_id, size // 3 + 1, content_type)
            recommendations.extend(similar)

        # 去重
        return list(dict.fromkeys(recommendations))[:size]

    def get_collaborative_filtering_recommendations(self, user_id: int, size: int, content_type: int = None) -> List[
        int]:
        """协同过滤推荐，基于用户亲属的最新浏览历史"""
        relatives = self.db_manager.get_family_relatives(user_id)
        if not relatives:
            return []

        # 获取用户已查看的内容
        viewed_ids = self.db_manager.get_user_viewed_contents(user_id, content_type)

        # 获取亲属最近的浏览内容（按时间排序）
        recent_relative_contents = self.db_manager.get_relatives_recent_viewed_contents(
            relatives,
            days=400,  # 只获取最近400天的
            limit=size * 2,  # 获取足够的内容
            content_type=content_type
        )

        # 排除用户已查看的内容
        recommendations = [cid for cid in recent_relative_contents if cid not in viewed_ids]

        return recommendations[:size]

    def get_hot_contents_with_filter(self, size: int = 20, content_type: int = None, user_id: int = None) -> List[int]:
        """获取热门内容（支持分层过滤）
        content_type: 1=图文, 2=视频, None=全部
        user_id: 如果提供，将进行去重过滤
        """
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                # 基础查询
                if content_type:
                    sql = """
                        SELECT id, (view_count * 0.1 + like_count * 0.3 + 
                                   comment_count * 0.4 + share_count * 0.2) as score
                        FROM content
                        WHERE status = 1
                        AND type = %s
                        AND created_time > DATE_SUB(NOW(), INTERVAL 400 DAY)
                        ORDER BY score DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, (content_type, size * 3))  # 获取3倍数量以便过滤
                else:
                    sql = """
                        SELECT id, (view_count * 0.1 + like_count * 0.3 + 
                                   comment_count * 0.4 + share_count * 0.2) as score
                        FROM content
                        WHERE status = 1
                        AND created_time > DATE_SUB(NOW(), INTERVAL 400 DAY)
                        ORDER BY score DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, (size * 3,))

                all_hot_contents = [(row[0], row[1]) for row in cursor.fetchall()]

                # 如果没有提供用户ID，直接返回
                if not user_id:
                    return [content_id for content_id, _ in all_hot_contents[:size]]

                # 获取用户已查看的内容
                viewed_ids = self.db_manager.get_user_viewed_contents(user_id, content_type)

                # 获取分层推荐历史
                very_recent_recommended = self.db_manager.get_recent_recommended_contents(
                    user_id, hours=6, content_type=content_type
                )
                recent_recommended = self.db_manager.get_recent_recommended_contents(
                    user_id, hours=24, content_type=content_type
                )

                # 分层过滤
                priority_1 = []  # 从未查看且从未推荐
                priority_2 = []  # 从未查看但6-24小时前推荐过
                priority_3 = []  # 从未查看但最近6小时推荐过

                for content_id, score in all_hot_contents:
                    if content_id in viewed_ids:
                        continue

                    if content_id not in recent_recommended:
                        priority_1.append((content_id, score))
                    elif content_id not in very_recent_recommended:
                        priority_2.append((content_id, score))
                    else:
                        priority_3.append((content_id, score))

                # 组装最终结果
                final_recommendations = []
                recommendations_for_log = []

                # 优先级1
                for content_id, score in priority_1[:size]:
                    final_recommendations.append(content_id)
                    recommendations_for_log.append((content_id, float(score), 'hot'))

                # 优先级2
                if len(final_recommendations) < size:
                    remaining = size - len(final_recommendations)
                    for content_id, score in priority_2[:remaining]:
                        final_recommendations.append(content_id)
                        recommendations_for_log.append((content_id, float(score), 'hot_repeat'))

                # 优先级3
                if len(final_recommendations) < size:
                    remaining = size - len(final_recommendations)
                    for content_id, score in priority_3[:remaining]:
                        final_recommendations.append(content_id)
                        recommendations_for_log.append((content_id, float(score), 'hot_recent_repeat'))

                # 保存推荐记录
                if recommendations_for_log:
                    self.db_manager.save_recommendation_log(user_id, recommendations_for_log)

                return final_recommendations

        finally:
            conn.close()

    def get_recommendation_stats(self, user_id: int, content_type: int = None) -> Dict[str, int]:
        """获取推荐统计信息"""
        viewed_ids = self.db_manager.get_user_viewed_contents(user_id, content_type)
        recent_recommended_ids = self.db_manager.get_recent_recommended_contents(user_id, hours=24,
                                                                                 content_type=content_type)

        # 获取所有可用内容
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                # 获取总内容数
                if content_type:
                    cursor.execute("SELECT COUNT(*) FROM content WHERE status = 1 AND type = %s", (content_type,))
                else:
                    cursor.execute("SELECT COUNT(*) FROM content WHERE status = 1")
                total_contents = cursor.fetchone()[0]

                # 获取已查看数
                viewed_count = len(viewed_ids)

                # 获取已推荐数
                recommended_count = len(recent_recommended_ids)

                # 计算剩余可推荐数
                remaining = total_contents - viewed_count - recommended_count

                return {
                    'total_contents': total_contents,
                    'viewed_count': viewed_count,
                    'recommended_count': recommended_count,
                    'remaining_count': max(0, remaining),
                    'content_type': content_type
                }
        finally:
            conn.close()

    def get_similar_contents_from_db(self, content_id: int, limit: int, content_type: int = None) -> List[int]:
        """从数据库获取相似内容"""
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                if content_type:
                    sql = """
                        SELECT cs.content_id2 as similar_content_id
                        FROM content_similarity cs
                        JOIN content c ON cs.content_id2 = c.id
                        WHERE cs.content_id1 = %s
                        AND c.type = %s
                        ORDER BY cs.similarity_score DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, (content_id, content_type, limit))
                else:
                    sql = """
                        SELECT content_id2 as similar_content_id
                        FROM content_similarity
                        WHERE content_id1 = %s
                        ORDER BY similarity_score DESC
                        LIMIT %s
                    """
                    cursor.execute(sql, (content_id, limit))
                return [row[0] for row in cursor.fetchall()]
        finally:
            conn.close()

    def get_user_viewed_contents(self, user_id: int) -> set:
        """获取用户已查看的内容"""
        cache_key = f"user:viewed:{user_id}"
        cached = self.redis_client.get(cache_key)
        if cached:
            return set(json.loads(cached))

        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                sql = """
                    SELECT DISTINCT content_id
                    FROM user_behavior
                    WHERE user_id = %s
                    AND behavior_type = 'view'
                """
                cursor.execute(sql, (user_id,))
                viewed = [row[0] for row in cursor.fetchall()]

                # 缓存1小时
                self.redis_client.setex(cache_key, 3600, json.dumps(viewed))

                return set(viewed)
        finally:
            conn.close()

    def get_behavior_weight(self, behavior_type: str) -> float:
        """获取行为权重"""
        weights = {
            'view': 1.0,
            'like': 3.0,
            'comment': 2.5,
            'share': 4.0,
            'collect': 3.5
        }
        return weights.get(behavior_type, 1.0)

    def calculate_and_save_content_similarity(self, content_id: int, content_vector: List[float]) -> List[
        Tuple[int, float]]:
        """计算并保存内容相似度"""
        conn = DatabaseManager.get_connection()
        try:
            with conn.cursor() as cursor:
                # 获取所有现有内容的特征向量
                sql = """
                    SELECT content_id, text_vector
                    FROM content_feature
                    WHERE content_id != %s AND text_vector IS NOT NULL
                """
                cursor.execute(sql, (content_id,))
                similar_contents = []

                for row in cursor.fetchall():
                    other_id = row[0]
                    other_vector = json.loads(row[1]) if row[1] else None

                    if other_vector is not None:
                        similarity = cosine_similarity([content_vector], [other_vector])[0][0]  # 采用余弦相似度计算
                        if similarity > 0.3:  # 相似度阈值
                            similar_contents.append((other_id, float(similarity)))

                # 按相似度排序，取内容总数的一半
                count = len(similar_contents)
                similar_contents.sort(key=lambda x: x[1], reverse=True)
                top_similar = similar_contents[:count // 2]

                # 保存到数据库
                if top_similar:
                    for sim_id, score in top_similar:
                        sql_insert = """
                            INSERT INTO content_similarity (content_id1, content_id2, similarity_score)
                            VALUES (%s, %s, %s)
                            ON DUPLICATE KEY UPDATE similarity_score = %s
                        """
                        cursor.execute(sql_insert, (content_id, sim_id, score, score))

                conn.commit()
                logging.info(f"Saved {len(top_similar)} similar contents for content {content_id}")
                return top_similar

        except Exception as e:
            logging.error(f"Error calculating similarity for content_id {content_id}: {str(e)}")
            conn.rollback()
            return []
        finally:
            conn.close()


class TextProcessor:
    def __init__(self):
        # 初始化jieba
        jieba.initialize()
        # 加载停用词
        self.stop_words = self.load_stop_words()

    def load_stop_words(self):
        """加载停用词表"""
        stop_words = set(['的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都',
                          '一', '一个', '上', '也', '很', '到', '说', '要', '去', '你',
                          '会', '着', '没有', '看', '好', '自己', '这', '那', '她', '他'])
        return stop_words

    def extract_text_from_html(self, html_content: str) -> str:
        """从HTML中提取纯文本"""
        if not html_content:
            return ""

        soup = BeautifulSoup(html_content, 'html.parser')

        # 移除script和style标签
        for script in soup(["script", "style"]):
            script.decompose()

        # 获取文本
        text = soup.get_text()

        # 清理空白字符
        lines = (line.strip() for line in text.splitlines())
        chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
        text = ' '.join(chunk for chunk in chunks if chunk)

        return text.strip()

    def extract_keywords(self, text: str, top_k: int = 20) -> Dict[str, float]:
        """提取关键词及权重"""
        if not text:
            return {}

        # 使用jieba的TextRank算法提取关键词
        keywords = jieba.analyse.textrank(
            text,
            topK=top_k,
            withWeight=True,
            allowPOS=('n', 'nr', 'ns', 'nt', 'nw', 'nz', 'v', 'vd', 'vn', 'a', 'ad', 'an')
        )

        # 同时使用TF-IDF提取
        tfidf_keywords = jieba.analyse.extract_tags(
            text,
            topK=top_k,
            withWeight=True
        )

        # 合并两种方法的结果
        keyword_dict = {}
        for word, weight in keywords:
            if word not in self.stop_words and len(word) > 1:
                keyword_dict[word] = weight

        for word, weight in tfidf_keywords:
            if word not in self.stop_words and len(word) > 1:
                if word in keyword_dict:
                    keyword_dict[word] = (keyword_dict[word] + weight) / 2
                else:
                    keyword_dict[word] = weight * 0.8

        # 归一化
        if keyword_dict:
            max_weight = max(keyword_dict.values())
            keyword_dict = {k: v / max_weight for k, v in keyword_dict.items()}

        # 按权重排序，返回前top_k个
        sorted_keywords = sorted(keyword_dict.items(), key=lambda x: x[1], reverse=True)
        return dict(sorted_keywords[:top_k])


class BERTEmbedder:
    """使用BERT生成文本向量"""

    def __init__(self, model_path):
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        print(f"Using device: {self.device}")

        # 加载本地模型
        print(f"Loading BERT model from {model_path}...")
        self.tokenizer = BertTokenizer.from_pretrained(model_path)
        self.model = BertModel.from_pretrained(model_path)
        self.model.to(self.device)
        self.model.eval()
        print("BERT model loaded successfully!")

    def get_embedding(self, text: str, max_length: int = 512) -> np.ndarray:
        """获取文本的BERT嵌入向量"""
        try:
            # 分词
            inputs = self.tokenizer(
                text,
                padding=True,
                truncation=True,
                max_length=max_length,
                return_tensors='pt'
            )

            # 移到设备
            inputs = {k: v.to(self.device) for k, v in inputs.items()}

            # 获取BERT输出
            with torch.no_grad():
                outputs = self.model(**inputs)

            # 使用[CLS]标记的输出作为句子表示
            embeddings = outputs.last_hidden_state[:, 0, :].cpu().numpy()
            return embeddings[0]
        except Exception as e:
            logging.error(f"Error getting embedding for text: {text[:30]}... : {str(e)}")
            return np.array([])  # 返回空数组以避免后续处理错误


# 初始化全局对象
print("Initializing services...")
text_processor = TextProcessor()
bert_embedder = BERTEmbedder(MODEL_PATH)
redis_client = redis.Redis(**REDIS_CONFIG)
recommendation_engine = RecommendationEngine(bert_embedder, redis_client)
print("Services initialized!")


@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'healthy',
        'service': 'recommendation-service',
        'timestamp': datetime.now().isoformat()
    })


@app.route('/extract_and_save_features', methods=['POST'])
def extract_and_save_features():
    """提取特征并计算相似度（内容上传时调用）"""
    try:
        data = request.json
        print(f"Received data for feature extraction: {data}")
        content_id = data.get('content_id')
        content_type = data.get('type')

        logging.info(f"Extracting features for new content: {content_id}")

        html_content = data.get('content', '')
        title = data.get('title', '')

        # 如果类型为1，文本类型正常提取特征向量
        if content_type == 1:
            # 提取纯文本
            plain_text = text_processor.extract_text_from_html(html_content)
            full_text = f"{title}。{plain_text}"
            print(f"Extracted text for content {content_id}: {full_text[:100]}...")

            # 限制长度
            if len(full_text) > 5000:
                full_text = full_text[:5000]

            # 提取特征
            keywords = text_processor.extract_keywords(full_text)
            text_vector = bert_embedder.get_embedding(full_text)

            if len(text_vector) > 0:
                text_vector = recommendation_engine.reduce_dimensions(text_vector, 256).tolist()

                # 保存特征向量到数据库
                DatabaseManager.save_content_features(content_id, text_vector, keywords)

                # 计算并保存相似度
                similar_contents = recommendation_engine.calculate_and_save_content_similarity(
                    content_id, text_vector
                )

                # 重要：这里需要返回响应！
                return jsonify({
                    'success': True,
                    'content_id': content_id,
                    'keywords': keywords,
                    'vector_dim': len(text_vector),
                    'similar_contents': similar_contents[:5]  # 返回前5个最相似的
                })
            else:
                return jsonify({
                    'success': False,
                    'error': 'Failed to generate text vector for text content'
                })

        # 如果是视频类型type=2，只需要根据标题和标签来提取特征向量
        elif content_type == 2:
            # 使用标题和标签作为文本内容
            title = title.strip()
            tags = data.get('tags', '')
            full_text = f"{title}。{tags}"
            print(f"Extracted text for video content {content_id}: {full_text[:100]}...")

            # 限制长度
            if len(full_text) > 2000:
                full_text = full_text[:2000]

            # 提取特征
            keywords = text_processor.extract_keywords(full_text)
            text_vector = bert_embedder.get_embedding(full_text)

            if len(text_vector) > 0:
                text_vector = recommendation_engine.reduce_dimensions(text_vector, 256).tolist()

                # 保存特征向量到数据库
                DatabaseManager.save_content_features(content_id, text_vector, keywords)

                # 计算并保存相似度
                similar_contents = recommendation_engine.calculate_and_save_content_similarity(
                    content_id, text_vector
                )

                return jsonify({
                    'success': True,
                    'content_id': content_id,
                    'keywords': keywords,
                    'vector_dim': len(text_vector),
                    'similar_contents': similar_contents[:5]  # 返回前5个最相似的
                })
            else:
                return jsonify({
                    'success': False,
                    'error': 'Failed to generate text vector for video content'
                })
        else:
            return jsonify({
                'success': False,
                'error': f'Unknown content type: {content_type}'
            })

    except Exception as e:
        logging.error(f"Error extracting features: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_recommendations', methods=['POST'])
def get_recommendations():
    """获取推荐内容ID列表（混合推荐）"""
    try:
        data = request.json
        user_id = data.get('user_id')
        size = data.get('size', 20)

        logging.info(f"Getting mixed recommendations for user: {user_id}")

        # 获取推荐
        recommendations = recommendation_engine.get_recommendations(user_id, size)

        return jsonify({
            'success': True,
            'user_id': user_id,
            'recommendations': recommendations,
            'count': len(recommendations)
        })

    except Exception as e:
        logging.error(f"Error getting recommendations: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_article_recommendations', methods=['POST'])
def get_article_recommendations():
    """获取图文推荐内容ID列表"""
    try:
        data = request.json
        user_id = data.get('user_id')
        size = data.get('size', 20)

        logging.info(f"Getting article recommendations for user: {user_id}")

        # 获取图文推荐（type=1）
        recommendations = recommendation_engine.get_recommendations(user_id, size, content_type=1)

        return jsonify({
            'success': True,
            'user_id': user_id,
            'recommendations': recommendations,
            'count': len(recommendations),
            'content_type': 'article'
        })

    except Exception as e:
        logging.error(f"Error getting article recommendations: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_video_recommendations', methods=['POST'])
def get_video_recommendations():
    """获取视频推荐内容ID列表"""
    try:
        data = request.json
        user_id = data.get('user_id')
        size = data.get('size', 20)

        logging.info(f"Getting video recommendations for user: {user_id}")

        # 获取视频推荐（type=2）
        recommendations = recommendation_engine.get_recommendations(user_id, size, content_type=2)

        return jsonify({
            'success': True,
            'user_id': user_id,
            'recommendations': recommendations,
            'count': len(recommendations),
            'content_type': 'video'
        })

    except Exception as e:
        logging.error(f"Error getting video recommendations: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/update_user_behavior', methods=['POST'])
def update_user_behavior():
    """更新用户行为后刷新缓存"""
    try:
        data = request.json
        user_id = data.get('user_id')

        # 清除用户相关缓存（包括各种类型的缓存）
        cache_keys = [
            f"recommendations:user:{user_id}:v3",
            f"recommendations:user:{user_id}:type:1:v3",
            f"recommendations:user:{user_id}:type:2:v3",
            f"recommendations:user:{user_id}:type:None:v3",
            f"user:viewed:{user_id}"
        ]

        for key in cache_keys:
            redis_client.delete(key)

        return jsonify({'success': True})

    except Exception as e:
        logging.error(f"Error updating user behavior: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_hot_contents', methods=['GET'])
def get_hot_contents():
    """获取热门内容（用于冷启动，支持去重）"""
    try:
        size = request.args.get('size', 20, type=int)
        user_id = request.args.get('user_id', type=int)
        content_type = request.args.get('type', type=int)

        hot_contents = recommendation_engine.get_hot_contents_with_filter(
            size=size,
            content_type=content_type,
            user_id=user_id
        )

        return jsonify({
            'success': True,
            'hot_contents': hot_contents,
            'count': len(hot_contents),
            'content_type': content_type
        })

    except Exception as e:
        logging.error(f"Error getting hot contents: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_hot_articles', methods=['GET'])
def get_hot_articles():
    """获取热门图文内容"""
    try:
        size = request.args.get('size', 20, type=int)
        user_id = request.args.get('user_id', type=int)

        hot_contents = recommendation_engine.get_hot_contents_with_filter(
            size=size,
            content_type=1,  # 图文类型
            user_id=user_id
        )

        return jsonify({
            'success': True,
            'hot_contents': hot_contents,
            'count': len(hot_contents),
            'content_type': 'article'
        })

    except Exception as e:
        logging.error(f"Error getting hot articles: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_hot_videos', methods=['GET'])
def get_hot_videos():
    """获取热门视频内容"""
    try:
        size = request.args.get('size', 20, type=int)
        user_id = request.args.get('user_id', type=int)

        hot_contents = recommendation_engine.get_hot_contents_with_filter(
            size=size,
            content_type=2,  # 视频类型
            user_id=user_id
        )

        return jsonify({
            'success': True,
            'hot_contents': hot_contents,
            'count': len(hot_contents),
            'content_type': 'video'
        })

    except Exception as e:
        logging.error(f"Error getting hot videos: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/get_recommendation_stats', methods=['GET'])
def get_recommendation_stats():
    """获取推荐统计信息"""
    try:
        user_id = request.args.get('user_id', type=int)
        content_type = request.args.get('type', type=int)

        stats = recommendation_engine.get_recommendation_stats(user_id, content_type)

        return jsonify({
            'success': True,
            'stats': stats
        })

    except Exception as e:
        logging.error(f"Error getting recommendation stats: {str(e)}")
        return jsonify({'success': False, 'error': str(e)}), 500


if __name__ == '__main__':
    print("=" * 50)
    print("Enhanced Recommendation Service Starting...")
    print(f"Database: {DB_CONFIG['database']}")
    print(f"Redis: {REDIS_CONFIG['host']}:{REDIS_CONFIG['port']}")
    print(f"Service URL: http://localhost:5000")
    print("=" * 50)
    print("\nAvailable endpoints:")
    print("- POST /get_recommendations (混合推荐)")
    print("- POST /get_article_recommendations (图文推荐)")
    print("- POST /get_video_recommendations (视频推荐)")
    print("- GET /get_hot_contents (热门内容-支持分类和去重)")
    print("- GET /get_hot_articles (热门图文)")
    print("- GET /get_hot_videos (热门视频)")
    print("- GET /get_recommendation_stats (推荐统计)")
    print("=" * 50)

    app.run(host='0.0.0.0', port=5000, debug=False)
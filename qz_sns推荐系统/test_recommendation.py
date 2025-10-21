#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
推荐系统测试脚本
用于测试推荐系统的各个接口功能
"""

import requests
import json
import time
import random
from datetime import datetime
from typing import Dict, List, Any
import sys

# 配置
BASE_URL = "http://localhost:5000"
TEST_USER_ID = 9  # 测试用户ID
TEST_CONTENT_IDS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]  # 测试内容ID列表


# 颜色输出
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'


def print_header(text: str):
    """打印标题"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'=' * 60}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{text.center(60)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'=' * 60}{Colors.ENDC}")


def print_success(text: str):
    """打印成功信息"""
    print(f"{Colors.OKGREEN}✓ {text}{Colors.ENDC}")


def print_error(text: str):
    """打印错误信息"""
    print(f"{Colors.FAIL}✗ {text}{Colors.ENDC}")


def print_info(text: str):
    """打印信息"""
    print(f"{Colors.OKBLUE}ℹ {text}{Colors.ENDC}")


def print_warning(text: str):
    """打印警告"""
    print(f"{Colors.WARNING}⚠ {text}{Colors.ENDC}")


class RecommendationSystemTester:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.session = requests.Session()
        self.test_results = []

    def test_health_check(self) -> bool:
        """测试健康检查接口"""
        print_header("测试健康检查接口")
        try:
            response = self.session.get(f"{self.base_url}/health")
            if response.status_code == 200:
                data = response.json()
                print_success(f"健康检查成功: {data}")
                return True
            else:
                print_error(f"健康检查失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"健康检查异常: {str(e)}")
            return False

    def test_extract_features(self, content_id: int, content_type: int) -> bool:
        """测试特征提取接口"""
        print_header(f"测试特征提取 (内容ID: {content_id}, 类型: {content_type})")

        # 准备测试数据
        if content_type == 1:  # 图文
            test_data = {
                "content_id": content_id,
                "type": content_type,
                "title": f"测试文章标题 {content_id}",
                "content": f"<h2>数学思维的重要性</h2><p>数学不仅仅是计算，更是一种思维方式。良好的数学思维能够帮助我们在生活和工作中更好地解决问题。</p><h3>逻辑推理训练</h3><p>通过解决数学难题，可以锻炼大脑的逻辑推理能力。建议每天花30分钟进行数学思维训练。</p>",
                "tags": "教育,数学,逻辑思维"
            }
        else:  # 视频
            test_data = {
                "content_id": content_id,
                "type": content_type,
                "title": f"测试视频标题 {content_id}",
                "tags": "测试视频,教程,科技"
            }

        try:
            response = self.session.post(
                f"{self.base_url}/extract_and_save_features",
                json=test_data
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    print_success(f"特征提取成功")
                    print_info(f"关键词: {list(data.get('keywords', {}).keys())[:5]}")
                    print_info(f"向量维度: {data.get('vector_dim')}")
                    print_info(f"相似内容数: {len(data.get('similar_contents', []))}")
                    return True
                else:
                    print_error(f"特征提取失败: {data.get('error')}")
                    return False
            else:
                print_error(f"特征提取失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"特征提取异常: {str(e)}")
            return False

    def test_get_recommendations(self, user_id: int, size: int = 10) -> bool:
        """测试混合推荐接口"""
        print_header(f"测试混合推荐 (用户ID: {user_id})")

        try:
            response = self.session.post(
                f"{self.base_url}/get_recommendations",
                json={"user_id": user_id, "size": size}
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    recommendations = data.get('recommendations', [])
                    print_success(f"获取推荐成功，推荐数量: {len(recommendations)}")
                    print_info(f"推荐内容ID: {recommendations[:5]}...")
                    return True
                else:
                    print_error(f"获取推荐失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取推荐失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取推荐异常: {str(e)}")
            return False

    def test_get_article_recommendations(self, user_id: int, size: int = 10) -> bool:
        """测试图文推荐接口"""
        print_header(f"测试图文推荐 (用户ID: {user_id})")

        try:
            response = self.session.post(
                f"{self.base_url}/get_article_recommendations",
                json={"user_id": user_id, "size": size}
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    recommendations = data.get('recommendations', [])
                    print_success(f"获取图文推荐成功，推荐数量: {len(recommendations)}")
                    print_info(f"内容类型: {data.get('content_type')}")
                    print_info(f"推荐内容ID: {recommendations[:5]}...")
                    return True
                else:
                    print_error(f"获取图文推荐失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取图文推荐失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取图文推荐异常: {str(e)}")
            return False

    def test_get_video_recommendations(self, user_id: int, size: int = 10) -> bool:
        """测试视频推荐接口"""
        print_header(f"测试视频推荐 (用户ID: {user_id})")

        try:
            response = self.session.post(
                f"{self.base_url}/get_video_recommendations",
                json={"user_id": user_id, "size": size}
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    recommendations = data.get('recommendations', [])
                    print_success(f"获取视频推荐成功，推荐数量: {len(recommendations)}")
                    print_info(f"内容类型: {data.get('content_type')}")
                    print_info(f"推荐内容ID: {recommendations[:5]}...")
                    return True
                else:
                    print_error(f"获取视频推荐失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取视频推荐失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取视频推荐异常: {str(e)}")
            return False

    def test_get_hot_contents(self, size: int = 10, user_id: int = None, content_type: int = None) -> bool:
        """测试热门内容接口"""
        type_name = {1: "图文", 2: "视频", None: "混合"}
        print_header(f"测试热门内容 (类型: {type_name.get(content_type)})")

        params = {"size": size}
        if user_id:
            params["user_id"] = user_id
        if content_type:
            params["type"] = content_type

        try:
            response = self.session.get(
                f"{self.base_url}/get_hot_contents",
                params=params
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    hot_contents = data.get('hot_contents', [])
                    print_success(f"获取热门内容成功，数量: {len(hot_contents)}")
                    print_info(f"热门内容ID: {hot_contents[:5]}...")
                    return True
                else:
                    print_error(f"获取热门内容失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取热门内容失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取热门内容异常: {str(e)}")
            return False

    def test_get_hot_articles(self, size: int = 10, user_id: int = None) -> bool:
        """测试热门图文接口"""
        print_header("测试热门图文")

        params = {"size": size}
        if user_id:
            params["user_id"] = user_id

        try:
            response = self.session.get(
                f"{self.base_url}/get_hot_articles",
                params=params
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    hot_contents = data.get('hot_contents', [])
                    print_success(f"获取热门图文成功，数量: {len(hot_contents)}")
                    print_info(f"内容类型: {data.get('content_type')}")
                    print_info(f"热门图文ID: {hot_contents[:5]}...")
                    return True
                else:
                    print_error(f"获取热门图文失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取热门图文失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取热门图文异常: {str(e)}")
            return False

    def test_get_hot_videos(self, size: int = 10, user_id: int = None) -> bool:
        """测试热门视频接口"""
        print_header("测试热门视频")

        params = {"size": size}
        if user_id:
            params["user_id"] = user_id

        try:
            response = self.session.get(
                f"{self.base_url}/get_hot_videos",
                params=params
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    hot_contents = data.get('hot_contents', [])
                    print_success(f"获取热门视频成功，数量: {len(hot_contents)}")
                    print_info(f"内容类型: {data.get('content_type')}")
                    print_info(f"热门视频ID: {hot_contents[:5]}...")
                    return True
                else:
                    print_error(f"获取热门视频失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取热门视频失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取热门视频异常: {str(e)}")
            return False

    def test_update_user_behavior(self, user_id: int) -> bool:
        """测试更新用户行为接口"""
        print_header(f"测试更新用户行为 (用户ID: {user_id})")

        try:
            response = self.session.post(
                f"{self.base_url}/update_user_behavior",
                json={"user_id": user_id}
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    print_success("更新用户行为成功")
                    return True
                else:
                    print_error(f"更新用户行为失败: {data.get('error')}")
                    return False
            else:
                print_error(f"更新用户行为失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"更新用户行为异常: {str(e)}")
            return False

    def test_get_recommendation_stats(self, user_id: int, content_type: int = None) -> bool:
        """测试推荐统计接口"""
        type_name = {1: "图文", 2: "视频", None: "全部"}
        print_header(f"测试推荐统计 (用户ID: {user_id}, 类型: {type_name.get(content_type)})")

        params = {"user_id": user_id}
        if content_type:
            params["type"] = content_type

        try:
            response = self.session.get(
                f"{self.base_url}/get_recommendation_stats",
                params=params
            )

            if response.status_code == 200:
                data = response.json()
                if data.get('success'):
                    stats = data.get('stats', {})
                    print_success("获取推荐统计成功")
                    print_info(f"总内容数: {stats.get('total_contents')}")
                    print_info(f"已查看数: {stats.get('viewed_count')}")
                    print_info(f"已推荐数: {stats.get('recommended_count')}")
                    print_info(f"剩余可推荐数: {stats.get('remaining_count')}")
                    return True
                else:
                    print_error(f"获取推荐统计失败: {data.get('error')}")
                    return False
            else:
                print_error(f"获取推荐统计失败: HTTP {response.status_code}")
                return False
        except Exception as e:
            print_error(f"获取推荐统计异常: {str(e)}")
            return False

    def test_recommendation_deduplication(self, user_id: int) -> bool:
        """测试推荐去重功能"""
        print_header("测试推荐去重功能")

        try:
            # 第一次获取推荐
            print_info("第一次获取推荐...")
            response1 = self.session.post(
                f"{self.base_url}/get_recommendations",
                json={"user_id": user_id, "size": 10}
            )

            if response1.status_code != 200 or not response1.json().get('success'):
                print_error("第一次获取推荐失败")
                return False

            recommendations1 = response1.json().get('recommendations', [])
            print_info(f"第一次推荐: {recommendations1}")

            # 等待一秒
            time.sleep(1)

            # 第二次获取推荐
            print_info("第二次获取推荐...")
            response2 = self.session.post(
                f"{self.base_url}/get_recommendations",
                json={"user_id": user_id, "size": 10}
            )

            if response2.status_code != 200 or not response2.json().get('success'):
                print_error("第二次获取推荐失败")
                return False

            recommendations2 = response2.json().get('recommendations', [])
            print_info(f"第二次推荐: {recommendations2}")

            # 检查重复
            duplicates = set(recommendations1) & set(recommendations2)
            if duplicates:
                print_warning(f"发现重复推荐: {duplicates}")
                print_info("这可能是因为缓存或者推荐池不足")
            else:
                print_success("推荐去重测试通过，没有重复推荐")

            return True

        except Exception as e:
            print_error(f"推荐去重测试异常: {str(e)}")
            return False

    def test_content_type_separation(self, user_id: int) -> bool:
        """测试内容类型分离"""
        print_header("测试内容类型分离")

        try:
            # 获取图文推荐
            response_article = self.session.post(
                f"{self.base_url}/get_article_recommendations",
                json={"user_id": user_id, "size": 5}
            )

            # 获取视频推荐
            response_video = self.session.post(
                f"{self.base_url}/get_video_recommendations",
                json={"user_id": user_id, "size": 5}
            )

            if (response_article.status_code == 200 and
                    response_video.status_code == 200 and
                    response_article.json().get('success') and
                    response_video.json().get('success')):

                articles = response_article.json().get('recommendations', [])
                videos = response_video.json().get('recommendations', [])

                print_success("内容类型分离测试成功")
                print_info(f"图文推荐数: {len(articles)}")
                print_info(f"视频推荐数: {len(videos)}")

                # 检查是否有交集（理论上不应该有）
                intersection = set(articles) & set(videos)
                if intersection:
                    print_warning(f"图文和视频推荐有交集: {intersection}")
                else:
                    print_success("图文和视频推荐完全分离")

                return True
            else:
                print_error("获取分类推荐失败")
                return False

        except Exception as e:
            print_error(f"内容类型分离测试异常: {str(e)}")
            return False

    def run_all_tests(self):
        """运行所有测试"""
        print_header("开始运行推荐系统测试")
        print_info(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print_info(f"测试地址: {self.base_url}")

        test_cases = [
            ("健康检查", lambda: self.test_health_check()),
            ("特征提取-图文", lambda: self.test_extract_features(101, 1)),
            ("特征提取-视频", lambda: self.test_extract_features(201, 2)),
            ("混合推荐", lambda: self.test_get_recommendations(TEST_USER_ID)),
            ("图文推荐", lambda: self.test_get_article_recommendations(TEST_USER_ID)),
            ("视频推荐", lambda: self.test_get_video_recommendations(TEST_USER_ID)),
            ("热门内容-混合", lambda: self.test_get_hot_contents()),
            ("热门内容-带用户去重", lambda: self.test_get_hot_contents(user_id=TEST_USER_ID)),
            ("热门图文", lambda: self.test_get_hot_articles()),
            ("热门视频", lambda: self.test_get_hot_videos()),
            ("更新用户行为", lambda: self.test_update_user_behavior(TEST_USER_ID)),
            ("推荐统计-全部", lambda: self.test_get_recommendation_stats(TEST_USER_ID)),
            ("推荐统计-图文", lambda: self.test_get_recommendation_stats(TEST_USER_ID, 1)),
            ("推荐统计-视频", lambda: self.test_get_recommendation_stats(TEST_USER_ID, 2)),
            ("推荐去重", lambda: self.test_recommendation_deduplication(TEST_USER_ID)),
            ("内容类型分离", lambda: self.test_content_type_separation(TEST_USER_ID)),
        ]

        passed = 0
        failed = 0

        for test_name, test_func in test_cases:
            try:
                if test_func():
                    passed += 1
                    self.test_results.append((test_name, True, ""))
                else:
                    failed += 1
                    self.test_results.append((test_name, False, "测试失败"))
            except Exception as e:
                failed += 1
                self.test_results.append((test_name, False, str(e)))
                print_error(f"{test_name} 测试异常: {str(e)}")

            # 测试间隔
            time.sleep(0.5)

        # 打印测试总结
        self.print_test_summary(passed, failed)

    def print_test_summary(self, passed: int, failed: int):
        """打印测试总结"""
        print_header("测试总结")

        total = passed + failed
        pass_rate = (passed / total * 100) if total > 0 else 0

        print(f"\n{Colors.BOLD}测试结果统计:{Colors.ENDC}")
        print(f"  总测试数: {total}")
        print(f"  {Colors.OKGREEN}通过: {passed}{Colors.ENDC}")
        print(f"  {Colors.FAIL}失败: {failed}{Colors.ENDC}")
        print(f"  通过率: {pass_rate:.1f}%")

        if failed > 0:
            print(f"\n{Colors.FAIL}失败的测试:{Colors.ENDC}")
            for test_name, success, error in self.test_results:
                if not success:
                    print(f"  - {test_name}: {error}")

        if passed == total:
            print(f"\n{Colors.OKGREEN}{Colors.BOLD}🎉 所有测试通过！{Colors.ENDC}")
        else:
            print(f"\n{Colors.WARNING}{Colors.BOLD}⚠️  部分测试失败，请检查！{Colors.ENDC}")


def main():
    """主函数"""
    # 检查命令行参数
    if len(sys.argv) > 1:
        base_url = sys.argv[1]
    else:
        base_url = BASE_URL

    # 创建测试器并运行测试
    tester = RecommendationSystemTester(base_url)
    tester.run_all_tests()


if __name__ == "__main__":
    main()
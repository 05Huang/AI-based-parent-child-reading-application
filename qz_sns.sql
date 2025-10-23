/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 90400 (9.4.0)
 Source Host           : localhost:3306
 Source Schema         : qz_sns

 Target Server Type    : MySQL
 Target Server Version : 90400 (9.4.0)
 File Encoding         : 65001

 Date: 21/10/2025 18:01:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_bot_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_bot_config`;
CREATE TABLE `ai_bot_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bot_user_id` bigint NOT NULL COMMENT 'AI机器人用户ID',
  `bot_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机器人名称',
  `bot_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机器人头像',
  `model_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'qwen' COMMENT 'AI模型类型 qwen:通义千问 ernie:文心一言',
  `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API密钥',
  `max_tokens` int NULL DEFAULT 2000 COMMENT '最大tokens',
  `temperature` decimal(3, 2) NULL DEFAULT 0.70 COMMENT '温度参数',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '系统提示词',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用 0:禁用 1:启用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bot_user`(`bot_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI机器人配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_bot_config
-- ----------------------------
INSERT INTO `ai_bot_config` VALUES (1, -1, '家庭AI助手', 'https://api.dicebear.com/7.x/bottts/svg?seed=aibot&backgroundColor=c7d2fe', 'qwen', NULL, 2000, 0.70, '你是一个温暖友善的家庭AI助手，你的任务是帮助家庭成员解答问题、提供建议，营造和谐的家庭氛围。回答要简洁、友好、富有温度。', 1, '2025-10-10 07:25:28', '2025-10-10 07:25:28');

-- ----------------------------
-- Table structure for ai_chat_record
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_record`;
CREATE TABLE `ai_chat_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户问题',
  `ai_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI回复',
  `tokens_used` int NULL DEFAULT 0 COMMENT '使用的token数',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group`(`group_id` ASC) USING BTREE,
  INDEX `idx_message`(`message_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI助手对话记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_chat_record
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID',
  `root_id` bigint NULL DEFAULT 0 COMMENT '根评论ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:删除',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `comment_type` tinyint(1) NULL DEFAULT 1 COMMENT '评论类型 1:普通评论 2:段落评论',
  `paragraph_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '段落ID(仅段落评论有值)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_content`(`content_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_paragraph`(`paragraph_id` ASC) USING BTREE COMMENT '段落评论索引'
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (4, 12, 2, 0, 0, '测试', 0, 1, 0, '2025-05-10 23:19:19', 1, NULL);
INSERT INTO `comment` VALUES (6, 12, 2, 0, 0, '测试', 1, 1, 1, '2025-05-11 20:52:23', 2, 'para-1746364185583-0');
INSERT INTO `comment` VALUES (7, 12, 2, 6, 6, '测试2', 1, 0, 0, '2025-05-11 21:08:54', 2, 'para-1746364185583-0');
INSERT INTO `comment` VALUES (8, 12, 2, 0, 0, '测试3', 1, 1, 1, '2025-05-11 21:13:34', 2, 'para-1746364185583-0');
INSERT INTO `comment` VALUES (9, 12, 2, 0, 0, '测试2', 1, 0, 1, '2025-05-11 21:18:27', 1, NULL);
INSERT INTO `comment` VALUES (10, 12, 2, 4, 4, '测试5', 0, 0, 0, '2025-05-11 21:18:49', 1, NULL);
INSERT INTO `comment` VALUES (11, 12, 2, 0, 0, '到底', 1, 1, 1, '2025-05-11 21:20:30', 2, 'para-1746364185583-24');
INSERT INTO `comment` VALUES (12, 12, 2, 11, 11, '测试6', 0, 0, 1, '2025-05-11 21:21:55', 2, 'para-1746364185583-24');
INSERT INTO `comment` VALUES (13, 12, 2, 0, 0, '测试😂', 1, 1, 1, '2025-05-24 22:30:04', 2, 'para-1746364185583-14');
INSERT INTO `comment` VALUES (14, 12, 2, 13, 13, '测2', 0, 0, 1, '2025-05-24 22:30:34', 2, 'para-1746364185583-14');
INSERT INTO `comment` VALUES (15, 12, 2, 0, 0, '测试', 0, 0, 1, '2025-05-24 22:31:00', 1, NULL);
INSERT INTO `comment` VALUES (16, 12, 3, 0, 0, '你说的对', 1, 0, 1, '2025-05-24 22:36:11', 2, 'para-1746364185583-0');
INSERT INTO `comment` VALUES (17, 12, 3, 8, 8, '你好，加个微信', 0, 0, 1, '2025-05-24 22:37:26', 2, 'para-1746364185583-0');
INSERT INTO `comment` VALUES (18, 13, 2, 0, 0, '测试', 0, 1, 1, '2025-05-25 10:03:57', 2, 'para-1748138393295-0');
INSERT INTO `comment` VALUES (19, 13, 2, 18, 18, '测试2', 1, 0, 1, '2025-05-25 10:04:12', 2, 'para-1748138393295-0');
INSERT INTO `comment` VALUES (20, 13, 2, 0, 0, '测试3', 0, 0, 1, '2025-05-25 10:04:28', 1, NULL);
INSERT INTO `comment` VALUES (21, 13, 2, 0, 0, '测试4', 0, 2, 1, '2025-05-25 10:04:32', 1, NULL);
INSERT INTO `comment` VALUES (23, 13, 2, 21, 21, '测试5', 1, 0, 1, '2025-05-25 10:04:53', 1, NULL);
INSERT INTO `comment` VALUES (24, 13, 2, 0, 0, '你好', 1, 2, 1, '2025-05-25 10:52:37', 2, 'para-1748138393295-0');
INSERT INTO `comment` VALUES (25, 13, 2, 21, 21, '测试6', 1, 0, 1, '2025-05-25 10:53:10', 1, NULL);
INSERT INTO `comment` VALUES (27, 12, 2, 0, 0, '这篇文章对微服务架构的讲解非常透彻，特别是关于服务间通信的部分，解决了我项目中的实际问题。期待作者更多的分享！', 0, 0, 1, '2025-05-25 14:14:01', 1, NULL);
INSERT INTO `comment` VALUES (28, 13, 2, 24, 24, 'hello', 0, 0, 1, '2025-05-25 20:54:07', 2, 'para-1748138393295-0');
INSERT INTO `comment` VALUES (29, 13, 3, 24, 24, 'hello ', 0, 0, 1, '2025-05-25 20:55:06', 2, 'para-1748138393295-0');
INSERT INTO `comment` VALUES (30, 13, 3, 0, 0, '我是二号', 1, 0, 1, '2025-05-25 20:55:23', 1, NULL);
INSERT INTO `comment` VALUES (31, 13, 3, 0, 0, '你好\n', 0, 0, 1, '2025-05-25 21:08:29', 2, 'para-1748138393295-0');
INSERT INTO `comment` VALUES (32, 43, 32, 0, 0, '写的真好', 0, 0, 1, '2025-09-24 13:34:37', 1, NULL);
INSERT INTO `comment` VALUES (33, 43, 32, 0, 0, '真棒', 0, 0, 1, '2025-09-24 13:36:30', 1, NULL);
INSERT INTO `comment` VALUES (34, 39, 32, 0, 0, '真好', 1, 1, 1, '2025-09-24 13:47:30', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (35, 39, 32, 0, 0, '@黄暄 是的', 1, 2, 1, '2025-09-24 13:48:26', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (36, 39, 32, 0, 0, 'yes', 0, 0, 1, '2025-09-24 14:03:05', 2, 'para-1758687682842-2');
INSERT INTO `comment` VALUES (38, 48, 32, 0, 0, '你好', 1, 1, 1, '2025-09-24 17:19:53', 1, NULL);
INSERT INTO `comment` VALUES (39, 39, 32, 0, 0, '那个', 1, 1, 1, '2025-09-27 15:05:40', 1, NULL);
INSERT INTO `comment` VALUES (40, 39, 32, 35, 35, '@黄暄 对应', 0, 2, 1, '2025-09-27 16:27:52', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (41, 39, 32, 40, 35, '@黄暄 哈哈', 1, 0, 1, '2025-09-27 16:28:06', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (42, 39, 32, 34, 34, '@黄暄 是的', 0, 1, 1, '2025-09-27 16:28:26', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (43, 39, 32, 42, 34, '@黄暄 刚刚', 0, 0, 1, '2025-09-27 16:28:32', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (44, 39, 32, 40, 35, '哈哈', 0, 1, 1, '2025-09-27 16:35:54', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (45, 39, 32, 35, 35, '哈哈', 0, 0, 1, '2025-09-27 16:36:01', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (46, 39, 32, 44, 35, '很好', 1, 0, 1, '2025-09-27 16:43:09', 2, 'para-1758687682842-0');
INSERT INTO `comment` VALUES (47, 39, 32, 39, 39, '是的', 0, 1, 1, '2025-09-27 16:45:21', 1, NULL);
INSERT INTO `comment` VALUES (48, 39, 32, 47, 39, '哈哈', 0, 0, 1, '2025-09-27 16:45:29', 1, NULL);
INSERT INTO `comment` VALUES (49, 48, 32, 38, 38, '哈哈', 0, 0, 1, '2025-09-27 16:49:10', 1, NULL);
INSERT INTO `comment` VALUES (50, 70, 32, 0, 0, '好', 0, 1, 1, '2025-09-30 12:31:35', 1, NULL);
INSERT INTO `comment` VALUES (51, 70, 32, 50, 50, '是的', 0, 0, 1, '2025-09-30 12:31:40', 1, NULL);
INSERT INTO `comment` VALUES (52, 68, 32, 0, 0, '说的对', 0, 0, 1, '2025-10-04 13:48:01', 1, NULL);
INSERT INTO `comment` VALUES (53, 68, 32, 0, 0, '哈哈😀😀😀😀😀😀😑', 1, 0, 1, '2025-10-04 14:00:45', 1, NULL);
INSERT INTO `comment` VALUES (54, 68, 32, 0, 0, '说的话😶😝😝', 0, 0, 1, '2025-10-04 14:03:15', 1, NULL);
INSERT INTO `comment` VALUES (55, 64, 32, 0, 0, '很好😄', 0, 1, 1, '2025-10-04 14:45:53', 1, NULL);
INSERT INTO `comment` VALUES (56, 64, 32, 55, 55, '是的', 1, 0, 1, '2025-10-04 14:46:29', 1, NULL);
INSERT INTO `comment` VALUES (57, 69, 32, 0, 0, '很好😗', 0, 0, 1, '2025-10-04 15:51:10', 2, 'para-1759235299705-0');
INSERT INTO `comment` VALUES (58, 65, 32, 0, 0, '你好', 0, 0, 1, '2025-10-04 16:04:06', 1, NULL);
INSERT INTO `comment` VALUES (59, 69, 32, 0, 0, '是的', 0, 0, 1, '2025-10-05 03:52:47', 1, NULL);
INSERT INTO `comment` VALUES (60, 66, 32, 0, 0, '是的', 0, 0, 1, '2025-10-05 04:27:44', 1, NULL);
INSERT INTO `comment` VALUES (61, 71, 32, 0, 0, '很好', 0, 0, 1, '2025-10-05 06:06:38', 2, 'para-1759588190397-2');
INSERT INTO `comment` VALUES (62, 61, 32, 0, 0, '很好', 0, 0, 1, '2025-10-05 08:02:34', 1, NULL);
INSERT INTO `comment` VALUES (63, 71, 32, 0, 0, '很好', 0, 1, 1, '2025-10-05 13:08:59', 2, 'para-1759588190397-10');
INSERT INTO `comment` VALUES (64, 71, 32, 63, 63, '很哈', 0, 0, 1, '2025-10-05 13:09:05', 2, 'para-1759588190397-10');

-- ----------------------------
-- Table structure for content
-- ----------------------------
DROP TABLE IF EXISTS `content`;
CREATE TABLE `content`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '富文本内容',
  `type` tinyint(1) NOT NULL COMMENT '类型 1:图文 2:视频',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `media_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '媒体URL',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `share_count` int NULL DEFAULT 0 COMMENT '分享数',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:下架',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `click_through_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '点击率',
  `avg_view_duration` int NULL DEFAULT 0 COMMENT '平均观看时长(秒)',
  `completion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '完成率',
  `content_length` int NULL DEFAULT 0 COMMENT '内容长度(字数/秒数)',
  `last_recommended` datetime NULL DEFAULT NULL COMMENT '最后推荐时间',
  `recommendation_score` decimal(6, 4) NULL DEFAULT 0.0000 COMMENT '综合推荐分数',
  `popularity_score` decimal(6, 4) NULL DEFAULT 0.0000 COMMENT '热度得分',
  `recommended_count` int NULL DEFAULT 0 COMMENT '被推荐次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_recommendation_score`(`recommendation_score` DESC) USING BTREE,
  INDEX `idx_content_length`(`content_length` ASC) USING BTREE,
  INDEX `idx_status_score`(`status` ASC, `recommendation_score` DESC) USING BTREE,
  INDEX `idx_type_category`(`type` ASC, `category_id` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of content
-- ----------------------------
INSERT INTO `content` VALUES (59, '习近平的读书之道', '国家广播电视总局 优秀网络视听作品展播《2023年第二季度推优》', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/59/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/59/video.mp4', 5, '时政', 3, 0, 0, 0, 1, -999, '2025-09-30 19:37:30', '2025-10-04 14:42:11', 0.0000, 0, 0.0000, 145, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (60, '世界读书日', '我们为什么要读书?读书，使我们的灵魂变得充盈，让我们拥有更多勇气面对人生起伏，今天是第27个世界读书日。你，为什么而读书呢?', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/60/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/60/video.mp4', 6, '读书日', 3, 0, 0, 0, 1, -999, '2025-09-30 19:38:09', '2025-10-04 14:44:44', 0.0000, 0, 0.0000, 83, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (61, '和女儿一起阅读', '为了丫头的任务，母亲也是尽力了日，要的时间紧，不然可以去图书馆拍', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/61/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/61/video.mp4', 4, '亲子阅读', 6, 1, 1, 0, 1, -999, '2025-09-30 19:38:46', '2025-10-05 08:02:34', 0.0000, 0, 0.0000, 245, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (62, '《做一个最好的你》', '《给孩子读诗》一书中选了这首诗来亲子共读，做一个最好的自己，主题也很适合送给孩子小学里第二次亲子作品合作，临时决定赶出来的，补光背景诵读都来不及做到心中完美，但也当一个成长回忆了!', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/62/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/62/video.mp4', 8, '#亲子共读#亲子阅读#阅读节#亲子朗诵 #做一个最好的你', 3, 0, 0, 0, 1, -999, '2025-09-30 19:39:33', '2025-10-07 14:02:10', 0.0000, 0, 0.0000, 85, '2025-10-07 21:57:49', 0.0000, 0.0000, 10);
INSERT INTO `content` VALUES (63, '共建书香社会 共享现代文明', '共建书香社会 共享现代文明', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/63/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/63/video.mp4', 9, '宣传', 2, 0, 0, 0, 1, -999, '2025-09-30 19:39:54', '2025-10-07 15:54:28', 0.0000, 0, 0.0000, 180, '2025-10-07 23:54:28', 0.0000, 0.0000, 19);
INSERT INTO `content` VALUES (64, '全民阅读公益宣传片', '在最快的时代，给自己最慢的角落；阅读不是任务，是好奇心的自然生长。阅读不是一种姿态，而是一种生活的本能。让我们一起阅读吧。', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/64/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/64/video.mp4', 7, '全名阅读', 15, 0, 2, 0, 1, -999, '2025-09-30 19:40:29', '2025-10-21 07:23:16', 0.0000, 0, 0.0000, 68, '2025-10-07 21:57:49', 0.0000, 0.0000, 10);
INSERT INTO `content` VALUES (65, '亲子阅读：共沐书香 陪伴成长——我国亲子阅读的现状调查及思考', '<p id=\"para-1759234577413-0\"><strong data-spm-anchor-id=\"C73544894212.P26598934187.0.i2\">&nbsp; &nbsp; &nbsp; &nbsp;亲子阅读，是以书为媒、以阅读为纽带，让孩子和家长共享多种形式的阅读过程，也是让孩子爱上阅读的最有效方式之一。“家庭是人生的第一所学校，家长是孩子的第一任老师，要给孩子讲好‘人生第一课’，帮助扣好人生第一粒扣子。”2018年，习近平总书记在全国教育大会上发表重要讲话，深刻诠释了家庭教育的重要意义。2021年，国务院颁布了《中国儿童发展纲要（2021—2030）》，提出进一步加强亲子阅读指导、培养儿童良好阅读习惯、分年龄段推荐优秀儿童书目等要求。当前，我国亲子阅读有何新趋势？尚存哪些短板？光明日报记者会同中国传媒大学国家艺术基金“以美为媒：中国原创绘本精品巡展”课题组，对我国亲子阅读的现状进行调研，并在此基础上提出形成良性共读生态圈的对策建议。</strong></p>\n<p id=\"para-1759234577413-1\"><strong data-spm-anchor-id=\"C73544894212.P26598934187.0.i2\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"http://127.0.0.1:9000/qz-sns/image/article/65/59f27c14-d003-4766-9211-f3eb5eeb9ff2.jpg\" width=\"499\" height=\"332\"></strong></p>\n<p id=\"para-1759234577413-2\"><strong data-spm-anchor-id=\"C73544894212.P26598934187.0.i2\"><strong data-spm-anchor-id=\"C73544894212.P26598934187.0.i4\">&nbsp; &nbsp; &nbsp; &nbsp; 万家灯火时的书桌前，孩子与大人相互依偎，感受着角色朗读的快乐；假期的图书馆里，儿童文学大家与绘本作者相聚于此，为读者家庭阐释着童书里的奥妙；街角社区的绘本馆内，大人小孩围坐一团，认真聆听着导读师的讲解……书为孩子们的世界打开了一扇窗，而“亲子阅读”则成为他们与世界联通的重要桥梁。</strong>&nbsp;</strong></p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/65/5be36c6b-bc4e-4317-b7cc-a920b46f873a.jpg', NULL, 5, '亲子阅读', 6, 1, 1, 0, 1, -999, '2025-09-30 20:16:17', '2025-10-05 07:45:09', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (66, '亲子阅读的深意与诗意', '<p style=\"list-style-type: none;\" id=\"para-1759234949816-0\">&nbsp; &nbsp; &nbsp; &nbsp;用机器给孩子阅读好吗？晚上读还是白天读？孩子喜欢看电视（电脑），不喜欢看书，怎么办？怎样理解幼儿读物的教育性与艺术性？如今，越来越多的新手父母意识到亲子阅读对在孩子成长中的必要性，但和孩子一起阅读，还需要大人付出时间、精力和智慧。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759234949816-1\">针对不同年龄段的孩子，究竟应该从何处抓起？如何有针对性地选择阅读书目，在亲子共读中又有哪些实用技巧？这些都是困扰大家的难题。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759234949816-2\">《阅读是最美的礼物：0-6岁亲子阅读指南》由知名儿童文学、儿童阅读专家赵霞和方卫平共同创作，针对家长、幼儿园教师及各类早教机构在 0—6 岁儿童早期阅读中存在的常见困惑予以专业、细致的解答。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759234949816-3\">&nbsp; &nbsp; &nbsp; &nbsp;近期，中国作家网编辑就本书的创作初衷、特点等问题采访了作者赵霞，在她看来，“谈论幼儿阅读跟谈论幼年、谈论儿童文学一样，看似只是谈论个体发展或文学生活中的一个细小片段，实则牵一发而动全身”，谨严和慎重是他们试图通过本书传达的一种观念与精神。<img src=\"http://127.0.0.1:9000/qz-sns/image/article/66/6598ee9b-4771-4955-99e2-b68d4e3c47e6.jpg\">&nbsp; &nbsp; &nbsp; &nbsp; 而和孩子一起读书，我们成人的生命也会经历新的拓展和反思，经受新的丰富与蜕变。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759234949816-4\">&nbsp; &nbsp; &nbsp;<strong style=\"list-style-type: none;\">赵霞：</strong>从事儿童文学及阅读研究多年，我们遇到过许多对儿童阅读怀有热忱的家长和老师。与他们的交流常常让我们感到，儿童文学是一个跟我们的日常生活联系多么密切的文类。这种联系的缘分因孩子而起，然而，一旦我们开始和孩子一起阅读童书，就会发现自己不知不觉地深深卷入其中——因为儿童文学是一个如此充满乐趣的世界，也因为和孩子一起读书是一件如此快乐的事情。由此，成人与孩子之间相互带动，使得亲子阅读成为了一项日益普及的当代家庭和校园活动。&nbsp;</p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/66/c465c8e7-6a9d-4f69-991b-2a0bc6d67e1e.jpg', NULL, 5, '书籍推荐', 10, 1, 1, 0, 1, -999, '2025-09-30 20:22:30', '2025-10-05 07:45:10', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (67, '亲子阅读是孩子成长过程中最好的陪伴方式', '<p id=\"para-1759235048316-0\"><img src=\"http://127.0.0.1:9000/qz-sns/image/article/67/58c97143-202f-43ea-ba45-8e8ab07618b6.jpg\"></p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-1\"><strong style=\"list-style-type: none;\">一、问卷调查概况</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-2\">新春佳节，平日里忙碌的家长有更多和孩子相处的机会，也是共读一本好书、共享阅读带来的收获和心灵愉悦的好时光。假期里亲子阅读情况究竟怎样？2023年春节前夕，《中国新闻出版广电报》面向幼儿园大中小班、小学一至六年级的学生家长发放《春节假期亲子阅读情况家长调查问卷》。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-3\">《调查问卷》涉及家长对于亲子阅读所关心的20个问题，自1月17日15时上线发布，截至1月27日15时，总计收回问卷556份，参与作答的家长覆盖全国31个省、直辖市、自治区（不包括港澳台）。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-4\">近日，本报记者对556份《调查问卷》作了认真细致的分析，形成了本报告。从家庭中孩子的性别看，男孩占51.62%、女孩占48.38%；从亲子阅读的年龄看，幼儿园阶段的孩子占26.08%（其中小班9.53%，中班6.12%，大班10.43%）；小学阶段的孩子占73.92%（其中小学一至三年级占24.82%，小学四至六年级占49.1%）。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-5\"><strong style=\"list-style-type: none;\">二、《报告》显示出10个结论</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-6\">1.亲子阅读在家庭日常生活中占有重要地位。《报告》显示，认为亲子阅读十分必要的家长占比84.53%；有时间就陪伴的占比14.93%；无所谓的占比0.36%；仅有一位家长认为没必要。这从家长对于“春节假期期间进行亲子阅读的频率”回复中可看出。在这一问中，每天坚持亲子阅读的家长占27.7%；一周4至5次的占22.3%；一周2至3次的占30.76%；一周一次的占14.21%；4个数字相加，体现了94.97%的家长一周之内至少有一次陪伴孩子亲子阅读。可见开展亲子阅读的必要性获得绝大多数家长认可与重视。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-7\">需注意的是，在家庭中基本没有亲子阅读行为的家长占5.03%。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-8\">2.读屏时代纸质图书仍最受青睐。《报告》显示，有80.4%的家长在亲子阅读中首选纸质书。这与纸质图书拥有翻页、触摸纸张、讨论内容等更多肢体与语言互动功能，以及对孩子眼睛更加友好等原因有关。对比之下，选择电子图书的家长仅占0.9%；纸质、电子两者都认可的家长占18.71%。反映出家长对于用数字化方式开展亲子阅读还需要一个接受过程。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-9\">在家庭拥有童书数量上，究竟该给孩子购买多少本图书合适？《报告》显示：选择50本以下的占24.1%；50至100本的家长占36.87%；100至200本的占20.86%；200本以上的占18.17%。前三个数字相加显示，超过80%的家长认为孩子拥有图书200本以内即可。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-10\">3.亲子阅读每次60分钟内为好。对于亲子阅读时长问题，选择60分钟以内的家长占比75.72%，其中30分钟以内的家长占比最高，为45.86%。《报告》显示，母亲陪伴孩子进行亲子阅读占比62.05%，家长共同参与的占比25.36%，父亲参与占比9.89%，祖辈参与有2.7%的占比。母亲成为亲子阅读的“主力”，可能在图书选择、阅读知识、阅读方式、阅读效果等方面，母亲更具有选择权与决定权。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-11\">记者交叉对比分析发现，随着孩子年龄增长，阅读频率虽然有所下降，但阅读时长有所提升，这意味着阅读深度不断增加，对家长陪伴的时间与精力提出更高要求。如小学四年级至六年级孩子选择“每天”进行亲子阅读的，在问卷设计的所有年龄段占比最低，但每次阅读“60分钟及以上”的则占比最高。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-12\">4.故事类图书成为亲子阅读首选。该给孩子读什么书也是家长们共同关心的问题。《报告》显示：故事类图书、图画书绘本、知识类图书占据前三的位置，分别占比69.78%、59.35%、51.8%，排第四位的名著类也受到家长和孩子的欢迎，占比42.45%。也有家长选择历史类、人物传记类以及动漫游戏类图书开展亲子阅读。从孩子年龄层来看，图画书绘本、立体书随着年龄增长阅读比例有所降低，名著则随年龄增长阅读比例有所增加。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-13\">5.假期近90%的家长乐于带孩子去书店或参加亲子阅读活动。《报告》显示：有88.31%的家长带孩子去书店买书或参加书店的亲子阅读活动。有11.69%的家长选择不去书店，理由包括：“疫情因素不想带孩子去公共场所”“没时间，怕聚集”“家里有很多书，未发现值得一去的活动”“回老家”“电商购书，孩子害羞不爱参加活动”等。这些因素提示书店后疫情时代应不断提高服务质量，一方面为读者寻找图书提供便利，另一方面要在活动设计上不断增强针对性与吸引力，满足读者多样化需求。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-14\">6.家长选书最看重内容。对于选择亲子阅读图书的标准，《报告》显示内容是家长最为看重的因素，占比70.79%；其次是孩子的阅读兴趣占比34.37%；随后依次为作者、出版社、装帧设计和价格。从选书的渠道来看，家长越来越重视孩子的自主选择，通常认为行之有效的家长间口口相传，已不再是家长们选书的主要参考因素。在图书内容和图书价格这两项一最高一最低之间，从过去图书价格普遍为家长首选项降至最后考虑因素，意味着随着我国经济的增长，家庭收入也在日益增长，家长更舍得为孩子阅读投入。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-15\">7.近80%的家长比较了解或非常了解亲子阅读重要性。亲子阅读，顾名思义就是以书为媒，以阅读为纽带，让孩子和家长共同分享多种形式的阅读过程。这一过程的直接效果是父母和孩子共读一本书时，很容易让孩子觉得读书是一件非常快乐的事情，进而更愿意自发地进行阅读。《报告》显示：有51.26%的家长选择比较了解亲子阅读重要性；有27.16%的家长选择非常了解；一般了解和不了解的分别为19.42%、2.16%；最后两个数字合计21.58%，说明4位家长中，就有1位家长对亲子阅读的重要性不甚了解，对这部分家长应加强有针对性的宣传，使亲子阅读行之有效、更加深入人心。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-16\">8.超过95%的家长掌握亲子阅读方法。对于亲子阅读的方法，家长们可谓各有所能。《报告》显示：选择原文朗读的最多，占比67.27%；选择对孩子阅读中提出的疑问进行解释的占比56.47%；共同讨论交流读后感的占比50.9%；根据孩子理解能力改编书中内容并讲述的占比36.33%；与孩子开展故事表演、阅读旅行等实践活动的占比仅14.39%。选择自创“妙招”的占比2.16%，分别是：生活中遇到一些话题或知识，回到家后把相关的书找出来，陪孩子再读一遍，并尝试让她（他）自己讲述（亲测有效）；探讨书中阐述的观点并延伸至内在的文化属性等方面问题；边读边讲解提问；给孩子讲过之后，让孩子根据书本讲故事；家长和孩子自己读自己的书等。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-17\">交叉对比分析显示，原文朗读是各年龄段均适用的亲子阅读方式。综合不同年龄层来看，幼儿园孩子家长较常根据孩子理解能力改编书中内容并讲述，小学孩子家长较常对孩子阅读中提出的疑问进行解释。本题目也显示出，家长开展亲子阅读依然以图书为主要依托，形式还有待进一步拓展。比如只有14.39%的家长选择与孩子开展故事表演、阅读旅行等实践活动。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-18\">9.家长没时间是阻碍亲子阅读的主要原因。家庭中难以开展亲子阅读的主要原因，首位的是家长没有时间陪伴；其次是不知道如何选择合适的图书。此外，不了解亲子阅读的方法与孩子喜欢独立阅读或不配合家长也是令家长头疼的问题。《报告》显示，家长迫切需要“来自外力”的亲子阅读方面的指导，排在首位的是学校老师指导，其他包括希望图书中能附带亲子阅读方法，通过参加书店或图书馆举办的亲子阅读活动得到指导，媒体加大对亲子阅读宣传力度等。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-19\">10.亲子阅读是家长在孩子成长过程中最好的陪伴方式。如何看待亲子阅读的益处？有325份答卷（占比63.85%）认同亲子阅读是家长在孩子成长过程中最好的陪伴方式。相比之下，帮助孩子养成爱读书的习惯（94份、占比19.11%）、有助于培养亲密和谐与共同成长的亲子关系（77份、占比15.94%）、扩展知识面与提高孩子学习成绩（31份、占比6.84%）、帮助孩子形成健康向上的人格（15份、占比3.51%）、增强孩子理解分析能力（14份、占比3.28%）均在100份之内。由此可见，家长们意识到孩子成长过程中精神陪伴比物质满足更为重要。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-20\"><strong style=\"list-style-type: none;\">三、家长和孩子心目中的好书有哪些特点</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-21\">本次问卷设计了“请写出一本让您印象深刻的亲子阅读图书书名，并简要说明原因”的开放性问题。556位家长提到的300多种中外图书既有共性也有个性。其中，有29位家长提及《西游记》，居首位；有18位提到《三国演义》、13位提到《十万个为什么》、12位提到《狼王梦》、8位提到“米小圈”系列图书、7位提到《史记》、各有5位提到《红楼梦》《水浒传》、4位提到《山海经》、各有3位提到《骆驼祥子》《天空100层的房子》《淘气包马小跳》，还有5位家长提到四大名著，可见文学作品在亲子阅读中颇受欢迎，特别是古典名著“一马当先”。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-22\">从国外作品来看，有18位家长提到《父与子》，列首位。8位提到《格林童话》、各有7位提到《神奇校车》《猜猜我有多爱你》、各有6位提到《海底两万里》《小王子》、各有5位提到《哈利·波特》《不一样的卡梅拉》、各有4位提到《昆虫记》《DK百科系列》、各有3位提到《伊索寓言》《我爸爸》《小鸡球球》《假如给我三天光明》。这些图书内容广泛，包括文学、童话、科普、励志等，形式也更为多样，涵盖漫画、绘本、自传等。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-23\">兴趣是最好的老师。从家长填写的上述图书入选的理由来看，有31位提到“很喜欢”，列首位。其他原因还包括图文有趣、内容通俗易懂、画面色彩丰富、故事贴近日常、想象力丰富、能够培养品质等。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759235048316-24\">综合梳理这些图书所具备的特点包括如下几个方面：一是家长有回忆、有共鸣，比如《葫芦兄弟》《没头脑和不高兴》等，是家长的儿时回忆，讲给孩子效果也很好。二是家长和孩子都能从中学到知识，可以共同提高。比如《十万个为什么》《写给儿童的中国历史》《神奇校车》，和孩子一起探讨书中的问题，既有益于密切亲子关系，也是共读共进步的好选择。三是书的内容与形式兼美，比如《DK奇妙动物大百科》《不可思议的旅程》等，画面清晰、排版精美、设计精妙，能够在幼儿的专注力范围内提供合适的信息量。四是能够引发孩子实践探索的欲望，比如《会响的小路》描写大自然之美，让孩子读后想去户外探索；《折纸飞机的男孩：钱学森的故事》激发孩子对科学的热爱，有了当科学家的梦想；《国宝流失百年祭》让孩子在亲子阅读后主动上网查找更多信息并与家长分享；读完《我的幸运一天》，孩子邀请家长一起扮演，还买了手偶在幼儿园表演，感受到阅读带来的满足感。五是对科学育儿有所帮助，如《大喊大叫的一天》有助于缓和紧张的亲子关系；《逃跑小豌豆》让孩子学习逆境中的抗击打能力；《我不喜欢被欺负》帮助孩子独立正确处理好与同学的关系；《孩子，我们牵手看世界》锻炼孩子与人沟通的技巧以及直面困难的勇气等。六是在家长引导下挑战阅读提升能力，如有的家长选择《西行漫记》《额尔古纳河右岸》以及文言文版的四大名著等。七是有助于学习的图书，比如《牛津阅读树》等英语学习类图书等。</p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/67/9821c0c8-f77c-4474-bcf5-3e080e83eb63.jpg', NULL, 3, '陪伴方式', 10, 1, 0, 0, 1, -999, '2025-09-30 20:24:08', '2025-10-05 07:45:11', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (68, '大手牵小手 相伴共阅读｜亲子阅读怎么读？这7点一定要抓住！', '<p id=\"para-1759235158607-0\">0-6岁是幼儿大脑发育、语言能力发展的关键时期，在此期间，亲子阅读是培养孩子成为终身阅读者的最有效手段。如何和孩子一起进行亲子阅读呢？小斑斑来给大家介绍一些注意点和实用的技巧！</p>\n<p id=\"para-1759235158607-1\">不要急于求成</p>\n<p id=\"para-1759235158607-2\">即便是已经学会了一些基础的拼音与汉字，也不能要求孩子马上独立阅读。</p>\n<p id=\"para-1759235158607-3\">因为此时孩子还没有建立起完备的语言系统，他们认识中的汉字是相对独立的，还无法进行独立思考。</p>\n<p id=\"para-1759235158607-4\">而亲子阅读是培养独立阅读的最有效方法，因为孩子在父母的陪伴下，在一起阅读的过程中，培养了语感和理解故事的逻辑思考能力，待时机成熟，阅读成为一种习惯，他们自然会拿起书自己阅读。<img src=\"http://127.0.0.1:9000/qz-sns/image/article/68/f2554f75-3780-4671-830b-17074c7b31c9.jpg\"></p>\n<p id=\"para-1759235158607-5\">亲子阅读是一件很难坚持的事情，可能父母们因为生活和工作的琐事，很少有富余的时间陪伴孩子一起阅读。</p>\n<p id=\"para-1759235158607-6\">但亲子阅读最重要的一点就是坚持，只要把它看成是一件很重要的事情，每天至少找出空闲的十分钟，陪伴孩子看完一个童话故事，或是绘本的一部分，都是帮助孩子养成阅读习惯的关键。<img src=\"http://127.0.0.1:9000/qz-sns/image/article/68/2ad53ac8-8979-424b-88e6-f55df78f6268.jpg\">只要一直坚持，就能引起孩子的阅读兴趣，比如今天阅读了某个故事的一角，第二天他们一定会追着问故事究竟是如何发展的。</p>\n<p id=\"para-1759235158607-7\">&nbsp;</p>\n<p id=\"para-1759235158607-8\">如何选书</p>\n<p id=\"para-1759235158607-9\">不同年龄段的孩子理解能力不同，选择的书籍也要有所不同。两周岁左右的孩子，形象思维尚未建立，认知书、简单易懂的儿歌，更适合他们。</p>\n<p id=\"para-1759235158607-10\">3岁以上，孩子可以读各种故事书、科普书，有趣的故事和科普事件有利于激发他们的阅读的欲望。</p>\n<p id=\"para-1759235158607-11\"><img src=\"http://127.0.0.1:9000/qz-sns/image/article/68/d2c53704-805e-4fc7-8de1-8bdb562c378b.jpg\"></p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/68/1d1565d8-34d9-4a17-ab12-4e9204871b9b.jpg', NULL, 5, '亲子阅读', 31, 0, 3, 0, 1, -999, '2025-09-30 20:25:58', '2025-10-07 14:12:40', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (69, '五部门联合印发意见 开展“书香飘万家”全国家庭亲子阅读行动', '<p id=\"para-1759235299705-0\">本报讯（记者 林焕新）近日，全国妇联、中宣部、教育部、文化和旅游部、国务院妇儿工委办公室联合印发《关于开展“书香飘万家”全国家庭亲子阅读行动的实施意见》，对深入学习宣传贯彻党的二十大精神和习近平总书记重要指示精神，深化开展“书香飘万家”全国家庭亲子阅读行动作出部署。</p>\n<p id=\"para-1759235299705-1\">　　意见指出，各地各相关部门要认真落实立德树人根本任务，聚焦在家庭中培育和践行社会主义核心价值观，深入实施亲子阅读行动，广泛宣传亲子阅读理念，创新提供阅读指导服务，大力弘扬家庭阅读风尚；激励引导儿童从小养成良好阅读习惯，爱读书、读好书、善读书，立志为中华民族伟大复兴而读书。</p>\n<p id=\"para-1759235299705-2\">　　意见要求，各地各相关部门要加强家庭亲子阅读指导服务，培育亲子阅读书香家庭典型，设立家庭亲子阅读体验基地，发展家庭亲子阅读指导队伍，打造家庭亲子阅读活动品牌，开发家庭亲子阅读服务产品，帮助家长儿童养成良好习惯，掌握科学方法，培育阅读风尚，在全社会营造人人爱阅读、家家有书香的良好环境。</p>\n<p id=\"para-1759235299705-3\">　　意见强调，各地各相关部门要特别关注农村留守流动儿童、困境儿童及其家庭，持续开展阅读公益项目，丰富儿童精神文化生活，传递党和政府的关怀温暖。</p>\n<p id=\"para-1759235299705-4\">　　据悉，2023年全国家庭亲子阅读行动将以“书香飘万家 阅启新征程”为主题，上下联动开展一系列丰富多彩的阅读活动，将在广大家庭中掀起亲子阅读新高潮。</p>\n<p id=\"para-1759235299705-5\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"http://127.0.0.1:9000/qz-sns/image/article/69/c92fe5dc-f359-43e0-9330-00375666723c.jpg\"></p>\n<p style=\"text-align: center;\" id=\"para-1759235299705-6\">扫一扫分享本页</p>\n<p id=\"para-1759235299705-7\">&nbsp;</p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/69/e4871536-93d5-4bdf-b7b5-43964c733ef3.jpg', NULL, 3, '政策', 14, 0, 2, 0, 1, -999, '2025-09-30 20:28:20', '2025-10-21 09:59:29', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (70, '30本适合亲子阅读的书籍，家长们快收藏起来！', '<p id=\"para-1759235427573-0\">为了让孩子享受到阅读的乐趣，真正地喜欢上阅读，现在的家长越来越认识到亲子阅读的重要性。亲子阅读可以培养孩子阅读的兴趣和习惯，提高阅读能力，同时也能增进父母与孩子之间的情感交流，及时了解孩子的心理活动，进行有益的正面引导，但适合亲子阅读的书籍也是至关重要的。</p>\n<p id=\"para-1759235427573-1\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"http://127.0.0.1:9000/qz-sns/image/article/70/b5b55779-347b-4c18-a2fa-f31089c617d9.jpg\"></p>\n<p id=\"para-1759235427573-2\">今天，饭妈为大家找来了30本适合亲子阅读的书籍，希望家长朋友们能从中挑选几本，与孩子进行一次亲密的亲子阅读，尽享天伦之乐！</p>\n<p id=\"para-1759235427573-3\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"http://127.0.0.1:9000/qz-sns/image/article/70/b7dcea3c-cd34-483c-b482-bc302dc749ef.jpg\"></p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/70/5befee1f-18a4-47bf-b979-22e92468ab76.png', NULL, 9, '书籍推荐', 54, 0, 2, 0, 1, -999, '2025-09-30 20:30:27', '2025-10-05 07:45:14', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (71, '第二十二次全国国民阅读调查成果发布', '<p style=\"list-style-type: none;\" id=\"para-1759588190397-0\"><span style=\"list-style-type: none;\">2025年4月24日上午，在第四届全民阅读大会上，中国新闻出版研究院发布了第二十二次全国国民阅读调查成果。</span></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-1\"><strong style=\"list-style-type: none;\">一、本次阅读调查的基本情况</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-2\">自1999年起，由中国新闻出版研究院组织实施的全国国民阅读调查已持续开展了二十一次。第二十二次全国国民阅读调查于2024年8月启动， 2024年10月至2025年2月同步开展样本采集、数据复核工作，2025年3月至4月开展数据处理和数据分析工作。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-3\">本次调查方式以网络在线调查和电话调查为主，线下邀约线上填答为辅，在全国189个城市进行样本采集，覆盖我国31个省、自治区、直辖市。本次调查共回收样本57.35万个，其中18周岁及以上成年人样本占到总样本量的78.1%，0—17周岁未成年人样本占到总样本量的21.9%，城乡样本比例为3.2:1。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-4\">样本回收后，我们运用SPSS社会学统计软件进行分析。本次调查经过加权可推及我国人口13.48亿，其中城镇居民占63.9%，农村居民占36.1%。本次调查的主要发现如下：</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-5\"><strong style=\"list-style-type: none;\">二、本次阅读调查的主要发现</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-6\"><strong style=\"list-style-type: none;\">（一）2024年我国成年国民各媒介综合阅读率小幅增长，图书阅读率稳步提升</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-7\">2024年我国成年国民包括书报刊和数字出版物在内的各种媒介的综合阅读率为82.1%，较2023年的81.9%提升了0.2个百分点。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-8\">2024年我国成年国民图书阅读率为59.9%，较2023年的59.8%增长了0.1个百分点；报纸阅读率为22.5%，较2023年的23.1%下降了0.6个百分点；期刊阅读率为17.1%，较2023年的17.5%下降了0.4个百分点；数字化阅读方式（电脑端网络在线阅读、手机阅读、电子阅读器阅读、Pad阅读、听书、视频讲书等）的接触率为80.6%，较2023年的80.3%增长了0.3个百分点。<img style=\"width: 800px; display: block; margin-left: auto; margin-right: auto;\" title=\"2.png\" src=\"https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611550752506.png\" alt=\"\" data-uploadpic=\"U020250424565389841441_ORIGIN.png\" data-needdownload=\"true\"></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-9\"><strong style=\"list-style-type: none;\">（二）数字化阅读重塑成年国民阅读习惯，近八成的人通过手机阅读</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-10\">从成年国民对各类数字化阅读载体的接触情况来看，手机阅读和听书等数字化阅读方式持续深入成年国民的生活，通过电脑端进行网络在线阅读和电子阅读器阅读的比例较上年有所下降。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-11\">具体来看，2024年有78.7%的成年国民进行过手机阅读，较2023年的78.3%增长了0.4个百分点；有38.5%的成年国民通过听书的方式进行阅读，较2023年的36.3%增长了2.2个百分点；23.6%的成年国民使用Pad（平板电脑）进行阅读，较2023年的22.5%增长了1.1个百分点；有5.7%的成年国民通过视频讲书的方式进行阅读，较2023年的4.4%增长了1.3个百分点。<img style=\"width: 800px; display: block; margin-left: auto; margin-right: auto;\" title=\"3.png\" src=\"https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611550860295.png\" alt=\"\" data-uploadpic=\"U020250424565596006197_ORIGIN.png\" data-needdownload=\"true\"></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-12\">此外，2024年有68.3%的成年国民通过电脑端进行网络在线阅读，较2023年的70.6%下降了2.3个百分点；有23.9%的成年国民在电子阅读器上阅读，比2023年的25.3%减少了1.4个百分点。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-13\"><strong style=\"list-style-type: none;\">（三）成年国民纸质图书和电子书阅读量双增长，报纸期刊阅读量持续下滑</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-14\">从成年国民对各类出版物阅读量的考察看，2024年我国成年国民的纸质图书阅读量和电子书阅读量均较上年有所提升。2024年，我国成年国民的人均纸质图书阅读量为4.79本，较2023年的4.75本增加了0.04本。人均电子书阅读量为3.52本，高于2023年的3.40本。人均纸质图书和电子书阅读量合计为8.31本，高于2023年的8.15本。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-15\"><img style=\"width: 800px; display: block; margin-left: auto; margin-right: auto;\" title=\"4.png\" src=\"https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611550968955.png\" alt=\"\" data-uploadpic=\"U020250424565914853297_ORIGIN.png\" data-needdownload=\"true\"></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-16\">纸质报刊阅读量均较上年有不同程度的下降。2024年，我国成年国民的人均纸质报纸阅读量为12.41期（份），较2023年的13.34期（份）减少了0.93期（份）；人均纸质期刊阅读量为1.73期（份），较2023年的1.89期（份）减少了0.16期（份）。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-17\">2024年我国成年国民中，有13.2%的人年均阅读10本及以上纸质图书，较2023年的12.3%增长了个0.9百分点；有10.0%的人年均阅读10本及以上电子书，高于2023年的9.9%。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-18\"><strong style=\"list-style-type: none;\">（四）成年国民人均每天数字化媒介接触时长超三小时，传统纸质媒介阅读时长仅半小时</strong></p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-19\">在传统纸质媒介中，成年国民人均每天读书时间最长。2024年我国成年国民人均每天阅读纸质图书24.41分钟，比2023年的23.38分钟增加了1.03分钟；人均每天阅读报纸4.17分钟，较2023年的4.97分钟减少了0.8分钟；人均每天阅读期刊2.85分钟，略低于2023年的3.12分钟。</p>\n<p style=\"list-style-type: none;\" id=\"para-1759588190397-20\">在数字化媒介中，2024年我国成年国民人均每天手机接触时长为108.76分钟，比2023年的106.52分钟增加了2.24分钟；人均每天通过电脑端网络在线阅读时长为63.76分钟，较2023年的65.14分钟减少了1.38分钟；人均每天电子阅读器阅读时长为10.29分钟，略低于2023年的10.33分钟；人均每天Pad（平板电脑）阅读时长为9.62分钟，高于2023年的9.11分钟。<img style=\"width: 800px; display: block; margin-left: auto; margin-right: auto;\" title=\"5.png\" src=\"https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611551076247.png\" alt=\"\" data-uploadpic=\"U020250424566255666557_ORIGIN.png\" data-needdownload=\"true\"></p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/71/e4aa315d-0b6b-455c-aad8-cc4ccfb9bbd2.png', NULL, 1, '调查报告', 75, 1, 3, 0, 1, -999, '2025-10-04 22:29:50', '2025-10-05 13:29:25', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (72, '新华网文化观察丨阅读是每个人的“终身大事”', '<p id=\"para-1759588448739-0\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_202504230a58e9c79e7d4cc185c28964e84b299d.jpg\" data-material-id=\"2025042303815545\" data-name=\"202504230a58e9c79e7d4cc185c28964e84b299d.jpg\"></p>\n<p id=\"para-1759588448739-1\">&nbsp; &nbsp; &nbsp; &nbsp; 新华网北京4月23日电 <strong>题：阅读是每个人的“终身大事”</strong></p>\n<p id=\"para-1759588448739-2\">  新华网记者李欣 王坤朔 钱芳</p>\n<p id=\"para-1759588448739-3\">  4月23日，是第30个世界读书日。今年世界读书日的主题是“阅读：通往未来的桥梁”，倡导通过阅读开阔视野、传递智慧，为构建更美好的明天提供精神动力。</p>\n<p id=\"para-1759588448739-4\">  在人工智能兴起的当下，人们的阅读习惯发生哪些变化？在时代变迁中，读者的阅读兴趣何在？我们该如何通过阅读这座桥梁，走向未来？<img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423af21ceb95d6c4b4696d9460d7eca086b.jpg\" data-material-id=\"2025042300880031\" data-name=\"20250423af21ceb95d6c4b4696d9460d7eca086b.jpg\"></p>\n<p id=\"para-1759588448739-5\"><strong>从手不释卷到数字视听</strong></p>\n<p id=\"para-1759588448739-6\">  清晨的上海地铁，找一个位置站定，打开包里大约2斤重的“大部头”沉浸到文字中，这是在上海从事品牌工作的一村每天通勤路上的“固定动作”。</p>\n<p id=\"para-1759588448739-7\">  千里之外的北京，去年刚大学毕业的小吴，也会在上下班的地铁上捧着纸质书。“看纸质书的感觉更有仪式感，也更专注。”小吴说，“我习惯拿笔随手记录，纸质书对我来说更方便。”</p>\n<p id=\"para-1759588448739-8\">  当前，纸质图书仍是出版主流，并在人们的阅读生活占据一席之地。</p>\n<p id=\"para-1759588448739-9\">  最新数据显示，2024年江苏居民综合阅读率达90.3%，人均纸质图书阅读量增至5.5本；《广州市“图书馆之城”建设年度报告2024》显示，2024年读者人均阅读纸质图书23.98本。</p>\n<p id=\"para-1759588448739-10\">  随着数字化浪潮涌动，手机阅读、听书、视频讲书等形式，日益融入生活，成为公众习惯的阅读新方式。</p>\n<p id=\"para-1759588448739-11\">  电子书在年轻群体中更为普遍。以阅文平台为例，2024年，年龄在30岁以下的读者在新增用户中占比近4成，全年人均在读作品数从28部提升至31部。</p>\n<p id=\"para-1759588448739-12\">  “便携、高效。”在北师大艺术学研究生小林看来，这是电子书最突出的两个优点，她可以在看书的时候随手截图存档，查找内容时也比纸质书更方便。此外，电子书的社交属性也很吸引她。她说：“在一些读书类App上，能看到朋友们正在读的书和笔记。”</p>\n<p id=\"para-1759588448739-13\">  程序员羡辙的经历，则更有数字时代的特色。因为喜爱阅读，但苦于平台众多、笔记散落，她用近两年时间开发了一款可跨平台整合笔记的读书APP“众山小”。惊喜的是，这款APP不仅是阅读助手，也让她看到了用户阅读量的增长，2020年，人均读完的图书数量和阅读时长是5本、20小时，2022到2024年，这组数据达到了10本左右、40-50小时，“在这个短视频争夺用户时间的时代，阅读时长的提升非常可贵。”羡辙说。</p>\n<p id=\"para-1759588448739-14\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_202504233f626bdf51f642c2b94a74d43cc97f2a.jpg\" data-material-id=\"2025042304454517\" data-name=\"202504233f626bdf51f642c2b94a74d43cc97f2a.jpg\"></p>\n<p id=\"para-1759588448739-15\">数字阅读并非独属年轻人的专利。在媒体工作了二十余年的方先生，曾多年从事外文报道工作，阅读英文原版电子书已成为他生活中的一部分。“读原版书有种‘原汁原味’的感觉，‘翻译是遗憾的艺术’，还是想直观地了解作者本来的意思。”</p>\n<p id=\"para-1759588448739-16\">  除便携外，方先生说：“像《耶路撒冷三千年》这类‘大部头’，纸质书看起来‘有压力’，电子书看不到具体厚度，反而利用碎片化时间看完了。”</p>\n<p id=\"para-1759588448739-17\">  “各种形式的书都会看。”国际关系学院青年教师张潇爽的读书习惯，是当今多元阅读方式的一个缩影。“日常工作用到的书比较多，因此会大量阅读更便携的电子书，但经常用到的书会习惯看纸质的。平时也会听书，以休闲娱乐为主。”她说。<img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423c0498a604f334d79a2bf1e532b3edc1d.jpg\" data-material-id=\"2025042301736742\" data-name=\"20250423c0498a604f334d79a2bf1e532b3edc1d.jpg\"></p>\n<p id=\"para-1759588448739-18\">面对阅读方式的变化，图书馆、出版社、书店等也在积极拥抱新技术，探索数字内容创新。比如，国家图书馆为读者提供了十万册近年出版的电子书阅读服务，并打造了“M·地铁图书馆”等数字阅读品牌。</p>\n<p id=\"para-1759588448739-19\">  近年来，人民文学出版社数字业务收入每年保持20%以上的增速，目前拥有可运营数字资源2000多种，有声资源300多种。在总编辑吴良柱看来，“纸质书和电子书之间是互补关系，互相促进。”</p>\n<p id=\"para-1759588448739-20\"><strong>  从时代热点到经典回归</strong></p>\n<p id=\"para-1759588448739-21\">  “碎片化阅读确实影响了读书习惯，近年来中短篇小说集、散文集渐成热销态势。”吴良柱说，“但只要是优质的作品，不论是长篇巨制还是名家短篇，都是正向的。像莫言新作《晚熟的人》、阿来的《去有风的旷野》、迟子建的《好时光悄悄溜走》都是非常优质的短篇集。”</p>\n<p id=\"para-1759588448739-22\">  阅读书单也是探寻时代脉动的体现。在加速演进的世界变局中，国际政治、经济、科技等无疑是从学界到民间都关注的话题。</p>\n<p id=\"para-1759588448739-23\">  在方先生的电子阅读器中，基辛格的《论中国》是他最近正在阅读的书目。最近，他还特意找来《美国官僚政治：政府机构的行为及其动因》这本书。中信出版社编辑介绍，3月出版的新书《论美国》近期也广受关注。</p>\n<p id=\"para-1759588448739-24\">  4月21日，记者来到位于西单的北京图书大厦。在“新书推荐”书架上，不少读者在阅读《中国经济2025》《经济新动能》《大出海》《锚定中国》以及人工智能应用方面的新书。</p>\n<p id=\"para-1759588448739-25\">  “老一辈革命家精神图谱”“创新引领高质量发展”“名家名作经典传承”等专柜也都在显眼位置，吸引读者驻足阅览。工作人员告诉记者，最近一段时间，来寻找新质生产力、低空经济、人工智能方面图书的读者比较多。</p>\n<p id=\"para-1759588448739-26\">  中信出版社2023年8月出版的《奥本海默传》，随着电影《奥本海默》热映，到今年恰逢抗战胜利80周年，一直保持着较高的话题度，销量稳定。在编辑姜楠眼中，这位“原子弹之父”的故事不仅见证了过去，也关乎现在，还提醒着未来。</p>\n<p id=\"para-1759588448739-27\">  此外，随着电影《哪吒之魔童闹海》、游戏《黑神话:悟空》火爆，《哪吒三界往事》《黑神话悟空：遇见山西》书籍等也在电商排行榜前列。</p>\n<p id=\"para-1759588448739-28\">  与快节奏阅读相对的是，在数字平台上正掀起经典阅读热潮。2024年，阅文平台上，《三国演义》《西游记》等名著位列年增速最快的前十大出版书，反映出当代年轻人对于传统文化与自我成长的阅读需求。</p>\n<p id=\"para-1759588448739-29\">  无独有偶，人民文学出版社的销售和阅读数据显示，近一年来严肃文学与经典作品在强势回归，迟子建的《额尔古纳河右岸》、梁晓声的《人世间》等，长期占据京东、当当等电商平台销售榜单前列。“青年群体正通过经典寻求精神共鸣与价值观构建。”总编辑吴良柱说。</p>\n<p id=\"para-1759588448739-30\">  今年3月的北京图书市集上，南京大学出版社“守望者”“折射集”等经典丛书品牌热度不减。该社媒体宣传部副主任戚宛珺认为，“读者渴望从中寻求观察世界、理解社会的新视角与新方法。在阅读中不仅收获知识，更期望获得对生活的深刻洞察与启示”。</p>\n<p id=\"para-1759588448739-31\">  当下的中国故事不仅是全民阅读，更是全球阅读。</p>\n<p id=\"para-1759588448739-32\">  截至2024年底，阅文集团向海外推出了6800部中国网文翻译作品，覆盖200多个国家与地区，起点国际访问用户累计近3亿。去年,阅文又有10部网文作品入藏大英图书馆。“网络文学为中华文化的国际传播搭建了一座桥梁。”阅文集团相关负责人说。</p>\n<p id=\"para-1759588448739-33\"><strong>  从阅读场景到文化地标</strong></p>\n<p id=\"para-1759588448739-34\">  随着阅读习惯的变化，与阅读相关的实体阅读空间也在悄然焕新，向多元文化空间和文化地标转型。</p>\n<p id=\"para-1759588448739-35\">  沉浸式的体验是实体阅读空间所不可替代的核心功能。在国家图书馆·国家典籍博物馆，“中华优秀传统文化典籍”“甲骨文记忆”等展览，让公众近距离感受典籍穿越历史的魅力。</p>\n<p id=\"para-1759588448739-36\">  在北京城市图书馆，超挑高的山间阅览区用一座书山展示知识文汇和数字图景，读者仰望间可感受文脉传承的震撼。<img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423612df811fd36412bb4cbb4d088da0ce6.jpg\" data-material-id=\"2025042305421512\" data-name=\"20250423612df811fd36412bb4cbb4d088da0ce6.jpg\"></p>\n<p id=\"para-1759588448739-37\">首都图书馆中，《本草纲目》《天工开物》等古籍内容经典场景被做成裸眼3D版，读者可在智慧桌面上直接“触摸”立体影像。</p>\n<p id=\"para-1759588448739-38\">  “作为一个城市的图书馆，能够支持这座城市的创新，是非常重要的。”近日，北京大学信息管理系主任、教授张久珍在演讲中说，“到实体空间来，它能够建立人与人的连接。我们看到，有非常多的中年人在图书馆得到治愈，也有一些老年人更愿意到图书馆来做志愿者，获得价值感。”</p>\n<p id=\"para-1759588448739-39\">  除了图书馆，近年来“书店+”业态兴起，越来越多的书店从“体验感”出发，打造更多元场景。</p>\n<p id=\"para-1759588448739-40\">  “书店也是在营造一个文化场域，寻找和自己同频的读者。”中华书局书店业务部负责人吴魏说，“书店应该成为一个文化地标，不是拍照打卡的地标，而是深度挖掘所在城市、街区的文化内涵，展现文化底蕴与文化形象的地标。”<img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423047b468e13374db88f80b86050b32997.jpg\" data-material-id=\"2025042302685484\" data-name=\"20250423047b468e13374db88f80b86050b32997.jpg\"></p>\n<p id=\"para-1759588448739-41\">他介绍，中华书局的灿然书屋最近和同街区的老舍纪念馆联动，以盖章打卡的形式，让游客重走老舍从丹柿小院到首都剧场上班这条路线。据介绍，灿然书屋收集了大量老舍相关的旧书进行展示，有一位天津读者，翻到了自己小学图书馆收藏过的老舍旧书《鼓书艺人》，这是旧书带给人的独特情感链接，他当即就把这本书收藏了。</p>\n<p id=\"para-1759588448739-42\">  “打造舒适的阅读场景，举办作家分享和签售活动、讲座、艺术沙龙、主题读书会、快闪活动等，建立专属服务社群等，都是增强读者黏性的重要方式。”PAGEONE书店总经理陈鹏介绍，书店每年都会举办几百场文化活动。他认为，实体书店有着网络平台和数字阅读都比不上的体验优势。</p>\n<p id=\"para-1759588448739-43\">  “书店作为阅读空间，应该是优质阅读资源的汇聚地、多元文化交流碰撞的空间、城市人文精神滋养的角落、读者心灵栖息的家园。”陈鹏说。</p>\n<p id=\"para-1759588448739-44\">  值得关注的是，随着全民阅读的推进，不少“家门口”的新型文化空间悄然生长，形成了颇具特色的“最后一公里”文化服务圈。</p>\n<p id=\"para-1759588448739-45\">  以坐落在北京广阳谷城市森林公园的广阳书院为例，这一隐藏在闹市区的多元文化空间，阅读中心是其中最重要的板块之一，社科生活、少儿类图书最受欢迎，每周三到周日还会开放公益绘本课。<img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423303e3ba29a56455694e632c1bc0e88a8.jpg\" data-material-id=\"2025042303526677\" data-name=\"20250423303e3ba29a56455694e632c1bc0e88a8.jpg\"></p>\n<p id=\"para-1759588448739-46\">&nbsp;</p>\n<p id=\"para-1759588448739-47\">广阳书院运营总监倪嘉甫认为，在数字化阅读的潮流下，更需倡导深度阅读，特别是对儿童阅读来说，线下阅读可成长为终生受用的学习方式。</p>\n<p id=\"para-1759588448739-48\"><strong>  阅读是每个人的“终身大事”</strong></p>\n<p id=\"para-1759588448739-49\">  每年世界读书日前，文化学者、南昌交通学院特聘教授朱华祥都会给学生们做读书讲座。“数字化让年轻一代随时随地可以找到想要阅读的图书，但青涩是学生的常态，我期望通过倡导读书，让他们沿着读书的路径，由青涩走向成熟。”朱华祥说。</p>\n<p id=\"para-1759588448739-50\">  “读书让我学到许多知识。”这是5岁的佳遇小朋友对阅读的理解。</p>\n<p id=\"para-1759588448739-51\">  “阅读是灵魂在杰作中奔跑，看到有限生命之外的无限世界。”中国政法大学新闻学研究生赵同学说。</p>\n<p id=\"para-1759588448739-52\">  “在AI时代，更需要阅读，保持欣赏和独立思考的能力，保持世界变局中的定力。”媒体人方先生说。</p>\n<p id=\"para-1759588448739-53\">  “活着总要好奇世界吧？读书是成本最低的环球旅行。”河南某大学医学专业退休教授蒋先生说。</p>\n<p id=\"para-1759588448739-54\">  首都图书馆馆长毛雅君认为，持续学习、终身学习越来越成为当下人们的需求。“一个人的精神发展史，应该是他的阅读史；一个民族的精神境界，应该是全民的阅读水平。”毛雅君认为，倡导阅读应从0岁开始直至终身。</p>\n<p id=\"para-1759588448739-55\">  4月21日，国家图书馆揭晓第二十届文津图书。《满世界寻找敦煌》《风起云飞扬：钱乘旦讲大国崛起》《为国铸盾：中国原子弹之路》《有趣的中国古建筑》等20种文津图书和46种文津提名图书脱颖而出。</p>\n<p id=\"para-1759588448739-56\">  值得关注的是，今年共计685种少儿类图书申报文津图书，是总参评图书中分量极重的一部分，反映出图书市场仍然对童书出版极为重视。作为评委代表的海飞表示，“童书，是下一代读者的起点。我们的使命，就是让孩子们开始享受阅读、爱上阅读，未来从阅读中获益。”</p>\n<p id=\"para-1759588448739-57\">  同日，喜马拉雅儿童App联合江西新华发行集团发起公益活动，打造“AI换声·爸妈分身”服务，用数字技术为留守儿童搭建亲子共读的桥梁。</p>\n<p id=\"para-1759588448739-58\">  去年5月，随着同名迷你剧热播，由李娟所著的《我的阿勒泰》在全国热销。 爆款影视剧不仅让原著触达更广泛的读者群体，让读者从不同角度感受作品的魅力，还进一步放大了优质内容的商业价值，从而激励更多创作者专注打磨精品。</p>\n<p id=\"para-1759588448739-59\">  “‘以IP改编反哺阅读’的模式，是全民阅读推广的供给侧创新。”阅文集团相关负责人说。</p>\n<p id=\"para-1759588448739-60\">  不仅仅是供给端在发力。深度参与南京全民阅读工作的金陵科技学院人文学院副院长赵步阳认为，推动全民阅读的着力点在于“积极促进作家与读者之间的同频共振”。他以自己参加的诗歌读享会为例，表示这种集体共读，由读者现场诵读，诗人和嘉宾进行释读、讨论，不流于形式，让阅读更见成效。</p>\n<p id=\"para-1759588448739-61\">  为推广经典文学作品，人民文学出版社每周四晚七点，邀请知名学者、专家做“朝内166文学讲座”。这一公益栏目已举办了12年。“阅读是点亮人类精神之光的火种，推广全民阅读则是守护文明火种的集体行动。”吴良柱说。</p>\n<p id=\"para-1759588448739-62\">  自2014年以来，“全民阅读”已连续12次写入政府工作报告。从“倡导全民阅读”到“深入推进全民阅读”，再到如今的“深化全民阅读活动”，全民阅读在国家文化发展战略中的重要性不断提升。</p>\n<p id=\"para-1759588448739-63\">  阅读，既是关乎个人兴趣的“小事”，也是关乎整个社会发展的“大事”。</p>\n<p id=\"para-1759588448739-64\">  （策划：林明；参与报道：李珊珊 崔雯雯 胡阳 林芷含）</p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/72/7aae398c-1371-400c-82b5-ffd7f28b7c7a.jpg', NULL, 2, '阅读的重要性', 16, 1, 0, 0, 1, -999, '2025-10-04 22:34:09', '2025-10-05 13:29:22', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (73, '激发孩子的阅读乐趣：家长指南', '<h2>引言</h2><p>在这个信息爆炸的时代，培养孩子对书籍的热爱变得尤为重要。通过亲子共读，不仅能够增进家庭成员之间的情感联系，还能促进孩子语言能力的发展，为未来的学习奠定坚实的基础。本文将向家长们分享几个实用建议，帮助您与孩子一起享受阅读的美好时光。</p><h2>创造舒适的阅读环境</h2><p>首先，营造一个适合阅读的空间是非常重要的。这个空间应该安静、光线充足，并且配备有适合儿童使用的书架或储物箱来存放他们喜爱的书籍。当孩子们拥有自己的小角落时，他们会更愿意花时间去探索书中的世界。</p><h2>选择合适的读物</h2><ul><li><strong>了解兴趣：</strong> 仔细观察并询问您的孩子最感兴趣的主题是什么？是恐龙、太空探险还是童话故事？根据他们的偏好挑选相关书籍。</li><li><strong>难度适宜：</strong> 确保所选图书既不会过于简单导致无聊，也不会太难而让人感到沮丧。随着年龄增长调整材料难度，保持挑战性但又不至于让孩子失去信心。</li></ul><h2>制定规律的读书时间</h2><p>每天设定一段固定的时间作为“家庭阅读时刻”，比如睡前半小时。这有助于形成良好的习惯，同时也让孩子们期待这一段特别的时光。在阅读过程中，家长可以采用轮流朗读的方式增加互动性，或者针对书中内容提问以提高参与度。</p><h2>成为榜样</h2><p>最后但同样重要的是，父母自身也需要表现出对于阅读的热情。当孩子们看到大人也沉浸在书海中时，自然会被这种积极的态度所感染。此外，还可以通过参观图书馆、参加书店活动等形式进一步拓宽视野，激发更多关于知识的好奇心。</p>', 1, 'http://127.0.0.1:9000/qz-sns/image/article/73/article_cover.jpg', NULL, 8, '亲子阅读,阅读兴趣,家庭教育', 0, 0, 0, 0, 1, 1, '2025-10-07 15:48:32', '2025-10-07 15:52:38', 0.0000, 0, 0.0000, 0, NULL, 0.0000, 0.0000, 0);
INSERT INTO `content` VALUES (74, '亲子厨房：和孩子一起做思维训练', '<h2>亲子厨房：和孩子一起做思维训练</h2>\n<p>在这个快节奏的时代，亲子阅读成为了家庭教育中不可或缺的一环。通过阅读，孩子们不仅能够学习知识，更能与父母建立深厚的情感纽带。</p>\n<p>营造良好的阅读氛围同样重要。一个安静舒适的阅读角落，充足的光线，以及丰富的图书资源，都能激发孩子的阅读兴趣。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/74/article_image_2.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>在阅读过程中，家长的陪伴和引导起着关键作用。不仅要读给孩子听，更要鼓励孩子表达自己的想法和感受，培养其独立思考的能力。</p>\n<p>选择适合孩子年龄段的读物至关重要。对于学龄前儿童，绘本是最好的选择；对于小学生，可以逐步引入儿童文学作品；而对于青少年，则可以尝试更加多元化的阅读材料。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/74/article_image_1.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>让我们一起行动起来，用阅读点亮孩子的未来，用书香浸润家庭的每一个角落。从今天开始，每天陪孩子读一本好书吧！</p>\n', 1, 'http://127.0.0.1:9000/qz-sns/image/article/74/article_cover.jpg', NULL, 7, '食谱分享,亲子厨房,美食制作,成长', 4581, 203, 298, 113, 1, 1, '2025-10-21 09:53:33', '2025-10-21 09:53:33', 0.1135, 0, 0.6550, 326, '2025-10-19 17:53:34', 0.3934, 0.4071, 214);
INSERT INTO `content` VALUES (75, '专注力食谱：简单又美味', '<h2>专注力食谱：简单又美味</h2>\n<p>在阅读过程中，家长的陪伴和引导起着关键作用。不仅要读给孩子听，更要鼓励孩子表达自己的想法和感受，培养其独立思考的能力。</p>\n<p>让我们一起行动起来，用阅读点亮孩子的未来，用书香浸润家庭的每一个角落。从今天开始，每天陪孩子读一本好书吧！</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/75/article_image_2.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>营造良好的阅读氛围同样重要。一个安静舒适的阅读角落，充足的光线，以及丰富的图书资源，都能激发孩子的阅读兴趣。</p>\n<p>研究表明，从小培养孩子的阅读习惯，对其语言能力、思维能力和想象力的发展都有着深远的影响。每天坚持半小时的亲子阅读时间，将成为孩子一生的财富。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/75/article_image_1.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>在这个快节奏的时代，亲子阅读成为了家庭教育中不可或缺的一环。通过阅读，孩子们不仅能够学习知识，更能与父母建立深厚的情感纽带。</p>\n', 1, 'http://127.0.0.1:9000/qz-sns/image/article/75/article_cover.jpg', NULL, 6, '教育资讯,院校选择,学业规划', 1730, 85, 30, 76, 1, 1, '2025-10-21 09:53:38', '2025-10-21 09:53:38', 0.1925, 0, 0.5877, 316, '2025-10-14 17:53:38', 0.3820, 0.3699, 300);
INSERT INTO `content` VALUES (76, '艺术启蒙培训：让孩子赢在起跑线', '<h2>艺术启蒙培训：让孩子赢在起跑线</h2>\n<p>营造良好的阅读氛围同样重要。一个安静舒适的阅读角落，充足的光线，以及丰富的图书资源，都能激发孩子的阅读兴趣。</p>\n<p>研究表明，从小培养孩子的阅读习惯，对其语言能力、思维能力和想象力的发展都有着深远的影响。每天坚持半小时的亲子阅读时间，将成为孩子一生的财富。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/76/article_image_2.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>在这个快节奏的时代，亲子阅读成为了家庭教育中不可或缺的一环。通过阅读，孩子们不仅能够学习知识，更能与父母建立深厚的情感纽带。</p>\n<p>选择适合孩子年龄段的读物至关重要。对于学龄前儿童，绘本是最好的选择；对于小学生，可以逐步引入儿童文学作品；而对于青少年，则可以尝试更加多元化的阅读材料。</p>\n', 1, 'http://127.0.0.1:9000/qz-sns/image/article/76/article_cover.jpg', NULL, 6, '升学指导,学业规划,教育资讯', 642, 90, 26, 9, 1, 1, '2025-10-21 09:53:43', '2025-10-21 09:53:43', 0.0577, 0, 0.6270, 282, '2025-10-16 17:53:44', 0.3942, 0.4720, 133);
INSERT INTO `content` VALUES (77, '亲子共读：编程思维主题书单推荐', '<h2>亲子共读：编程思维主题书单推荐</h2>\n<p>让我们一起行动起来，用阅读点亮孩子的未来，用书香浸润家庭的每一个角落。从今天开始，每天陪孩子读一本好书吧！</p>\n<p>选择适合孩子年龄段的读物至关重要。对于学龄前儿童，绘本是最好的选择；对于小学生，可以逐步引入儿童文学作品；而对于青少年，则可以尝试更加多元化的阅读材料。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/77/article_image_2.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>营造良好的阅读氛围同样重要。一个安静舒适的阅读角落，充足的光线，以及丰富的图书资源，都能激发孩子的阅读兴趣。</p>\n<p>在这个快节奏的时代，亲子阅读成为了家庭教育中不可或缺的一环。通过阅读，孩子们不仅能够学习知识，更能与父母建立深厚的情感纽带。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/77/article_image_1.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>研究表明，从小培养孩子的阅读习惯，对其语言能力、思维能力和想象力的发展都有着深远的影响。每天坚持半小时的亲子阅读时间，将成为孩子一生的财富。</p>\n<p>在阅读过程中，家长的陪伴和引导起着关键作用。不仅要读给孩子听，更要鼓励孩子表达自己的想法和感受，培养其独立思考的能力。</p>\n', 1, 'http://127.0.0.1:9000/qz-sns/image/article/77/article_cover.jpg', NULL, 9, '职业发展,个人成长,技能学习,综合素质,能力培养,家庭', 2402, 178, 196, 111, 1, 1, '2025-10-21 09:53:47', '2025-10-21 09:53:47', 0.2130, 0, 0.4451, 397, '2025-10-20 17:53:48', 0.4471, 0.6241, 32);
INSERT INTO `content` VALUES (78, '在线学习：数学思维技能提升指南', '<h2>在线学习：数学思维技能提升指南</h2>\n<p>在这个快节奏的时代，亲子阅读成为了家庭教育中不可或缺的一环。通过阅读，孩子们不仅能够学习知识，更能与父母建立深厚的情感纽带。</p>\n<p>研究表明，从小培养孩子的阅读习惯，对其语言能力、思维能力和想象力的发展都有着深远的影响。每天坚持半小时的亲子阅读时间，将成为孩子一生的财富。</p>\n<p><img src=\"http://127.0.0.1:9000/qz-sns/image/article/78/article_image_2.jpg\" alt=\"配图\" style=\"max-width:100%;height:auto;\"/></p>\n<p>在阅读过程中，家长的陪伴和引导起着关键作用。不仅要读给孩子听，更要鼓励孩子表达自己的想法和感受，培养其独立思考的能力。</p>\n<p>营造良好的阅读氛围同样重要。一个安静舒适的阅读角落，充足的光线，以及丰富的图书资源，都能激发孩子的阅读兴趣。</p>\n', 1, 'http://127.0.0.1:9000/qz-sns/image/article/78/article_cover.jpg', NULL, 4, '技能培训,在线教育,学习资源', 897, 54, 60, 32, 1, 1, '2025-10-21 09:53:54', '2025-10-21 09:53:54', 0.2450, 0, 0.4463, 265, '2025-10-17 17:53:54', 0.4072, 0.4994, 123);
INSERT INTO `content` VALUES (79, '探索世界：英语启蒙的人文风景 - 视频教程', '<p>这是一个关于旅游的精彩视频内容，适合家长和孩子一起观看学习。</p>', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/79/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/79/video.mp4', 2, '自然探索,旅行攻略,文化体验,家庭出游,亲子旅行,亲子', 8086, 356, 1045, 236, 1, 1, '2025-10-21 09:53:58', '2025-10-21 09:53:58', 0.1110, 8, 0.8000, 10, '2025-10-17 17:53:58', 0.5398, 0.6217, 495);
INSERT INTO `content` VALUES (80, '健康饮食：英语启蒙的营养价值 - 视频教程', '<p>这是一个关于美食的精彩视频内容，适合家长和孩子一起观看学习。</p>', 2, 'http://127.0.0.1:9000/qz-sns/video/resource/80/video_cover.jpg', 'http://127.0.0.1:9000/qz-sns/video/resource/80/video.mp4', 7, '儿童营养,美食制作,食谱分享,健康饮食,亲子厨房,家庭', 2957, 255, 328, 104, 1, 1, '2025-10-21 09:54:02', '2025-10-21 09:54:02', 0.3106, 8, 0.7273, 11, '2025-10-18 17:54:03', 0.5884, 0.6811, 433);

-- ----------------------------
-- Table structure for content_category
-- ----------------------------
DROP TABLE IF EXISTS `content_category`;
CREATE TABLE `content_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of content_category
-- ----------------------------
INSERT INTO `content_category` VALUES (1, '科技前沿', 3, 1);
INSERT INTO `content_category` VALUES (2, '旅游', 0, 1);
INSERT INTO `content_category` VALUES (3, '科技资讯', 1, 1);
INSERT INTO `content_category` VALUES (4, '教育培训', 2, 1);
INSERT INTO `content_category` VALUES (5, '教育', 1, 1);
INSERT INTO `content_category` VALUES (6, '升学', 2, 1);
INSERT INTO `content_category` VALUES (7, '美食', 3, 1);
INSERT INTO `content_category` VALUES (8, '在线学习', 4, 1);
INSERT INTO `content_category` VALUES (9, '技能提升', 5, 1);

-- ----------------------------
-- Table structure for content_feature
-- ----------------------------
DROP TABLE IF EXISTS `content_feature`;
CREATE TABLE `content_feature`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `text_vector` json NULL COMMENT '文本向量(处理后的特征)',
  `keyword_vector` json NULL COMMENT '关键词向量',
  `extracted_time` datetime NULL DEFAULT NULL COMMENT '特征提取时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_content`(`content_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 135 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容特征表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of content_feature
-- ----------------------------
INSERT INTO `content_feature` VALUES (119, 72, '[-0.4598519802093506, 0.28671571612358093, -0.25528937578201294, 0.4994991719722748, 0.7947233319282532, -0.21991033852100372, -0.46841540932655334, 0.05852673947811127, 0.1238672062754631, 0.37369534373283386, 0.17564786970615387, 0.3591592609882355, 0.0368497371673584, 0.04964566230773926, 0.627533495426178, 0.10133744031190872, -0.029976636171340942, 2.4878857135772705, -0.08213233947753906, -0.4616115987300873, 0.15344196557998657, 0.3675371706485748, -0.14307691156864166, -0.06543853133916855, -0.140204057097435, -0.03192402049899101, -0.7580106854438782, -0.06446216255426407, 0.1711469143629074, -0.4866394102573395, 0.5179592967033386, 0.13882072269916534, -0.009798129089176657, -0.5019250512123108, -0.33980241417884827, -0.5472168326377869, -0.5223529934883118, 0.166558638215065, 0.36598268151283264, -1.167948842048645, 0.01720782183110714, -0.24363811314105988, -0.04916269704699516, 0.20276682078838348, 1.08810293674469, -0.5775039792060852, -0.2636092007160187, 0.5239808559417725, 0.6606585383415222, 0.22670532763004303, -0.6544484496116638, -0.327146053314209, -0.015229950658977032, 0.32285740971565247, -0.4349395334720611, -0.342996746301651, 0.0929071679711342, 0.6204354763031006, -0.4444778263568878, -0.05000500008463859, 0.168925940990448, 0.07166220992803574, 0.053473297506570816, 0.4637676477432251, -1.3995766639709473, 0.12376558035612106, 0.0358559787273407, -0.07743414491415024, -0.10312778502702712, 0.19337205588817596, -0.4296838939189911, -0.5332348942756653, 0.5351269245147705, -0.04896852374076843, 0.39455661177635193, -0.009365458972752094, -0.7340413928031921, -0.26711544394493103, -0.7966372966766357, 0.4576777517795563, 0.5777752995491028, 0.46939870715141296, 0.4110177457332611, 0.006070256233215332, 0.5092793107032776, -0.12759770452976227, -0.2589404582977295, -0.2029859572649002, 0.9230996966362, 0.2373780757188797, -0.07598956674337387, -0.6523596048355103, 0.615772008895874, -0.11392288655042648, 0.36251822113990784, -0.11310923099517822, -0.12496167421340942, -0.5644369125366211, 1.0219682455062866, 0.24786919355392456, -0.06129562854766846, 0.23647189140319824, -0.15672898292541504, -0.42046165466308594, 0.05864626169204712, -0.715050458908081, -0.3334183990955353, 0.05945712327957153, 0.12800563871860504, 0.10146497935056686, -0.29583480954170227, 0.3199721574783325, -0.18621420860290527, 0.08656863123178482, 0.27247533202171326, -0.5284491777420044, -0.7650511860847473, 0.042230844497680664, 0.6116239428520203, -0.04250410199165344, -0.15629056096076965, 0.08850099891424179, -0.2823798954486847, 0.4319235384464264, -0.045931171625852585, -0.04436459019780159, -0.025594407692551613, -1.6741784811019895, -0.3297104835510254, 0.209598109126091, -0.2587665617465973, -0.45234012603759766, -0.2176639288663864, 0.45481619238853455, 0.2583831548690796, 0.03349415585398674, -0.16588172316551208, 0.2550368905067444, 0.5056737065315247, -0.14681243896484375, -0.1861940175294876, 0.5089040994644165, 0.4213810265064239, 0.2476448267698288, 0.12741337716579437, -0.07361766695976257, 0.1302214115858078, 0.31556084752082825, 0.4166359007358551, -0.9520114064216614, -0.09761906415224075, 0.2826826870441437, -0.005943735595792532, 0.028649747371673584, 0.28332844376564026, 1.008154034614563, 0.24356181919574735, -0.28343427181243896, -0.34205055236816406, -0.24041317403316495, 0.759244978427887, 0.21292591094970703, 0.14895571768283844, 0.06228743866086006, -0.11566434055566788, -0.33669471740722656, -0.0799831822514534, -0.42150941491127014, 0.748867928981781, -0.2338586449623108, -0.05552026629447937, 0.4067467153072357, -0.38650575280189514, -0.42233017086982727, -0.5531393885612488, -0.2848702371120453, 0.004648337606340647, -0.12464498728513718, -0.18415029346942904, -0.510621190071106, -0.21044711768627167, -0.06375952810049057, 0.16841495037078855, -0.5733441710472107, 0.5592902302742004, 0.5853757262229919, -0.16911089420318604, 0.05704157426953316, -0.07574488967657089, -0.649566650390625, 0.22531186044216156, 0.43237075209617615, 0.06822047382593155, 0.4363485276699066, -0.12159321457147598, 0.1892094612121582, -0.08150165528059006, 0.03120426274836063, -0.27541983127593994, 0.7290094494819641, -0.6737768650054932, -0.41534653306007385, -0.3737736046314239, -0.4379368722438812, -0.29506683349609375, 0.9077884554862976, -0.07480186223983765, -0.2943783700466156, 0.3670727014541626, -0.009244024753570557, 0.02313012443482876, -0.24579834938049316, -0.1565312147140503, -0.3483855724334717, -0.08878502249717712, -0.9071865081787108, 0.3291301429271698, -0.5160498023033142, -0.24227170646190643, -0.21762476861476895, -0.18627922236919403, -0.08251172304153442, 0.22124461829662323, -0.0024583141785115004, -0.3940903246402741, -0.36027956008911133, 0.19584673643112183, 0.45128384232521057, -0.10432521253824234, 0.6293002963066101, 0.6978543400764465, -0.729762613773346, -0.23468327522277832, 0.1153452768921852, 0.27847668528556824, 0.2276189923286438, -0.04966458678245544, -0.1790708303451538, 0.01915051601827145, -0.27086755633354187, -0.3601628541946411, -0.4950253963470459, 0.8647980690002441, -0.13331513106822968, 0.12907208502292633, 1.297598361968994, 0.4449050724506378, 0.7511609196662903, 0.06037376448512077, -0.008248855359852314, -0.1421436220407486, -0.1225789710879326, -0.8342263102531433, -0.5350061058998108, 0.04915039613842964, -0.24206167459487915]', '{\"世界\": 0.1309303980978331, \"中国\": 0.1434699025495539, \"习惯\": 0.105689849834567, \"书店\": 0.1612311090599603, \"全民\": 0.18766648754405788, \"图书\": 0.1955051870320322, \"城市\": 0.1370301536425863, \"工作\": 0.17233861517638835, \"数字\": 0.16082294944450634, \"文化\": 0.2292020359427876, \"时代\": 0.1384559001098738, \"空间\": 0.12338431463580989, \"纸质\": 0.1261106929652945, \"经典\": 0.1309983496947283, \"认为\": 0.18523410771587975, \"读书\": 0.16333176337699176, \"读者\": 0.3237600309580783, \"阅读\": 1.0, \"图书馆\": 0.16479242807330585, \"电子书\": 0.13075302666524263}', '2025-10-07 13:47:05');
INSERT INTO `content_feature` VALUES (120, 71, '[0.23577730357646945, 0.4271364212036133, -0.5338556170463562, -0.029425710439682007, 0.28761962056159973, 0.08364993333816528, -0.4039595425128937, -0.19756518304347992, -0.361916184425354, 0.004499753471463919, 0.11896210163831712, 0.3042794466018677, -0.056053441017866135, 0.08455556631088257, 0.5353140830993652, 0.4531199038028717, -0.10998406261205672, 2.7787535190582275, 0.31691986322402954, -0.42563319206237793, -0.26414164900779724, 0.3659211397171021, -0.27230599522590637, 0.06659502536058426, -0.062199294567108154, -0.4211364686489105, -0.18108707666397095, 0.18700940907001495, -0.20998793840408325, -0.8897719979286194, 0.047375936061143875, 0.3230258822441101, -0.12150126695632936, -0.18880094587802887, -0.4102774560451507, -0.07277468591928482, -0.5858889818191528, -0.41316089034080505, -0.10451745986938477, -0.9640519022941588, -0.1847991794347763, -0.26656627655029297, -0.01726299524307251, -0.015541682951152325, 0.17586131393909454, -0.3857557773590088, -0.16002309322357178, 0.10392584651708604, 0.5822651982307434, 0.0939655900001526, -0.575153648853302, -0.27601343393325806, -0.257724404335022, 0.3782796859741211, -0.3698570728302002, -0.2206888198852539, 0.2381386011838913, 0.42316898703575134, -0.5505408048629761, -0.24266235530376432, 0.123110331594944, 0.21964193880558017, -0.2131233960390091, 0.4147964417934418, -1.504747748374939, -0.12654979526996613, 0.23576216399669647, -0.17775578796863556, 0.11320974677801132, 0.27426600456237793, 0.00520296161994338, -0.3628099858760834, 0.4179137945175171, -0.13307352364063263, 0.5713010430335999, 0.4328381717205048, -0.525076687335968, 0.29440009593963623, -0.3811722993850708, 0.24596814811229703, 0.49405980110168457, 0.8038644790649414, 0.4408654272556305, 0.09183504432439804, 0.010986845009028912, 0.06997493654489517, 0.4598639905452728, -0.11323630809783936, 0.7162582874298096, 0.027691205963492393, -0.005372246261686087, -0.9868456721305848, 0.8447659611701965, -0.13570475578308103, -0.31676289439201355, 0.1335354894399643, 0.24938248097896576, -0.4857004582881927, 0.7704108357429504, 0.21286332607269287, -0.16073693335056305, 0.1084333285689354, 0.128883957862854, -0.17167381942272186, -0.562431275844574, 0.019275125116109848, -0.5622923970222473, 0.7964922785758972, 0.4770473539829254, -0.4676845371723175, 0.03141666576266289, 0.1779843121767044, -0.6406517028808594, -0.326860636472702, 0.29078730940818787, -0.581752359867096, -0.4242651462554931, -0.23134468495845795, 0.0925651416182518, 0.060673389583826065, -0.07621600478887558, -0.14876793324947357, -0.4337328374385834, 0.33225199580192566, 0.017688969150185585, -0.22835560142993927, -0.2770824134349823, -1.6715832948684692, -0.4257892370223999, 0.18229328095912933, -0.04844701290130615, -0.932869017124176, -0.1610841304063797, 0.04134569689631462, 0.27704253792762756, -0.01883365772664547, -0.29657867550849915, 0.3463435173034668, 0.7592389583587646, -0.34118354320526123, -0.16756241023540497, 0.5323429703712463, 0.3751572370529175, 0.20532011985778809, -0.24744342267513275, 0.33917486667633057, -0.06510564684867859, 0.19688822329044345, 0.09064730256795885, -1.1530625820159912, 0.47921109199523926, 0.2010686993598938, -0.015358288772404194, 0.2118626981973648, 0.17830181121826172, 0.5492555499076843, 0.5885257124900818, 0.18202821910381317, 0.05704984068870544, -0.8574469685554504, 1.1004260778427124, 0.5178146958351135, 0.8499922752380371, -0.1407354325056076, 0.24063710868358612, 0.05631519481539726, -0.24802501499652865, -0.102077879011631, 0.7779633402824402, -0.04543755576014519, -0.0321887731552124, 0.6707994937896729, -0.019536951556801796, -0.13681869208812714, 0.3956376016139984, -0.436975359916687, 0.4626181125640869, -0.2889770269393921, -0.22958682477474213, -0.35362711548805237, 0.2249925285577774, -0.49215129017829895, -0.15672193467617035, -0.5110196471214294, 0.24663716554641724, 0.4760456383228302, -0.11332441121339798, -0.3925762176513672, -0.08808337897062302, -0.7854104042053223, -0.003313302993774414, 0.40762245655059814, 0.3275149166584015, 0.15566779673099518, -0.40042051672935486, -0.05889492109417915, -0.3580828905105591, -0.17406170070171356, 0.3568008244037628, 0.3894809186458587, -0.4261152744293213, -0.6988748908042908, -0.3825625479221344, -0.16233773529529572, -0.007542987819761038, 1.1140364408493042, -0.2881528437137604, -0.05434025824069977, 0.34719184041023254, -0.005659481044858694, -0.21695412695407867, 0.16401982307434082, -0.3766505420207978, -0.10747701674699783, -0.563344419002533, -0.536182701587677, -0.06098705530166626, 0.33041754364967346, 0.26497963070869446, 0.05754375457763672, -0.1278022825717926, 0.6101667881011963, 0.25406187772750854, 0.2908807098865509, -0.3707028925418854, -0.37811508774757385, 0.33075547218322754, 0.08451736718416214, -0.34336423873901367, 0.31484052538871765, 0.42839646339416504, -0.048473019152879715, -0.11768730729818344, -0.0685480609536171, -0.001243939041160047, 0.27305102348327637, -0.1598712056875229, -0.22625596821308136, -0.3041673004627228, -0.627473771572113, -0.7805382609367371, -0.35852012038230896, 0.8049809336662292, -0.13524410128593445, 0.2706673741340637, 1.2712806463241575, -0.2346620112657547, 0.3024108409881592, -0.38483068346977234, 0.3078080117702484, 0.02496355213224888, -0.13841676712036133, -0.6482939124107361, 0.2807179391384125, -0.3013322055339813, 0.03900114819407463]', '{\"2023\": 0.526455236454422, \"2024\": 0.27375672295629944, \"人均\": 0.18384572422965476, \"全国\": 0.22072381635444244, \"分钟\": 0.21162623661862495, \"国民\": 0.5191842294946464, \"图书\": 0.16913876870497563, \"媒介\": 0.14257030500714254, \"成年\": 0.3065040834770528, \"方式\": 0.15781594681923777, \"时长\": 0.14982844574324297, \"样本\": 0.19478933679758445, \"电脑\": 0.14005950088055066, \"纸质\": 0.2405785193013406, \"调查\": 0.3033242990504784, \"进行\": 0.2819260710475027, \"阅读\": 1.0, \"数字化\": 0.13851090612892614, \"电子书\": 0.13607699767925455, \"百分点\": 0.13684939233311855}', '2025-10-07 13:47:08');
INSERT INTO `content_feature` VALUES (121, 70, '[-0.3804372251033783, 0.526885986328125, -0.4518451690673828, 0.06367935985326767, 0.44014620780944824, -0.3243713080883026, 0.004124457482248545, 0.24028898775577545, 0.25411853194236755, 0.749873697757721, 0.2198646068572998, 0.3613569736480713, -0.3322063386440277, -0.26141324639320374, 0.43232405185699463, 0.2528343200683594, -0.3075670301914215, 2.575294733047486, 0.029111087322235107, -0.7291536927223206, 0.27602067589759827, 0.4322102963924408, -0.3281750977039337, -0.20661145448684692, 0.02044385671615601, 0.48807623982429504, -0.3017946481704712, 0.39983630180358887, 0.0893312320113182, -0.33900484442710876, 0.4114989340305328, -0.00403614342212677, -0.06085975840687752, -0.666733980178833, -0.25346317887306213, 0.20603685081005096, -0.22767706215381625, 0.2267749160528183, 0.3596329987049103, -0.9108522534370422, 0.18990103900432587, -0.2933613955974579, 0.15795834362506866, 0.6007831692695618, 0.545280396938324, -0.23247648775577545, -0.3994547426700592, -0.3532021939754486, 1.0938959121704102, 0.11053919792175292, -0.828122615814209, -0.3301728069782257, -0.07598976045846939, -0.1046293005347252, 0.219024121761322, -0.023297330364584923, 0.4037867486476898, 0.9775490164756776, -0.6551159024238586, -0.22246141731739044, 0.18853859603405, -0.15641705691814425, -0.07674854248762131, 0.6855219006538391, -1.5335932970046997, 0.3021214008331299, 0.0625176727771759, -0.14882266521453855, -0.28175005316734314, -0.13517534732818604, -0.4667495787143707, -0.2197643667459488, 0.5029550790786743, -0.030453026294708252, -0.2007098942995071, 0.07586503028869629, -0.46063923835754395, -0.3631398379802704, -0.4974547326564789, 0.9434483051300048, 0.21931858360767365, 0.006040970329195261, 0.09643443673849106, 0.20644374191761017, 0.21486441791057587, 0.2249068021774292, -0.04951070621609688, -0.20048992335796356, 0.9062986373901368, -0.016858026385307312, 0.27410611510276794, -1.2405716180801392, 1.0925110578536987, -0.4930398762226105, 0.07865822315216064, -0.5019761919975281, -0.3800763785839081, -0.3242233097553253, 0.859390914440155, 0.3985577523708343, 0.19208259880542755, 0.43598103523254395, -0.08331213146448135, -0.3499995768070221, -0.1963046044111252, -0.554097592830658, -0.2450820952653885, 0.2732921242713928, 0.003004511119797826, 0.061068590730428696, -0.4712202548980713, 0.1844520568847656, -0.4650048911571502, -0.3183063566684723, -0.01978856325149536, -0.35910388827323914, -0.6515935063362122, 0.12639431655406952, 0.1581033319234848, 0.35598909854888916, 0.1207333207130432, -0.2243567556142807, -0.16035233438014984, 0.13175298273563385, -0.11239484697580338, 0.2877959907054901, -0.4117213785648346, -1.802087426185608, -0.01785770058631897, -0.1446578949689865, -0.28968262672424316, -0.8707671165466309, -0.3190155327320099, -0.1780879944562912, 0.5517485737800598, 0.038880668580532074, 0.009957601316273212, 0.5613996386528015, 0.3678101301193238, -0.21060185134410855, -0.672120988368988, 0.49321457743644714, 0.6779884696006775, 0.3637421131134033, -0.12336591631174088, -0.15230922400951383, -0.07427152246236801, -0.36570635437965393, 0.7016012668609619, -0.4560315608978271, 0.1269816905260086, -0.03341127559542656, -0.3688338100910187, 0.4861761033535004, -0.23666264116764069, 0.6997190117835999, 0.6176285743713379, -0.3071267902851105, -0.8992123007774353, 0.04266524314880371, 1.0796793699264526, 0.26170048117637634, 1.024093747138977, 0.13818670809268951, 0.3430890142917633, -0.21540682017803192, -1.0330381393432615, -0.12852227687835693, -0.08928534388542175, 0.07320836931467056, 0.4942063093185425, 0.2701115608215332, -0.17061220109462738, -0.5297999978065491, -0.6640853881835938, -0.2753990590572357, 0.2890368700027466, -0.39832761883735657, 0.27732041478157043, -0.3900234699249267, -0.13684497773647308, -0.190151646733284, 0.14654578268527985, -0.5027651786804199, 0.182428315281868, 0.13872601091861725, -0.4625119268894195, -0.13945533335208893, -0.4383276700973511, -0.3067598044872284, 0.2440689355134964, 0.6158270835876465, 0.08790170401334763, 0.5113044381141663, -0.25369617342948914, -0.281764417886734, -0.06372485309839249, -0.18708650767803192, -0.13654810190200806, 0.7196596264839172, 0.043307799845933914, -0.08563172817230225, -0.32213863730430603, -0.5942087173461914, 0.03429136797785759, 0.5254384279251099, 0.38200291991233826, -0.7611573338508606, 0.447955846786499, -0.355694055557251, -0.2577470541000366, 0.047367703169584274, -0.06690237671136856, -0.4561409652233124, 0.09774071723222733, -0.3505121171474457, 0.4042427241802216, 0.1929403990507126, -0.2576272189617157, -0.34451282024383545, -0.010175347328186035, 0.8543877601623535, 0.26664456725120544, 0.014849345199763777, -0.03347896412014961, -0.3576522171497345, 0.1683525592088699, -0.06811235100030899, 0.10954614728689194, 0.6548063158988953, 0.09971866756677628, -0.4879935681819916, 0.025904089212417603, -0.2813723385334015, 0.19413645565509796, -0.042989034205675125, -0.1124786213040352, 0.06606131792068481, -0.21962934732437137, -0.2174166887998581, -0.10701673477888109, -0.485764741897583, 0.46615147590637207, 0.20729883015155792, 0.08608361333608627, 1.0575603246688845, 0.015115052461624146, 0.4139771461486816, 0.40236935019493103, -0.32957950234413147, 0.14167420566082, -0.24951209127902985, -0.5875036120414734, -0.5442397594451904, -0.2484661191701889, 0.09615138918161392]', '{\"30\": 0.2798161025966332, \"习惯\": 0.19021922922893716, \"书籍\": 0.3739366103351656, \"了解\": 0.21118736134641597, \"交流\": 0.1716549619022689, \"亲子\": 0.789966288506014, \"兴趣\": 0.1889005806463606, \"培养\": 0.17562231589610583, \"孩子\": 0.5329917592988378, \"家长\": 0.35413482377185834, \"引导\": 0.1863787149820675, \"心理\": 0.20217100586040737, \"找来\": 0.1626700201016772, \"提高\": 0.1615315532145549, \"朋友\": 0.16958904642794995, \"活动\": 0.2030966285013174, \"进行\": 0.29893275800244623, \"适合\": 0.3473540185844063, \"阅读\": 1.0, \"饭妈\": 0.16720910821173243}', '2025-10-07 13:47:11');
INSERT INTO `content_feature` VALUES (122, 69, '[-0.4839265644550323, 0.44511425495147705, -0.22895629703998568, -0.08670588582754135, 0.8186235427856445, 0.2937341630458832, -0.2761046886444092, -0.18254612386226657, 0.11631163954734802, 0.696707010269165, 0.1237928792834282, 0.31249484419822693, -0.07595417648553848, -0.1924934983253479, 0.49625420570373535, 0.15528063476085663, 0.03413251414895058, 2.6600024700164795, -0.05742047354578972, -0.4201999008655548, 0.03725339844822883, 0.5379491448402405, -0.34817084670066833, -0.14235614240169525, -0.04159398749470711, 0.01688992977142334, -0.8619940876960754, 0.4877753257751465, 0.11402680724859238, -0.6507988572120667, 0.1573007106781006, 0.2043922394514084, -0.24678529798984528, -0.17187829315662384, -0.19838611781597135, 0.08109962195158005, -0.054812926799058914, -0.06437429040670395, -0.10563478618860243, -0.9331695437431335, -0.2610992193222046, -0.3101242482662201, 0.4067049026489258, 0.0713069811463356, 0.6645915508270264, -0.367310494184494, -0.3238154947757721, 0.5506572127342224, 0.6533819437026978, 0.013250529766082764, -0.8147245049476624, -0.17927028238773346, -0.19370561838150024, 0.621894121170044, -0.44975563883781433, -0.1208738461136818, 0.2266879826784134, 0.6889753937721252, -0.5150728821754456, -0.32210326194763184, 0.45481905341148376, 0.03435661271214485, 0.0481402762234211, 0.627713143825531, -1.8319883346557615, 0.30963101983070374, 0.09738192707300186, -0.2319376915693283, -0.09780199080705644, 0.2743285000324249, 0.07241532951593399, -0.06291323900222778, 0.6080563068389893, -0.09681910276412964, 0.825323760509491, 0.3839607238769531, -0.5439021587371826, -0.4231776297092438, -0.4136136472225189, 0.4794118404388428, 0.5280042290687561, 0.357753723859787, 0.2198505401611328, -0.5035375952720642, 0.1250380426645279, 0.06498398631811142, -0.25900986790657043, 0.14746005833148956, 0.9582788348197936, 0.29891064763069153, 0.03397110477089882, -0.7993155121803284, 0.47622165083885193, -0.011435375548899174, -0.22189319133758545, 0.024307092651724815, -0.4027833044528961, -0.2215639501810074, 0.9119885563850404, 0.14176999032497406, -0.020168930292129517, -0.19762574136257172, 0.09959853440523148, -0.388472318649292, 0.03218621015548706, -0.6537145972251892, -0.1132926344871521, 0.2490675449371338, -0.05923069640994072, -0.12331002950668336, -0.2924470007419586, -0.02357769012451172, -0.29349949955940247, -0.4584072530269623, 0.12398280948400496, -0.3644191026687622, -0.6791518330574036, -0.2517223060131073, 0.3567526638507843, 0.4230704307556152, -0.17925803363323212, 0.07851745933294296, -0.46703922748565674, 0.8762223124504089, -0.1219644546508789, -0.12599758803844452, -0.18534763157367704, -2.315314769744873, 0.024844899773597717, -0.2846491038799286, -0.08905839920043945, -0.4884897768497467, 0.07896295189857483, 0.21527409553527832, 0.2365064173936844, 0.3034246861934662, -0.16228051483631134, 0.6014542579650879, 0.40745094418525696, 0.010643690824508669, -0.38447675108909607, 0.007565736770629883, 0.2605026364326477, 0.5200520157814026, 0.3419395983219147, -0.0631680116057396, -0.03374743461608887, -0.3279155492782593, 0.28278017044067383, -0.8037490248680115, 0.2523045837879181, 0.359598308801651, 0.1345195174217224, -0.36274099349975586, -0.2650461792945862, 0.8501213192939758, 0.2208069413900375, -0.039481233805418015, -0.5973231196403503, -0.5051090717315674, 0.8940372467041016, 0.2815416753292084, 0.9300656318664552, -0.1400231420993805, -0.2447386234998703, -0.30096375942230225, -0.6482264995574951, 0.07617121934890747, 0.19115620851516724, -0.21458737552165985, -0.038309693336486816, 0.624737024307251, -0.46380749344825745, -0.1562248021364212, -0.6119944453239441, 0.07818523794412613, 0.4873761832714081, -0.19312117993831637, 0.09619810432195663, -0.31628045439720154, 0.35177484154701233, -0.21015898883342743, 0.35918715596199036, -0.4499901235103607, 0.17077569663524628, 0.3519555628299713, -0.2933856248855591, -0.3588639199733734, -0.14615614712238312, -0.9082047343254088, 0.1162225604057312, 0.5643982291221619, -0.017128825187683105, 0.5488700270652771, -0.5684956312179565, 0.3842313289642334, -0.43702253699302673, -0.1446319967508316, -0.272246778011322, 0.7037461400032043, -0.20972780883312225, -0.045857567340135574, -0.44623398780822754, -0.11080614477396011, -0.06918589025735855, 0.8460123538970947, -0.3829571008682251, -0.4773740768432617, 0.10994262248277664, -0.35459887981414795, -0.2302025556564331, 0.26420846581459045, -0.22183556854724884, -0.5593680739402771, -0.0624561570584774, -0.6065275073051453, 0.12476076930761336, -0.2449691742658615, -0.22888730466365817, 0.25291135907173157, 0.013727128505706789, 0.29544389247894287, 0.4770324230194092, 0.25788670778274536, -0.35205045342445374, -0.05005194619297981, 0.4759052097797394, 0.49568626284599304, 0.06312007457017899, 0.624927282333374, 0.39030805230140686, -0.7281845211982727, 0.04587652161717415, 0.21646593511104584, -0.038002241402864456, 0.17104898393154144, 0.07207999378442764, 0.00986685324460268, -0.23382620513439176, -1.10781991481781, -0.2551923096179962, -0.5302587151527405, 0.5110558867454529, -0.29902753233909607, 0.34812137484550476, 0.9279525876045228, 0.3117034137248993, 0.7347626686096191, -0.15819378197193146, -0.0007980465888977051, 0.09660997241735458, -0.4150034487247467, -0.5534828305244446, -0.4332295358181, -0.32252204418182373, -0.3030738830566406]', '{\"万家\": 0.15108999373346071, \"书香\": 0.3037343231505877, \"亲子\": 0.8159506305468004, \"儿童\": 0.3246133122380744, \"全国\": 0.1742538487636489, \"养成\": 0.14187747051477928, \"培育\": 0.17913053942715926, \"家庭\": 0.6978624126918341, \"开展\": 0.24566135561180064, \"意见\": 0.2096091506618718, \"指导\": 0.17584012892586415, \"文化\": 0.22142622605833065, \"服务\": 0.1743362676318982, \"深入\": 0.1729162794338057, \"精神\": 0.18868892281747057, \"联合\": 0.18616141170661188, \"行动\": 0.2673754321546279, \"读书\": 0.1894233374486956, \"部门\": 0.16157610940817246, \"阅读\": 1.0}', '2025-10-07 13:47:15');
INSERT INTO `content_feature` VALUES (123, 68, '[-0.8098848462104797, 0.40513062477111816, -0.5463840961456299, -0.006313373800367117, 0.4538443982601166, 0.15341101586818695, -0.1713976263999939, 0.07902678847312927, 0.031637877225875854, 0.8940829634666443, 0.26237329840660095, 0.3722163736820221, -0.7721810936927795, 0.14281536638736725, 0.28866925835609436, 0.4332711696624756, -0.2645721733570099, 2.4645369052886963, 0.30269014835357666, -0.49236783385276794, -0.06118844822049141, 0.25514736771583557, -0.2884708344936371, 0.03360304236412048, 0.03948599100112915, -0.09559448808431624, -0.18149758875370023, -0.3051607310771942, 0.4442147314548493, -0.4444628953933716, 0.2255547046661377, 0.09668871015310287, -0.4810879230499267, -0.600077211856842, -0.04243455454707146, -0.1877482384443283, 0.02054363489151001, 0.009313702583312988, 0.6215990781784058, -1.0063356161117554, -0.1100832000374794, -0.15125270187854767, 0.18386375904083252, 0.1887321025133133, 0.5841298699378967, -0.29902082681655884, -0.7175300717353821, -0.030767088755965233, 0.878948986530304, 0.22234052419662476, -0.4768104553222656, -0.4553824365139008, 0.02380836009979248, 0.4978948533535004, -0.055057745426893234, -0.025311509147286415, 0.3374752104282379, 0.26489493250846863, -0.1172921434044838, 0.025374948978424072, -0.0784749761223793, 0.3967440128326416, 0.0939631462097168, 0.49368318915367126, -1.4228512048721311, 0.09209736436605452, 0.12237870693206788, 0.01013160776346922, -0.02795779705047607, -0.2450779229402542, -0.16128818690776825, -0.31624430418014526, 0.909846842288971, 0.16055886447429657, 0.12456429749727248, -0.11707764863967896, -0.5762787461280823, -0.4243511855602264, -0.3224956691265106, 0.9953848719596864, 0.09497356414794922, 0.01060811709612608, 0.37143436074256897, -0.07669059187173843, -0.06923136860132217, -0.1330033391714096, 0.13403193652629852, 0.02210082672536373, 0.9759647846221924, -0.3534988462924957, 0.17216233909130096, -1.004059672355652, 0.5396655797958374, -0.5371214747428894, 0.029846971854567528, -0.7102531790733337, -0.21271246671676636, -0.34925851225852966, 0.6819760203361511, -0.08608830720186234, 0.14097800850868225, 0.16603392362594604, -0.23103874921798703, 0.3877263963222504, -0.029106656089425087, -0.4064888060092926, 0.025904783979058266, 0.7440106272697449, 0.5973623394966125, -0.4090705215930938, -0.5007462501525879, 0.28991299867630005, -0.5985173583030701, -0.5223710536956787, 0.3323587477207184, -0.4132904708385467, -0.572968065738678, -0.08370908349752426, 0.4747936427593231, 0.5231533050537109, 0.06194639578461647, 0.11928153038024902, -0.0787801742553711, -0.07110640406608582, -0.3832136392593384, 0.368243932723999, -0.574739933013916, -1.4789973497390747, -0.3324088752269745, 0.1211063265800476, -0.208890900015831, -0.5633425116539001, -0.2246421724557877, 0.426038533449173, 0.4781573712825775, 0.15830925107002258, -0.2468237727880478, 0.044645845890045166, 0.4285300672054291, -0.2829795777797699, -0.5541582107543945, 0.7183334827423096, 0.6710651516914368, -0.12348069995641708, 0.1044902428984642, -0.013439188711345196, -0.17234431207180023, 0.13746307790279388, 0.36467981338500977, -0.39565667510032654, -0.13247472047805786, 0.45308470726013184, 0.12738995254039764, 0.8458667397499084, 0.008162657730281353, 1.044932246208191, 0.5399609208106995, 0.2001758813858032, -0.6365429759025574, -0.21833570301532745, 0.8463847041130066, 0.41016626358032227, 0.8045050501823425, 0.1919525116682053, 0.22660578787326813, -0.01024019718170166, -0.9542238116264344, 0.05365407466888428, 0.2586294114589691, -0.26094162464141846, 0.615919291973114, 0.35147416591644287, -0.23784947395324707, -0.6832430958747864, -0.3308587968349457, -0.5333762168884277, 0.1575428694486618, -0.6267080903053284, 0.005878041032701731, -0.13493657112121582, -0.05070631578564644, -0.26909342408180237, -0.12428971379995346, -0.49495580792427063, -0.11463004350662231, 0.2193150669336319, -0.24853630363941193, -0.22024941444396973, -0.3252820670604706, 0.09704073518514632, 0.1197793260216713, 0.17143796384334564, 0.07373436540365219, 0.3658308982849121, 0.11848405748605728, -0.21684956550598145, -0.2160595804452896, -0.013998071663081646, -0.08415282517671585, 0.11985483020544052, 0.298107773065567, -0.3558598458766937, -0.46727117896080017, -0.4116969108581543, -0.0423145592212677, 0.6017430424690247, 0.05219759419560432, -0.679877758026123, 0.45221564173698425, -0.3983568847179413, -0.122056283056736, -0.18103249371051788, -0.4875582754611969, -0.26496055722236633, -0.3061390817165375, -0.5256536602973938, 0.448548823595047, -0.13830626010894775, 0.004834294319152832, -0.3335055410861969, -0.24045228958129883, 0.2153899222612381, 0.3377077281475067, 0.2451327443122864, -0.14852599799633026, -0.18545301258563995, 0.4391102492809296, 0.10111036151647568, -0.4584376513957978, 0.6542954444885254, 0.3620894849300385, -0.4137285053730011, -0.349103718996048, 0.10550043731927872, 0.6291188597679138, -0.2840879261493683, -0.28826987743377686, -0.13121040165424347, -0.1467808336019516, -0.742455780506134, -0.15669003129005432, -0.3671914637088776, 0.5442855954170227, 0.08023196458816528, 0.5029494166374207, 1.2172755002975464, 0.2124362140893936, 0.5866663455963135, 0.02394501306116581, -0.014958232641220093, 0.13829894363880155, -0.3018582761287689, -0.8390843272209167, -0.4250886738300323, -0.0810096338391304, -0.1773492842912674]', '{\"不同\": 0.2824764930931272, \"习惯\": 0.13557568185134633, \"事情\": 0.19481937000909033, \"亲子\": 0.437435255840403, \"儿歌\": 0.1927872422672939, \"培养\": 0.22227748564228325, \"孩子\": 0.7291538847996166, \"实用\": 0.19072847310597257, \"建立\": 0.2377881440338204, \"故事\": 0.2487897809601924, \"汉字\": 0.17025526129251448, \"注意\": 0.25637786151158065, \"父母\": 0.13162558265216365, \"独立\": 0.2035228680412761, \"能力\": 0.2651409023162212, \"认知\": 0.18642801617854612, \"语言\": 0.21149388998621965, \"重要\": 0.18277438408351449, \"阅读\": 1.0, \"陪伴\": 0.21027866302879253}', '2025-10-07 13:47:18');
INSERT INTO `content_feature` VALUES (124, 67, '[-0.3442799150943756, 0.43968018889427185, -0.6405981183052063, 0.20262831449508667, 0.5929577350616455, 0.07903452217578888, -0.5144855380058289, -0.14491169154644012, -0.052706558257341385, 0.5579965114593506, 0.30728664994239807, 0.41131946444511414, -0.3582538068294525, -0.07541880756616592, 0.38099154829978943, 0.35153332352638245, -0.295503705739975, 2.738002061843872, -0.00005310773849487305, -0.8132269382476807, 0.02224389649927616, 0.5109577775001526, -0.08822246640920639, 0.0910235047340393, -0.024214735254645348, -0.08485180139541626, -0.45203542709350586, 0.00016617774963378906, 0.1339702606201172, -0.9256283640861512, 0.5384522676467896, 0.1333317756652832, 0.07384335994720459, -0.1839626282453537, -0.06404068320989609, 0.026005664840340614, -0.6357921957969666, -0.11119231581687929, -0.09044114500284196, -1.0383590459823608, -0.08450218290090561, -0.0987597480416298, -0.08093225955963135, 0.021942416206002235, 0.3722950518131256, -0.32660430669784546, -0.08983390778303146, -0.07241181284189224, 0.7498249411582947, 0.17125529050827026, -0.7746906280517578, -0.10670429468154909, -0.1930054873228073, 0.378625750541687, 0.04777193069458008, 0.0176466703414917, 0.18897084891796112, 0.7498772740364075, -0.7770118117332458, 0.054076891392469406, 0.22465263307094577, 0.03601229190826416, -0.1573652625083923, 0.5829636454582214, -1.7773395776748655, 0.02797471545636654, -0.07936988025903702, 0.14513340592384338, 0.1805235892534256, -0.06325171142816544, 0.12848450243473053, -0.5276290774345398, 1.011765956878662, -0.16332004964351654, 0.6259207725524902, 0.4109263420104981, -0.8334879279136658, -0.173257514834404, -0.37363728880882263, 0.21896423399448395, 0.6313577890396118, 0.4447754919528961, 0.1802164316177368, -0.157255157828331, -0.09317189455032349, -0.21971969306468964, 0.25141820311546326, -0.28103113174438477, 0.9546931385993958, 0.1695159524679184, 0.00027617564774118364, -1.0831198692321775, 0.9897764325141908, -0.40046799182891846, 0.03815050050616264, -0.02970433235168457, 0.04749029874801636, -0.5310918688774109, 1.071325421333313, 0.27216285467147827, -0.19580680131912231, 0.1042066216468811, 0.16685156524181366, 0.01575850509107113, 0.0018115838756784797, -0.10413701087236404, -0.4072442352771759, 0.7034242153167725, 0.2580891251564026, -0.3336620330810547, -0.05933827161788941, 0.115521639585495, -0.5995876789093018, -0.2031598538160324, 0.3136305510997772, -0.901681900024414, -0.8145638108253479, -0.031914371997117996, 0.092844195663929, -0.032844722270965576, -0.1607983112335205, 0.18609686195850372, -0.4267399311065674, 0.3087703287601471, 0.15125524997711182, 0.04359699413180351, -0.385444313287735, -1.718698501586914, -0.5036361813545227, -0.03781550005078316, 0.028210585936903954, -0.6069756746292114, -0.2123173028230667, 0.05192112922668457, 0.2179165631532669, 0.030685165897011757, -0.3137498199939728, 0.4638242423534394, 0.8399865031242371, -0.3507002890110016, 0.03861886262893677, 0.29394206404685974, 0.5551050305366516, 0.18116481602191925, -0.15834331512451172, -0.1978580802679062, -0.3263614773750305, -0.03292232006788254, 0.1596537083387375, -0.7247616648674011, 0.33013924956321716, 0.07757526636123657, -0.12684909999370575, 0.2106887847185135, 0.16093510389328003, 0.637927234172821, 0.6815376877784729, 0.11327311396598816, -0.180213525891304, -0.12161383777856828, 1.101035714149475, 0.43320682644844055, 0.6261237263679504, 0.13406294584274292, 0.26993119716644287, -0.4459538459777832, -0.2900398075580597, -0.1115046814084053, 0.7279338836669922, -0.0714927688241005, 0.115679569542408, 0.872613251209259, -0.40295395255088806, -0.6800147891044617, -0.08201432228088379, -0.06055213883519173, 0.2856803238391876, -0.5210736393928528, -0.0444340705871582, -0.4597272574901581, 0.08467865735292435, -0.0746380090713501, 0.128865048289299, -0.5019879937171936, 0.08326508849859238, 0.6320328712463379, -0.2004249691963196, -0.18097881972789764, -0.4681665003299713, -0.5356672406196594, 0.1522809863090515, 0.42853689193725586, 0.09861906617879868, 0.30530983209609985, -0.22845880687236783, -0.05739930272102356, -0.3632650077342987, -0.18249750137329104, 0.151050865650177, 0.3759596645832062, -0.28180503845214844, -0.5993233323097229, -0.273345947265625, -0.1334739476442337, -0.048357460647821426, 0.864173173904419, -0.09028398990631104, -0.1639307290315628, -0.0731620192527771, -0.1855362057685852, -0.11745259910821916, 0.03271516039967537, -0.27171042561531067, -0.38011571764945984, -0.4536043703556061, -0.36305150389671326, 0.16195517778396606, -0.20866085588932037, 0.09680049866437912, -0.002697636606171727, -0.03552575781941414, 0.04339370131492615, 0.17960147559642792, 0.4528721570968628, -0.05087294057011604, -0.5444685816764832, 0.26347991824150085, 0.120906800031662, -0.34990549087524414, 0.8971332907676697, 0.29296794533729553, -0.038541704416275024, -0.15667413175106049, 0.1225276216864586, 0.264825701713562, 0.3136475086212158, 0.17088425159454346, -0.14963994920253754, -0.20364166796207428, -0.5111127495765686, -0.3932928144931793, -0.7462244033813477, 0.6818947792053223, -0.09383666515350342, 0.1834428757429123, 1.1095173358917236, -0.08854589611291885, 0.3225351572036743, -0.09016690403223038, 0.14492101967334747, -0.16970054805278778, -0.3816125690937042, -0.8541709780693054, -0.3163865804672241, -0.4814279973506927, 0.11547600477933884]', '{\"书店\": 0.11239688110620988, \"亲子\": 0.7516443603602259, \"内容\": 0.1491288329667768, \"包括\": 0.16032629576657845, \"图书\": 0.3463911218414546, \"孩子\": 0.7799054613719502, \"家长\": 0.8702512375385966, \"开展\": 0.10926053715757952, \"报告\": 0.1327025153652037, \"提到\": 0.21493817278333063, \"故事\": 0.188150504622482, \"显示\": 0.1499085441570649, \"比如\": 0.1448212904621492, \"活动\": 0.10124562915770352, \"过程\": 0.16708689344226504, \"进行\": 0.14107966489650547, \"选择\": 0.2100229321489003, \"阅读\": 1.0, \"陪伴\": 0.13858763397862192, \"类图书\": 0.10682516594039998}', '2025-10-07 13:47:22');
INSERT INTO `content_feature` VALUES (125, 66, '[-0.3771826922893524, 0.2885415256023407, -0.3962307870388031, 0.16158108413219452, -0.03660641238093376, 0.23365651071071625, -0.2539065182209015, 0.16835957765579224, 0.17762978374958038, 0.7539560794830322, 0.3276121914386749, 0.4062279760837555, -0.5092741847038269, 0.01906476356089115, 0.48749908804893494, 0.07946699112653732, -0.3900345265865326, 2.7606041431427, 0.04252823069691658, -0.8321800231933594, -0.14387165009975433, 0.48101839423179626, 0.2499583810567856, -0.19287629425525665, 0.06629041582345963, 0.11184997111558914, -0.35530295968055725, 0.006274978164583445, 0.05651779845356941, -0.932769238948822, 0.48582181334495544, 0.15986204147338867, -0.007575491908937693, -0.5496699213981628, -0.31078222393989563, -0.0031197171192616224, -0.09129756689071657, -0.023196779191493988, 0.4099810123443603, -0.7852339744567871, 0.08716102689504623, -0.10811404138803482, 0.14115524291992188, -0.11414029449224472, 0.3788881301879883, -0.47439590096473694, 0.029511013999581337, 0.008253778330981731, 0.6714419722557068, 0.464396595954895, -0.36034438014030457, -0.7648844718933105, 0.5016438961029053, 0.11538124084472656, -0.11274832487106325, 0.3872999250888825, 0.11268290132284164, 0.6226502060890198, -0.5096551775932312, -0.07071416825056076, 0.2837316393852234, 0.3811613619327545, -0.2176621705293655, 0.7479028701782227, -1.4631174802780151, 0.2421197146177292, 0.146504744887352, -0.116045780479908, -0.19851429760456085, 0.15974169969558716, -0.04209153354167938, -0.2990091145038605, 0.8696625828742981, -0.12376174330711363, 0.6261626482009888, 0.21996892988681793, -0.5656999349594116, -0.3118358850479126, -0.4648419320583343, 0.29837915301322937, 0.7364436984062195, 0.18493320047855377, 0.40938934683799744, -0.19508837163448337, 0.2281539887189865, -0.11554154008626938, -0.002584566595032811, -0.33547699451446533, 0.7750391960144043, -0.06709925085306168, -0.04918939247727394, -1.0849509239196775, 0.353494256734848, -0.23651494085788727, 0.002652585506439209, -0.4411803185939789, -0.6891453862190247, -0.5042445063591003, 0.7851034998893738, 0.22487635910511017, 0.11359814554452896, 0.7085428237915039, -0.07555942982435226, -0.3736066520214081, -0.33280009031295776, -0.5519271492958069, -0.050302907824516296, -0.028002917766571045, 0.2926799952983856, -0.22193682193756104, -0.362052321434021, 0.33272305130958557, -0.4984817504882813, -0.42877277731895447, 0.13721086084842682, -0.5607262253761292, -0.6141257286071777, 0.06283638626337051, 0.4464593827724457, -0.16046656668186188, -0.1120341345667839, 0.008018811233341694, -0.5142924189567566, 0.3810621201992035, -0.238795205950737, 0.2783023416996002, -0.20159704983234408, -1.6056575775146484, -0.4835769832134247, -0.15937809646129608, -0.3017214834690094, -0.35727834701538086, -0.4876196384429931, 0.3954700529575348, 0.3461727797985077, -0.05937743186950683, -0.015662947669625282, 0.398423284292221, 0.560395359992981, 0.12330283969640732, -0.22410710155963895, 0.35592517256736755, 0.6626307368278503, 0.341645210981369, 0.008302539587020874, 0.20245222747325897, -0.11333149671554564, 0.11497694253921507, 0.4722944498062134, -0.7509387135505676, 0.16156595945358276, 0.16795940697193146, -0.23346586525440216, 0.05952165648341179, -0.3163338601589203, 1.0198239088058472, 0.4532994329929352, 0.32541871070861816, -0.5265932679176331, -0.20401008427143097, 1.1578688621520996, 0.4584275782108307, 0.721250057220459, 0.3469773232936859, -0.3321259915828705, -0.37263062596321106, -0.6787102818489075, -0.16182880103588104, 0.6472455859184265, -0.3610953092575073, 0.3693118989467621, 0.9579651951789856, -0.695859968662262, -0.8064134120941162, -0.1593937873840332, -0.1393308937549591, 0.3280275762081146, -0.3078863322734833, -0.057472288608551025, -0.2435312122106552, 0.166206032037735, -0.013056199066340923, 0.3444637358188629, -0.6538724899291992, 0.3654774427413941, 0.28286561369895935, -0.07902020961046219, -0.2959325909614563, -0.08322316408157349, -0.37107858061790466, 0.366582304239273, 0.41425395011901855, 0.08508657664060593, 0.30170440673828125, -0.11496353149414062, -0.14886575937271118, -0.2290515303611755, 0.16961169242858887, -0.15344107151031494, 0.3728629052639008, 0.07454223185777664, -0.4376859962940216, -0.44842255115509033, -0.5946477055549622, -0.3019566237926483, 0.8123312592506409, 0.06857889145612717, -0.1778583526611328, -0.03945492580533027, -0.6183193325996399, -0.1326398104429245, 0.10971930623054504, -0.2162826806306839, -0.10250139981508256, -0.20939047634601593, -0.3653469979763031, 0.15437711775302887, -0.04546956345438957, -0.35693708062171936, -0.4491212069988251, -0.37898531556129456, 0.3259083926677704, 0.5581358671188354, 0.103438138961792, -0.07451773434877396, -0.5312492847442627, 0.42026615142822266, 0.14953334629535675, -0.12294139713048936, 0.4793398678302765, 0.6580319404602051, -0.5884530544281006, -0.18516869843006137, -0.09983634948730467, 0.06009504199028015, 0.23635374009609225, 0.1893743872642517, -0.22467398643493652, -0.18897755444049835, -0.49943187832832336, -0.1903967261314392, -0.5167106986045837, 0.6902599334716797, 0.04115568473935127, 0.12422867864370346, 1.236746311187744, 0.346441388130188, 0.6161478161811829, -0.16359342634677887, 0.01945020817220211, 0.04766613245010376, -0.5050632953643799, -1.2920626401901243, -0.4425251483917236, -0.058459799736738205, -0.33434978127479553]', '{\"专业\": 0.17633538921984446, \"亲子\": 0.3958762199096412, \"儿童\": 0.22397143197121216, \"创作\": 0.13402359595430932, \"喜欢\": 0.20206838487639944, \"孩子\": 0.5211089578091855, \"家庭\": 0.18770276787111964, \"家长\": 0.17229798780867317, \"幼儿\": 0.17107236700575462, \"成人\": 0.1425729672073083, \"拓展\": 0.18490011462756809, \"教师\": 0.17633515824599622, \"时间\": 0.18034241714393712, \"经受\": 0.1829719406778232, \"联系\": 0.2131360107529761, \"观念\": 0.18800568743322515, \"谈论\": 0.31943540340914994, \"赵霞\": 0.30862381075104683, \"阅读\": 1.0, \"儿童文学\": 0.38034530051058696}', '2025-10-07 13:47:25');
INSERT INTO `content_feature` VALUES (126, 65, '[-0.4515503942966461, 0.27468210458755493, -0.2597542107105255, 0.2185457348823547, 0.3636859357357025, 0.0871080532670021, -0.3298647701740265, -0.16389299929141998, 0.11455044150352478, 0.3931606113910675, 0.05156992003321648, 0.40078243613243103, -0.12908333539962769, 0.018005749210715294, 0.553322970867157, 0.2099717408418655, -0.3281691074371338, 2.6818597316741943, 0.21289171278476715, -0.18332023918628693, -0.04675872623920441, 0.3978310525417328, -0.020733952522277832, -0.2783243954181671, -0.10944970697164536, 0.08786291629076004, -0.6916300654411316, 0.1282273381948471, 0.2773343026638031, -0.5550177097320557, 0.5416498780250549, 0.21686936914920807, 0.1049109473824501, -0.5349140167236328, -0.5635558366775513, -0.2167315036058426, -0.4800274074077606, 0.24041958153247833, 0.14003656804561615, -1.0389258861541748, 0.020504623651504517, -0.5105359554290771, -0.09548743814229964, 0.2697189748287201, 0.8703168034553528, -0.4580773413181305, -0.11447129398584366, 0.2682555913925171, 0.566156268119812, 0.17778964340686798, -0.6262263655662537, -0.7457707524299622, -0.11016053706407548, 0.16652806103229523, -0.13113777339458466, -0.3708665668964386, 0.32405540347099304, 0.4180159568786621, -0.7181388735771179, -0.18858468532562256, 0.4630320966243744, 0.2624983787536621, 0.25436660647392273, 0.7941384315490723, -1.6300994157791138, 0.07945824414491653, 0.017927253618836403, -0.34189924597740173, -0.12149357795715332, 0.12518684566020966, -0.016971180215477943, -0.30685415863990784, 0.6549652814865112, 0.18361441791057587, 0.6608878374099731, 0.17824043333530426, -0.8245227932929993, -0.1331286579370499, -0.900303840637207, 0.4702810049057007, 0.917156219482422, 0.1575399786233902, 0.4087194204330444, -0.31610873341560364, 0.4812179505825043, -0.00014293193817138672, 0.051971543580293655, -0.29266971349716187, 1.2063817977905271, -0.00035369396209716797, -0.09813990443944932, -0.531182587146759, 0.3613056242465973, -0.04843500629067421, -0.13540710508823395, 0.09054511785507202, -0.4768377542495727, -0.2451833337545395, 0.7903540730476379, 0.2125788778066635, -0.06075521185994148, 0.41407832503318787, -0.050068825483322144, -0.45779135823249817, -0.10697539895772934, -0.6458134055137634, -0.004135438706725836, -0.1008526086807251, 0.29858747124671936, -0.30677366256713867, -0.44914281368255615, 0.3794071972370148, -0.13776631653308868, -0.46733927726745605, 0.6499900817871094, -0.46575021743774414, -0.5588299632072449, 0.3747589588165283, 0.5488316416740417, -0.06650334596633911, -0.10376676917076112, 0.20771463215351105, -0.6973142623901367, 0.6603655815124512, -0.33750414848327637, 0.08123919367790222, -0.24609313905239105, -1.605669021606445, -0.41146254539489746, -0.05972903594374657, 0.0762733444571495, -0.2419972866773605, -0.3268251419067383, 0.2989249527454376, 0.503576934337616, 0.19793005287647247, -0.2734778821468353, 0.26432737708091736, 0.6031434535980225, 0.1271613985300064, -0.06623545289039612, 0.3464370667934418, 0.03299878537654877, 0.3909938335418701, -0.03678275644779205, 0.07683417946100235, -0.013126407749950886, 0.028812935575842857, 0.41812440752983093, -0.8097098469734192, 0.08909019827842712, -0.04118657112121582, -0.0020285844802856445, 0.40672120451927185, -0.3344416916370392, 0.982675075531006, 0.5389624238014221, -0.291450172662735, -0.7138097286224365, -0.4952125549316406, 0.9289347529411316, 0.4007728099822998, 0.6082882881164551, 0.01235796045511961, -0.27802398800849915, -0.4300005137920379, -0.3272395431995392, 0.050361353904008865, 0.6251832246780396, -0.26042428612709045, 0.0441298671066761, 0.2730507552623749, -0.39765000343322754, -0.3875386714935303, -0.11063209921121596, -0.06829076260328293, 0.3918744623661041, -0.013954500667750835, -0.1937245577573776, -0.3456536829471588, -0.034159936010837555, -0.31952956318855286, 0.24022413790225983, -0.48966118693351746, 0.22784416377544403, 0.33950158953666687, 0.0608515627682209, 0.10447412729263306, -0.28535929322242737, -0.4538688659667969, -0.23082895576953888, 0.3890632390975952, -0.02213260345160961, 0.3116367757320404, -0.18576662242412567, 0.035342633724212646, -0.07987416535615921, -0.31721118092536926, 0.380295068025589, 0.6509160399436951, -0.34012457728385925, -0.4174672067165375, -0.45168519020080566, -0.2247671335935593, -0.04909052327275276, 0.7914097309112549, 0.09484189003705978, -0.4352437555789947, 0.09688136726617812, -0.3470517098903656, 0.02735804952681065, 0.16393880546092987, 0.03081693686544895, -0.41643285751342773, -0.3155940771102905, -0.5998883843421936, 0.24168042838573456, -0.18712218105793, -0.3067023456096649, -0.02935793064534664, -0.28987523913383484, 0.21253062784671783, 0.7074401378631592, 0.20736779272556305, -0.45023348927497864, -0.32077208161354065, 0.4070989191532135, 0.20808297395706177, -0.6641913056373596, 0.656497061252594, 0.5069034099578857, -0.48454609513282776, -0.353983998298645, -0.19626910984516144, -0.056642353534698486, 0.21345573663711548, 0.004400372505187988, -0.36582788825035095, -0.16613848507404327, -0.4219244718551636, -0.4085190296173096, -0.6065389513969421, 0.6655063629150391, -0.20745646953582764, 0.246170237660408, 0.8896178603172302, 0.18691563606262207, 0.825676441192627, -0.07871417701244354, -0.14822955429553986, 0.0018167296657338736, -0.12021294981241226, -0.7696927189826965, -0.24100510776042935, 0.05802290141582489, -0.26659438014030457]', '{\"世界\": 0.376934202306989, \"中国\": 0.7307915507895478, \"亲子\": 0.7483600407588774, \"人生\": 0.3195181827006997, \"儿童\": 0.41970233193625583, \"共读\": 0.3151044716952011, \"发表\": 0.3001190658274559, \"围坐\": 0.3095007578243155, \"大人\": 0.24878463585512417, \"孩子\": 0.5272194599989231, \"家庭\": 0.3253128968059855, \"帮助\": 0.30264724217829564, \"提出\": 0.3344913616446412, \"现状\": 0.23576908907280864, \"绘本\": 0.5153706174509106, \"重要\": 0.3782179126153456, \"阅读\": 1.0, \"馆内\": 0.33827799373385076, \"生态圈\": 0.21203456998773865, \"传媒大学\": 0.20636873817110823}', '2025-10-07 13:47:29');
INSERT INTO `content_feature` VALUES (127, 64, '[-0.5065187811851501, 0.5102083086967468, -0.4656910002231598, 0.04362478852272034, 0.4633847177028656, 0.03765523433685303, -0.08592534810304642, -0.15073294937610626, 0.16275590658187866, 0.41669344902038574, -0.4493651390075683, 0.20494680106639865, 0.5493156313896179, -0.6391523480415344, 0.3932217061519623, 0.4095940589904785, 0.04314125701785088, 3.68929672241211, 0.12995323538780212, -0.46859946846961975, -0.09321751445531844, 0.6715955138206482, -0.3965068757534027, 0.13693679869174957, -0.09131773561239244, 0.3349348306655884, 0.07511422038078308, 0.04170157387852669, -0.23898303508758545, -0.6378005146980286, -0.23320798575878143, 0.2292199283838272, 0.19890357553958893, -0.5962600111961365, -0.2977418601512909, 0.06976769119501114, -0.4681163132190704, -0.15935134887695312, -0.08736062049865723, -1.0052672624588013, 0.15435661375522614, -0.17399103939533234, 0.11008162051439284, 0.2752774655818939, 0.4678150117397308, 0.05178208276629448, -0.30918148159980774, -0.09104802459478378, 1.2930189371109009, 0.07961364835500717, 0.312658429145813, -0.6117604374885559, 0.0050777047872543335, 0.217751145362854, 0.31584224104881287, -0.3519251048564911, 0.034083981066942215, 0.12309397011995316, -0.031007876619696617, -0.629492461681366, -0.2445625066757202, -0.35875067114830017, 0.029990708455443386, 0.526884913444519, -0.9701879620552064, 0.61007159948349, -0.15151703357696533, -0.3102138936519623, 0.07393164932727814, 0.24715988337993625, 0.13128596544265747, -0.2089509516954422, 0.5234987735748291, -0.3098978102207184, -0.11489972472190856, -0.14518752694129944, 0.2965773940086365, -0.4235067367553711, 0.33288177847862244, 0.6991105675697327, -0.1472534537315369, -0.3617749512195587, 0.3732794225215912, -0.6788650155067444, -0.4601500332355499, 0.4757899343967438, 0.401782363653183, 0.16300249099731445, 0.9303663372993468, 0.09004787355661392, 0.19159287214279175, -0.5062829852104187, 0.5010499358177185, -0.025620996952056885, -0.3903856575489044, 0.048892199993133545, -0.23579108715057373, 0.0102328360080719, 0.6543532013893127, -0.05333994701504707, -0.4105354845523834, 0.171620175242424, -0.4751574993133545, 0.14917442202568054, -0.032451555132865906, -0.5553097724914551, -0.970797061920166, 0.40035709738731384, 0.19522003829479215, 0.03299826383590698, -0.532832145690918, 0.5044938921928406, -0.017930546775460243, 0.10478761792182922, 0.5901568531990051, -0.1080366000533104, -0.3917276859283447, -0.4292185008525848, -0.38241806626319885, 0.6979982852935791, 0.2945277988910675, -0.30178573727607727, 0.015538017265498638, -0.3634385168552398, -0.480541855096817, 0.4016834497451782, 0.008303324691951275, -1.1413017511367798, 0.2377307415008545, 0.2712273895740509, 0.38034844398498535, 0.26920226216316223, 0.27364087104797363, 0.4588475525379181, -0.18028271198272705, -0.2829930782318115, -0.36990460753440857, 0.4356054365634918, 0.2194397449493408, -0.005213797092437744, -0.33788707852363586, -0.08735105395317078, -0.3103945553302765, 0.4785018861293793, 0.025196164846420288, -0.3934429883956909, 0.620445191860199, 0.06780144572257996, 1.1245273351669312, 0.12281080335378648, 0.3377440273761749, 0.16332747042179108, -0.43971219658851624, -0.31193795800209045, 0.18504345417022705, -0.01240086555480957, -0.023136615753173828, 0.11406876891851424, -0.4822005331516266, -0.9659233689308168, 0.6950536370277405, 0.5623453855514526, 0.14829786121845245, -0.7248892784118652, -0.16485391557216644, -0.25837206840515137, -0.15593229234218595, -0.1746646761894226, -0.34295472502708435, -0.6474449634552002, -0.4064040184020996, 0.1899675130844116, -0.4245050251483917, -0.14458328485488892, -0.17986536026000977, -0.08651059865951538, 0.007117231842130423, -0.40971872210502625, 0.09453245252370834, -0.6182653307914734, -0.3635998070240021, -0.01720069907605648, 0.021797269582748413, 0.04796255752444267, 0.005116254091262817, -0.08324787765741348, 0.13312555849552157, 0.094706691801548, -0.5518136620521545, -0.4784888029098511, -0.2216917723417282, 0.09740206599235536, 0.5883724093437195, -0.1403028964996338, -0.3750039041042328, 0.12642621994018555, 0.08139774948358536, 0.14129756391048431, 0.0835677906870842, 0.056638892740011215, -0.5563461184501648, -0.17493562400341034, -0.10669157654047012, -0.10863395780324936, -0.3600486218929291, 0.13518120348453522, -0.2989203929901123, -0.7645857930183411, 0.3463003635406494, -0.1536145061254501, 0.19311267137527463, -0.4638422429561615, 0.31807008385658264, 0.005490899085998535, -0.3905254304409027, -0.19044692814350128, 0.3614921569824219, 0.7482176423072815, 0.3666727542877197, -0.4916583001613617, 0.5477216243743896, 0.8089630007743835, 0.2893810570240021, -0.045136261731386185, -0.2360967248678207, -0.2920774221420288, 0.0270713958889246, -0.21335886418819427, 0.1033727303147316, 0.417174220085144, 0.4541639983654022, -0.0665024146437645, -0.17286358773708344, 0.06975149363279343, -1.0090444087982178, -0.20154719054698944, -0.2135792225599289, -0.15802165865898132, 0.19725029170513153, -1.080991268157959, 0.3452187478542328, 0.21928828954696655, 0.6599056124687195, 0.2038722783327103, 0.26717349886894226, 1.3158254623413086, -0.02786046266555786, 0.21967698633670807, -0.24456088244915009, -0.1380019634962082, 0.37411728501319885, -0.7898207306861877, -0.8224758505821228, -0.1929284930229187, 0.21183599531650543, 0.5463708639144897]', '{\"全名\": 0.9328972340999968, \"全民\": 0.6836160741526772, \"公益\": 0.7615398144005359, \"阅读\": 1.0, \"宣传片\": 0.9200436145967756}', '2025-10-07 13:47:31');
INSERT INTO `content_feature` VALUES (128, 63, '[-0.15736979246139526, 0.610521137714386, -0.049961015582084656, -0.4227867424488067, 0.31461331248283386, -0.24988120794296265, -0.5177555680274963, -0.4699932038784027, 0.05048319324851036, 0.412136048078537, -0.6596881747245789, 0.040684301406145096, 0.2373514622449875, 0.11680451780557632, 0.5916759371757507, 0.3751345574855805, -0.28020986914634705, 3.0875940322875977, 0.18982301652431488, 0.2766377627849579, 0.0037860076408833265, 0.4808488190174103, -0.3884822130203247, 0.11669107526540756, 0.10005170106887816, 0.5539658069610596, -0.11361236125230788, 0.02210656739771366, -0.47681692242622375, -0.2305506020784378, -0.7801360487937927, 0.005294928792864084, 0.19355423748493197, -0.08016598224639893, -0.7719388008117676, -0.2973168194293976, -0.4043208658695221, 0.35406699776649475, -0.30848923325538635, -1.528128147125244, -0.32347920536994934, -0.2917357385158539, -0.1373172402381897, 0.14936649799346924, 0.755071222782135, 0.00423181289806962, -0.5302391052246094, 0.22258193790912628, 1.0400856733322144, -0.032718438655138016, 0.44283440709114075, -0.5439887046813965, 0.15550057590007782, 0.297909140586853, -0.32190385460853577, -0.4122132360935211, 0.11301467567682266, 0.28041303157806396, -0.2850768566131592, -0.9825718402862548, -0.012814740650355816, -0.008778144605457783, 0.2599588632583618, 0.6334671378135681, -1.5521154403686523, 0.3432944118976593, 0.0320802740752697, -0.3158814609050751, -0.17625421285629272, 0.4640600681304931, -0.2026775628328323, -0.32959112524986267, 0.4918854236602783, -0.3058408498764038, -0.2825751006603241, 0.27226242423057556, -0.12785667181015015, -0.015813305974006653, -0.11403688043355942, 0.8105083107948303, -0.018306011334061623, -0.1273823231458664, 0.6865944862365723, -0.6813012957572937, -0.37311992049217224, 0.7689309120178223, 0.1098514199256897, 0.3716625273227692, 1.0041358470916748, 0.02602595090866089, -0.25795719027519226, -0.8894800543785095, 0.7644066214561462, 0.06648951023817062, -0.3856339752674103, 0.32381513714790344, 0.4034287929534912, -0.25809070467948914, 1.0728493928909302, 0.2734896242618561, -0.5373395085334778, -0.013493984937667848, -0.259933203458786, -0.10867134481668472, -0.017929688096046448, -0.4774858057498932, -0.02274447120726109, 0.3906162679195404, 0.2672521471977234, -0.05793197080492973, -0.3722451627254486, 0.35376620292663574, 0.005487481597810984, -0.3778763711452484, 0.4713113307952881, 0.1057966947555542, -0.828557014465332, -0.07357994467020035, -0.40750813484191895, 0.7840659618377686, -0.004450609441846609, -0.5902103185653687, -0.14940528571605682, 0.3097418248653412, -0.6131197810173035, 0.708896815776825, 0.29155251383781433, -1.8885536193847656, 0.27591636776924133, 0.014377777464687824, 0.4056210517883301, 0.08059835433959961, 0.07957186549901962, 0.4289243221282959, 0.29340317845344543, -0.13790296018123627, 0.006039986852556467, 0.5972846746444702, 0.14467763900756836, 0.08563053607940674, -0.7427917122840881, -0.00929753016680479, -0.3409394323825836, -0.034572552889585495, 0.1490803062915802, -0.1393490582704544, 0.29532262682914734, 0.00044954815530218184, 0.5751399993896484, -0.4654885530471802, -0.11495885998010635, -0.1201123371720314, -0.7786746025085449, -0.1505366563796997, -0.30221331119537354, -0.19330202043056488, -0.11949758976697922, 0.3050321042537689, -0.5282209515571594, -0.9210770130157472, 0.7879767417907715, 0.7703060507774353, 0.17246611416339874, -0.9905291199684144, -0.1121927872300148, -0.31176283955574036, -0.43352341651916504, 0.08577319234609604, 0.2907584011554718, -0.420735627412796, -0.11908918619155884, 0.19385896623134613, -0.394500732421875, -0.04734901711344719, -0.10936090350151062, -0.2539392411708832, 0.34496983885765076, -0.30400988459587097, 0.35676732659339905, -0.49044689536094666, -0.11595960706472395, 0.09615415334701538, 0.14922019839286804, -0.30025193095207214, -0.4340524971485138, -0.28252872824668884, -0.28255924582481384, -0.013572171330451964, -0.08918661624193192, -0.1810571700334549, -0.48687148094177246, 0.4699610471725464, 0.08697396516799927, 0.332881361246109, -0.4652562141418457, 0.5017452836036682, 0.7571085095405579, -0.603507936000824, 0.01762968860566616, 0.3181297481060028, -0.37827765941619873, 0.021113762632012367, -0.2567364275455475, -0.6167841553688049, 0.3954695761203766, -0.08546710759401321, -0.5134105086326599, -0.69753497838974, 0.728263795375824, 0.21412165462970736, -0.06417965143918991, -0.09416734427213667, 0.01849619299173355, -0.1523703932762146, -0.4599224627017975, -0.3517681360244751, 0.7519152760505676, 0.18274301290512085, -0.08747265487909317, -0.20696449279785156, 0.22947239875793457, -0.013937552459537985, 0.059088628739118576, -0.5832575559616089, -0.15131837129592896, 0.30005744099617004, 0.0071557960473001, 0.3909887373447418, 0.04496303200721741, 0.5193469524383545, 0.37913778424263, 0.033281248062849045, 0.28072723746299744, 0.41102123260498047, -0.49198636412620544, 0.07663089036941528, -0.04090672731399536, 0.19591599702835083, 0.08209706097841263, -0.6988489031791687, 0.3505464494228363, -0.6885957717895508, 0.3349144756793976, 0.032094597816467285, 0.5738323330879211, 1.3740404844284058, 0.10585995763540268, 0.32925915718078613, -0.08779429644346237, -0.15062303841114044, 0.5426011085510254, -0.5360656380653381, -0.16396990418434143, 0.17313282191753387, 0.5893566012382507, -0.021033084020018578]', '{\"共享\": 0.8461705298782316, \"共建\": 0.8358123233939115, \"宣传\": 0.5832427515790239, \"书香社会\": 1.0, \"现代文明\": 0.9891814967409344}', '2025-10-07 13:47:34');
INSERT INTO `content_feature` VALUES (129, 62, '[-0.2619539499282837, 0.575814425945282, -0.5734872221946716, -0.30820634961128235, 0.600152313709259, -0.21603083610534668, -0.40613147616386414, -0.07497159391641617, 0.13057442009449005, 0.8607912063598633, -0.08239056915044785, 0.11517786979675292, 0.07438524812459946, -0.4693821370601654, 0.27821823954582214, 0.3140111267566681, -0.06111142039299011, 3.748682975769043, -0.1373719722032547, -0.4051620066165924, -0.2478521317243576, 0.6129220128059387, -0.331399530172348, -0.30408328771591187, 0.07550688832998276, 0.1419273167848587, 0.07684540003538132, 0.3143494725227356, -0.13264980912208557, -0.8977047801017761, 0.16731929779052734, 0.24107372760772705, 0.09073177725076675, -0.2570580542087555, -0.280011385679245, -0.2327813357114792, -0.5255460739135742, 0.35509824752807617, 0.05509907007217407, -0.7708700299263, 0.10414668172597884, 0.0218272116035223, 0.097364641726017, 0.39585062861442566, 0.7689898610115051, -0.032518357038497925, -0.07793046534061432, -0.015394668094813824, 0.6581875681877136, 0.43151989579200745, 0.01300064753741026, -0.3821476399898529, -0.0029060940723866224, 0.2575179636478424, 0.3958363533020019, -0.2926044464111328, -0.21151858568191528, 0.07793418318033218, -0.4993891716003418, -0.3823241889476776, -0.08028799295425415, -0.12189113348722458, -0.31748318672180176, 0.5852371454238892, -0.8526245951652527, -0.033415645360946655, 0.35495007038116455, -0.2150835543870926, -0.486152321100235, 0.02049875259399414, -0.2563156187534332, -0.2705031931400299, 0.6169092059135437, -0.17081649601459503, 0.06951519101858139, 0.5274691581726074, -0.3127264678478241, -0.1909281760454178, -0.29582369327545166, 0.2808395326137543, -0.32688429951667786, -0.4111603796482086, 0.04424483701586723, 0.00014146168541628867, -0.15887080132961273, -0.0826968252658844, 0.10147181153297424, 0.0020575623493641615, 1.0924855470657349, 0.0957135334610939, 0.05616037920117378, -0.5383806228637695, 0.6570320129394531, -0.4507873356342316, -0.26736947894096375, -0.6647743582725525, -0.5077613592147827, -0.20666192471981049, 1.0052709579467771, 0.2500501573085785, -0.33324432373046875, 0.6031894683837891, 0.16597358882427216, -0.21446996927261353, -0.36212095618247986, -0.4385171830654144, -0.32567858695983887, 0.2669992744922638, 0.2537837624549866, 0.0055445232428610325, -0.6816214919090271, 0.6173223853111267, -0.3251509964466095, -0.2188236564397812, 0.07095031440258026, 0.1969088315963745, -0.2133328914642334, -0.055829714983701706, 0.4432484805583954, 0.3987100124359131, -0.22252953052520752, -0.19716697931289673, -0.05809066817164421, -0.5640477538108826, -0.2092471569776535, 0.6944459080696106, -0.1397988647222519, -1.891688346862793, -0.24114759266376495, 0.07213540375232697, 0.3722339570522308, 0.027464469894766808, -0.2911287248134613, -0.02170037291944027, 0.4694027602672577, 0.15404647588729858, -0.3493378162384033, 0.1955726146697998, 0.03233436867594719, -0.26189175248146057, -0.047204870730638504, 0.08316688984632492, -0.1366259604692459, 0.4637993574142456, 0.0280937347561121, 0.396906465291977, 0.09263642877340315, -0.024669259786605835, 0.5541477799415588, -0.4485980570316314, 0.4880717098712921, -0.18865829706192017, -0.2861640155315399, 0.5822334289550781, 0.0807146430015564, -0.18600928783416748, 0.1471378654241562, -0.09995889663696288, -0.5797222256660461, -0.6026214957237244, 0.6344447731971741, 0.0553724467754364, 0.19870662689208984, 0.16099031269550323, -0.023098865523934364, 0.053188443183898926, 0.0950823649764061, -0.3816023766994476, -0.4243880808353424, 0.05151847004890442, 0.07268477976322174, 0.5671274065971375, -0.06514880806207657, -0.20534390211105347, -0.1759728640317917, -0.07605334371328354, 0.08852709084749222, -0.08034645766019821, -0.04862220957875252, -0.2908840477466583, -0.17540879547595978, 0.03768877312541008, -0.027512043714523315, -0.7800474762916565, 0.17782925069332123, -0.2737586498260498, -0.20879411697387695, 0.2253115177154541, -0.5256825089454651, -0.36244335770606995, 0.03414274752140045, 0.727904736995697, 0.23965336382389069, 0.06823881715536118, -0.26489612460136414, -0.03796206787228584, 0.1882048398256302, -0.039679110050201416, 0.03730296716094017, 0.2780005633831024, -0.46338310837745667, -0.25510719418525696, 0.11598020792007446, -0.1853385716676712, 0.02539451979100704, 0.2071397304534912, -0.05657915398478508, -0.7258894443511963, 0.6413481831550598, -0.2987813651561737, -0.18197153508663175, 0.07817361503839493, -0.14187310636043549, -0.22067026793956757, -0.3477156162261963, 0.021855393424630165, -0.07829301804304123, 0.105029858648777, 0.34452715516090393, -0.124984472990036, 0.5031864047050476, 0.3732692003250122, 0.22850875556468964, 0.04143736883997917, -0.15287651121616364, -0.36122751235961914, 0.7289406657218933, 0.03737619519233704, -0.2754575312137604, 0.2846193015575409, -0.02566666342318058, -0.0867014154791832, -0.3045170307159424, 0.07351451367139816, -0.119392991065979, -0.24484075605869293, -0.2677750289440155, 0.09638103097677232, -0.1434258371591568, -1.2376574277877808, 0.5015864968299866, -0.05274270102381706, 0.30012246966362, 0.00981383491307497, 0.36305955052375793, 1.4137357473373413, 0.3850996792316437, 0.2777651250362396, -0.0008030235767364502, -0.4734073579311371, 0.009954889304935932, -0.13329993188381195, -1.2159503698349, -0.4902100563049317, 0.15810810029506683, 0.5567760467529297]', '{\"亲子\": 1.0, \"共读\": 0.38253054406732034, \"最好\": 0.4425628906775104, \"朗诵\": 0.2864676290643344, \"阅读\": 0.5171993528840728}', '2025-10-07 13:47:37');
INSERT INTO `content_feature` VALUES (130, 61, '[-0.5334064960479736, 0.5260862708091736, -0.5993046164512634, -0.14863382279872894, 0.6231474280357361, -0.10209286212921144, 0.011313418857753277, -0.09137669205665588, 0.10825029760599136, 0.3679637014865875, -0.36166879534721375, 0.09199011325836182, 0.698269784450531, -0.7406325340270996, 0.39246633648872375, -0.003003222169354558, 0.06146480143070221, 3.620393753051758, -0.13442455232143402, -0.427107572555542, 0.2043824642896652, 0.29848259687423706, -0.4498189389705658, -0.26720044016838074, 0.3267974257469177, 0.5491345524787903, -0.02814653515815735, -0.19156937301158905, -0.3844744861125946, -1.0073572397232056, -0.13111047446727753, -0.1872666627168655, 0.08771670609712601, -0.5809702277183533, -0.4649488925933838, -0.37061607837677, 0.06092118099331856, 0.04928453266620636, -0.12435034662485124, -1.2913905382156372, 0.20546476542949677, 0.2125980108976364, 0.24669738113880155, 0.16608338057994845, 0.42326173186302185, 0.034537333995103836, -0.2319246083498001, 0.05762214958667755, 1.322867751121521, 0.6197303533554077, 0.24025385081768036, -0.7178993225097656, -0.17844872176647186, 0.01485327910631895, 0.17455142736434937, -0.38006338477134705, 0.2749318778514862, 0.16750866174697876, -0.39953088760375977, -0.5727755427360535, -0.17319905757904053, 0.011302907951176168, -0.10624875873327257, 0.061015766113996506, -1.1658331155776978, 0.15084585547447205, -0.06572668999433517, -0.16795681416988373, -0.21709458529949188, 0.41274452209472656, -0.3110123574733734, 0.03333248198032379, 0.9730824828147888, 0.04226674512028694, -0.18064604699611664, 0.15711183845996857, -0.1758345365524292, -0.16362030804157257, -0.011249949224293232, 1.127246975898743, -0.2108908444643021, -0.1309042125940323, 0.6310209631919861, -0.11703068763017654, -0.441394180059433, 0.4205588400363922, 0.1354823261499405, 0.283239483833313, 0.9051563143730164, -0.10246729850769044, 0.16550996899604797, -0.7824785709381104, 0.7053537964820862, -0.36612626910209656, -0.4670399427413941, -0.2073188275098801, -0.02401495911180973, -0.04151284322142601, 0.5474393367767334, 0.14933599531650543, -0.3330077528953552, 0.5080838799476624, -0.23099923133850095, -0.1136954128742218, -0.07390891760587692, -0.7659969329833984, -0.43690791726112366, 0.8725013732910156, 0.1994187980890274, -0.3530396521091461, -0.08956757932901382, 0.3658442497253418, -0.0922754630446434, -0.16251103579998016, 0.34343087673187256, -0.01450541615486145, -0.3231252133846283, 0.04180192947387695, 0.18266218900680545, 0.6598301529884338, 0.021849438548088074, -0.30733799934387207, -0.10778871923685074, -0.10212024301290512, -0.19252967834472656, 0.8068106770515442, 0.019173284992575645, -1.4688329696655271, -0.15251801908016205, -0.19110484421253204, 0.3592231571674347, 0.04936905577778816, -0.18941475450992584, 0.42605313658714294, 0.2570287883281708, -0.049109864979982376, -0.06292613595724106, 0.4939891993999481, -0.02225649356842041, -0.32592514157295227, -0.31679630279541016, 0.19325773417949677, -0.3005348742008209, 0.17566239833831787, 0.17888200283050537, -0.36263930797576904, 0.17395885288715365, -0.2893201410770416, 0.7171962261199951, -0.07233663648366928, 0.23372673988342285, 0.26033684611320496, -0.696366012096405, 0.3094530701637268, 0.07305256277322769, -0.253629595041275, -0.1046939417719841, 0.1495460718870163, -0.42761850357055664, -1.1606683731079102, 0.5929416418075562, 0.29019495844841003, 0.43143248558044434, -0.37428638339042664, -0.026463240385055545, -0.1463693082332611, -0.3532029688358307, -0.09975159913301468, -0.6483243107795715, -0.510230541229248, 0.2372456341981888, 0.35024645924568176, -0.4179038107395172, -0.7966726422309875, -0.22502155601978305, -0.32506266236305237, 0.2419096082448959, -0.16070963442325592, 0.34171879291534424, -0.4543122351169586, -0.03163135424256325, -0.030794085934758183, 0.04473124444484711, 0.08074947446584702, -0.07106561213731766, -0.34767386317253113, -0.0004666099848691374, 0.25796517729759216, -0.1478293240070343, -0.35814782977104187, -0.3353286683559418, 0.19135646522045135, 0.303426593542099, 0.25622251629829407, -0.44741371273994446, -0.08297154307365417, 0.3458012342453003, -0.35914838314056396, 0.13955028355121613, -0.10585325956344604, -0.18209420144557953, 0.1929492950439453, 0.19215929508209229, 0.07093016058206558, -0.22092729806900024, -0.100061796605587, -0.23702478408813477, -0.4324363172054291, 0.6295919418334961, -0.4977087080478668, -0.3970417082309723, -0.26402321457862854, 0.4262585639953614, -0.1944858878850937, -0.6474993228912354, 0.15889610350131989, 0.5469307899475098, 0.05234694480895996, 0.34387651085853577, -0.3831473886966706, 0.1906100958585739, 0.37936100363731384, 0.3461599349975586, -0.4788205921649933, -0.3252115845680237, -0.12728458642959595, 0.5434157848358154, -0.036853108555078506, -0.03170572593808174, 0.2827970087528229, 0.3167392313480377, -0.19197829067707065, -0.01476884912699461, 0.2757190763950348, -0.5412824749946594, -0.14158833026885986, -0.15788085758686066, -0.33802852034568787, 0.042363446205854416, -0.8050225377082825, 0.4329475462436676, 0.0660252794623375, 0.3527137339115143, 0.21854005753993988, 0.5999346375465393, 1.431879997253418, 0.02065878175199032, -0.3690895140171051, -0.01066790521144867, 0.007706304546445608, 0.440830796957016, -0.24704021215438843, -0.5159077644348145, -0.1767667979001999, -0.0040397047996521, 0.13560150563716888]', '{\"一起\": 0.46557371869277614, \"亲子\": 0.7978299320501343, \"女儿\": 0.48825905192463454, \"阅读\": 1.0}', '2025-10-07 13:47:40');
INSERT INTO `content_feature` VALUES (131, 60, '[-0.38915643095970154, 0.4301278293132782, -0.7069819569587708, -0.03335655853152275, 0.7710885405540466, 0.056854527443647385, 0.09192421287298204, 0.20196090638637543, 0.3566564619541168, 0.4163075387477875, -0.2376021146774292, 0.1785038858652115, 0.42534494400024414, -0.5268541574478149, 0.07668695598840714, 0.005887683015316725, -0.46659764647483826, 3.5862090587615967, 0.20903676748275757, -0.2069830745458603, -0.16952739655971527, 0.7286247611045837, -0.4660521447658539, -0.04606106877326965, 0.22260133922100067, 0.6651219725608826, -0.2289428263902664, 0.12028825283050536, -0.5215569138526917, -0.6660988330841064, -0.48302337527275085, 0.009283225052058697, 0.15740181505680084, -0.7402958869934082, -0.2703317701816559, -0.33152830600738525, -0.2442338466644287, -0.3284452259540558, 0.28363195061683655, -0.8243341445922852, -0.15136773884296417, 0.41907429695129395, 0.15621720254421234, -0.15334701538085938, 0.6318144798278809, 0.17116153240203855, -0.7819690704345703, -0.0830276757478714, 1.2610373497009275, 0.49171820282936096, 0.2801545262336731, -0.47769176959991455, -0.4331086874008178, -0.19017887115478516, 0.15528683364391327, -0.517974317073822, 0.22175483405590057, 0.16044209897518158, -0.22359085083007812, -0.7445394396781921, -0.10685182362794876, -0.28294891119003296, 0.031498897820711136, -0.33871015906333923, -0.806534469127655, 0.28853297233581543, 0.3135489225387573, -0.32429075241088867, -0.3089573085308075, 0.3292003273963928, 0.02936258353292942, -0.4343724250793457, 0.6620838642120361, -0.2930684983730316, 0.13952749967575073, -0.1849352866411209, 0.01095298957079649, 0.20501284301280975, 0.08534758538007736, 0.3249475061893463, 0.19361241161823273, 0.0027816544752568007, 0.8693475723266602, 0.12744863331317902, -0.4232930839061737, 0.41410496830940247, 0.27900969982147217, 0.04678793624043465, 0.7288880348205566, 0.033300962299108505, 0.007891260087490082, -0.7290287017822266, 0.6574447751045227, 0.05424327775835991, -0.6117042899131775, 0.14428851008415222, -0.4447689056396485, -0.09956876188516615, 0.7154362201690674, 0.4693868160247803, -0.35869279503822327, 0.3867737054824829, -0.042999494820833206, -0.2258952409029007, 0.1635848879814148, -0.566059410572052, -0.44311967492103577, 0.6104564070701599, 0.3536945581436157, -0.1047126054763794, 0.08366290479898453, 0.29048672318458557, -0.28280314803123474, -0.07445428520441055, -0.09006177634000778, -0.20658022165298465, -0.45693838596343994, 0.17786948382854462, -0.22599218785762787, 0.8521677851676941, -0.3260255753993988, -0.4041851758956909, 0.13157592713832855, -0.33042117953300476, -0.4961855411529541, 0.630151093006134, 0.009609758853912354, -1.574458122253418, 0.18170101940631864, 0.3664743900299072, 0.12117775529623032, -0.02146876789629459, 0.09469733387231828, 0.34082892537117004, -0.2884179353713989, -0.3677471578121186, 0.1597001999616623, 0.48775771260261536, 0.34431543946266174, -0.5494655966758728, -0.5969571471214294, -0.14910490810871124, -0.08609384298324585, -0.02362070418894291, -0.26475265622138977, -0.5747659802436829, -0.08735401183366776, -0.4547432363033294, 0.8915342688560486, 0.10670702904462814, 0.08384857326745987, 0.24906547367572784, -0.6814213395118713, 0.4289848506450653, 0.5517033934593201, -0.23324675858020785, 0.27561938762664795, 0.06821348518133163, -0.4196341037750244, -1.0573838949203491, 0.9359021186828612, 0.4946858584880829, 0.20506781339645383, -0.659876823425293, 0.07051694393157959, -0.3596099317073822, -0.3351137340068817, -0.12781323492527008, -0.17640872299671173, -0.2019786685705185, -0.13437248766422272, 0.25781509280204773, -0.25972843170166016, 0.12387889623641968, -0.238855242729187, -0.08625558018684387, -0.3394912779331207, -0.1356394588947296, 0.294074147939682, -0.14032810926437378, -0.2920311987400055, -0.28676602244377136, -0.27377113699913025, -0.11979082971811296, 0.2343272715806961, -0.14693152904510498, 0.013465295545756817, 0.10982926934957504, 0.18159647285938263, -0.5499626398086548, 0.18589402735233307, 0.10961278527975082, 0.13606034219264984, -0.2971215546131134, -0.3344478905200958, 0.3229375183582306, -0.01378399133682251, 0.08865895122289658, 0.08450400829315186, 0.33742713928222656, -0.29130277037620544, 0.056371647864580154, -0.11499115824699402, 0.16109217703342438, 0.25297072529792786, -0.03941699489951134, -0.05167730525135994, -0.3939487040042877, 0.22634102404117584, -0.3371849060058594, -0.27988168597221375, -0.16935022175312042, 0.13347594439983368, -0.11729761213064194, -0.32569625973701477, -0.014859527349472046, 0.6226256489753723, 0.48582470417022705, 0.7192065119743347, -0.9105343222618104, 0.6988496780395508, 0.42505836486816406, 0.4008886516094208, 0.13994209468364716, -0.28341495990753174, -0.35355231165885925, 0.2212342768907547, -0.4744226932525635, 0.04513353109359741, 0.3564382493495941, 0.44061923027038574, 0.2747144401073456, -0.0388447605073452, 0.0483240969479084, -0.7987126708030701, -0.3288014829158783, -0.14665721356868744, 0.004774888511747122, 0.04135510325431824, -1.1311510801315308, 0.08639931678771973, 0.487949013710022, -0.050234656780958176, -0.09847728163003922, 0.3270013630390167, 1.830806255340576, 0.06320410966873169, 0.04663574695587158, 0.06762271374464035, -0.2680896818637848, 0.0012161731719970703, -0.33048027753829956, -0.5241796374320984, -0.18724112212657928, -0.22077226638793945, 0.08698467165231705]', '{\"世界\": 0.3466321204899466, \"读书\": 1.0}', '2025-10-07 13:47:42');
INSERT INTO `content_feature` VALUES (132, 59, '[-0.6790391802787781, 0.09170929342508316, -0.25399351119995117, 0.058717548847198486, 0.6077470779418945, -0.1667148321866989, -0.5164479613304138, 0.4885928928852082, 0.5393348932266235, 0.09403196722269058, -0.590543806552887, -0.2898949086666107, 0.21488921344280243, -0.19271934032440183, 0.2949528396129608, -0.013217595405876637, -0.27577388286590576, 3.567970514297486, 0.20068593323230743, 0.04317636415362358, 0.17246460914611816, 0.2653147280216217, -0.5834044814109802, 0.1277233064174652, 0.7797484397888184, 0.4104648530483246, 0.41500917077064514, -0.006227692123502493, -0.4581269323825836, -0.717156708240509, -0.1006370410323143, 0.20933334529399872, 0.09150748699903488, -0.5645833611488342, -0.30302056670188904, -0.29997381567955017, -0.5196754932403564, 0.10092911124229433, -0.28110918402671814, -0.9518983364105223, -0.0968540981411934, 0.6188610196113586, -0.02933489717543125, -0.09997601062059402, 1.0907610654830933, 0.13689561188220978, -0.5413476824760437, 0.27012789249420166, 0.818009614944458, 0.5024604797363281, -0.3587164878845215, -0.38002753257751465, -0.01974049210548401, -0.017370184883475304, 0.6564260721206665, -0.6872725486755371, 0.11908825486898422, -0.2735951244831085, -0.022344907745718956, -0.5512604713439941, -0.10600472241640092, -0.2571829557418823, 0.23426125943660736, 0.5399366021156311, -0.9679558873176576, -0.5390123724937439, 0.06549104303121567, -0.646260142326355, 0.3200949430465698, 0.7354412078857422, -0.016424184665083885, -0.3095691204071045, 0.9674857258796692, -0.0925866961479187, 0.2348527759313583, -0.15987645089626312, 0.0964144840836525, -0.1551747769117355, 0.04286504909396171, 0.22591960430145264, 0.002609163522720337, -0.03777503967285156, 0.25387656688690186, 0.3468984067440033, -0.40361857414245605, 0.4502001702785492, -0.12255501747131348, 0.02098008431494236, 0.7032163739204407, 0.09773672372102736, 0.03950296714901924, -0.7812774777412415, 0.35679662227630615, -0.3483501970767975, -0.14791442453861237, 0.2589903175830841, -0.4455658495426178, -0.4866691827774048, 1.003277063369751, 0.3125779628753662, -0.3423764407634735, -0.019030386582016945, -0.8074116110801697, -0.1944803148508072, 0.048191845417022705, -0.5869773030281067, -0.4036968648433686, 0.2943807542324066, 0.7259681224822998, -0.2965417206287384, -0.10800950974225998, 0.3020208477973938, -0.2559824287891388, -0.24727869033813477, 0.12487927824258804, -0.14209647476673126, -0.15559127926826477, 0.1398535966873169, -0.01883928664028645, 0.5116415023803711, -0.245769664645195, -0.354074090719223, -0.07756864279508591, -0.08534985035657883, -0.4934022724628449, 0.0876743495464325, 0.5053008198738098, -1.5960102081298828, 0.08212820440530777, 0.0064977011643350124, 0.0592411644756794, -0.05461184307932854, -0.27365216612815857, 0.4485863149166107, 0.09685616940259932, 0.07399971038103104, 0.004355391021817923, 0.3277778923511505, 0.35683944821357727, -0.0295442845672369, -0.3601773977279663, -0.1162162348628044, -0.26594412326812744, 0.6297073364257812, 0.4222372770309448, -0.11253931373357771, -0.0028487045783549547, -0.3798561096191406, 0.6457231640815735, -0.23996572196483612, 0.16526466608047485, 0.07981544733047485, -0.5672880411148071, 0.2072722166776657, 0.36381426453590393, -0.3342810571193695, 0.3887281715869904, 0.013144214637577534, -0.32562142610549927, -0.6347720623016357, 0.6539077162742615, 0.20503997802734375, 0.2003608196973801, -0.46973717212677, -0.10912898182868958, -0.22770826518535617, -0.2658013105392456, -0.247830793261528, -0.12023920565843582, -0.3703325688838959, -0.026531973853707314, 0.27244219183921814, -0.3504157066345215, -0.2955503761768341, 0.2005854696035385, -0.25156500935554504, 0.028491869568824768, -0.3551332652568817, 0.48402777314186096, -0.2236928194761276, 0.0591895692050457, -0.6033835411071777, -0.34359073638916016, -0.022148648276925087, 0.250460147857666, 0.30987223982810974, 0.22013770043849945, 0.2812877595424652, -0.27735501527786255, -0.005310654640197754, -0.4240607023239136, 0.2891536056995392, 0.26012757420539856, 0.34749069809913635, -0.4640466272830963, 0.1844102144241333, 0.6787092089653015, -0.5930780172348022, -0.058278292417526245, 0.5190841555595398, -0.2727403938770294, 0.35037991404533386, -0.008412186987698078, -0.2393304705619812, -0.12356185913085938, -0.1307346671819687, -0.5036950707435608, -0.7598299384117126, 1.0233498811721802, -0.39010998606681824, -0.125215545296669, -0.2819807231426239, 0.3614557981491089, -0.38236430287361145, -0.5647436380386353, -0.10891520977020264, 0.4101685583591461, 0.09698525816202164, 0.6991779804229736, -0.8048532009124756, 0.364330917596817, 0.5263424515724182, 0.23246687650680545, -0.4696507751941681, -0.11301668733358385, -0.20685116946697235, 0.1113780215382576, -0.3002358078956604, -0.02250019647181034, 0.45525792241096497, 0.682898998260498, -0.4748861789703369, -0.0642157793045044, 0.04800214245915413, -0.6691581606864929, -0.05835014581680298, 0.16401636600494385, -0.7853602766990662, 0.4474915564060211, -0.9111220240592957, 0.5181800723075867, 0.11632400751113892, 0.32487353682518005, -0.14612466096878052, 0.4436476528644562, 1.752702236175537, -0.021281441673636436, -0.39062848687171936, 0.09803131967782974, -0.45253095030784607, 0.2575550973415375, -0.1950287818908691, -0.3433111011981964, -0.5104578137397766, -0.14072982966899872, 0.03348206728696823]', '{\"之道\": 0.7928054610163616, \"时政\": 0.8454563347646601, \"读书\": 0.6952296703538441, \"习近平\": 1.0}', '2025-10-07 13:47:45');
INSERT INTO `content_feature` VALUES (133, 101, '[-0.20573830604553223, 0.2261679768562317, -0.23055066168308255, -0.4682445228099823, -0.11012061685323717, 0.12937551736831665, 0.447124719619751, 0.21667933464050293, -0.006919304374605417, 0.39731815457344055, -0.28883835673332214, 0.5484446883201599, -0.1741882562637329, -0.1215781792998314, 0.32481297850608826, -0.1784578561782837, -0.4351262152194977, 3.7871809005737305, 0.6932455897331238, -0.3851850032806397, 0.12057241797447205, 0.3871837854385376, 0.04163007065653801, -0.010167728178203106, -0.23652283847332, 0.5469591021537781, -0.4720517694950104, -0.3872865438461304, -0.0929047241806984, -0.5334873795509338, -0.3690558671951294, -0.06200230121612549, -0.31853723526000977, -0.6350443959236145, -0.7566239237785339, 0.14998532831668854, 0.03320923447608948, -0.28166306018829346, 0.03524096682667732, -0.7234427332878113, -0.4607642889022827, -0.047043636441230774, -0.15911664068698883, 0.018653109669685364, 0.3796280324459076, -0.11419832706451416, -0.26421985030174255, 0.20816825330257416, 0.5648356080055237, 0.6769633293151855, -0.10583002120256424, -0.4695093631744385, -0.4953356981277466, 0.4752897322177887, -0.17578010261058807, -0.013205170631408691, 0.46084973216056824, -0.3110915720462799, -0.2888886034488678, -0.2484984844923019, -0.4387312829494476, 0.27065563201904297, -0.13165943324565887, 0.6348979473114014, -1.3927116394042969, 0.29183098673820496, -0.037631552666425705, -0.6601958274841309, -0.28234583139419556, 0.2994186282157898, -0.22201363742351532, -0.4148625433444977, 0.5232645869255066, 0.06834861636161804, -0.3756090700626374, -0.00497628515586257, -0.30910563468933105, -0.3360973596572876, -0.05409284308552742, 0.4054027795791626, -0.12282368540763856, -0.3412937819957733, 0.21776534616947177, 0.0020641982555389404, -0.30103474855422974, 0.376460462808609, 0.3483785092830658, 0.20574967563152313, 0.6424174904823303, -0.5235193371772766, -0.1337270438671112, -0.9475367069244384, 0.1632811427116394, -0.578630268573761, -0.13557398319244385, 0.3798377215862274, 0.16759194433689115, -0.20732708275318143, 0.8086905479431152, 0.2121468037366867, 0.1345437616109848, 0.686777651309967, -0.121923066675663, 0.39131829142570496, 0.04498879238963127, -0.3772774040699005, -0.3735835254192352, 0.7894110679626465, 0.19635380804538727, 0.3146266043186188, -0.2868295907974243, 0.14568907022476196, -0.4772976338863373, -0.9416615962982178, 0.5735763311386108, -0.16682328283786774, -0.31091412901878357, -0.0501662902534008, 0.2503231167793274, 0.5287861824035645, 0.24931691586971283, -0.1272769719362259, -0.3961980640888214, -0.2536640465259552, -0.3197232484817505, 0.5324651598930359, -0.20886220037937164, -1.8059145212173464, -0.4677084982395172, -0.17916160821914673, 0.5294037461280823, -0.7830233573913574, -0.7253554463386536, 0.05528765544295311, 0.7151403427124023, -0.12669260799884796, 0.1053074225783348, 0.4782468378543854, 0.41141584515571594, -0.5180820822715759, -0.7551240921020508, 0.34606465697288513, 0.1130235567688942, -0.039070796221494675, 0.30552682280540466, 0.19539810717105863, 0.12084115296602248, -0.36989274621009827, 0.7269783020019531, -0.4946563243865967, -0.17968939244747162, -0.2891964018344879, -0.5490798354148865, 1.120249629020691, -0.18388289213180545, 0.13674186170101166, 0.5758671164512634, 0.38875946402549744, -0.09640708565711976, -0.3245649039745331, 0.7865660190582275, 0.42354846000671387, 0.33630383014678955, -0.2750970721244812, 0.24683277308940887, 0.4891674816608429, -0.494492381811142, 0.02778034470975399, 0.38908228278160095, -0.05643012002110481, -0.22561681270599365, 0.642996609210968, 0.21684397757053375, -0.0850091278553009, -0.05929495766758919, -0.195897713303566, 0.05419419705867767, -0.5747659206390381, 0.047941405326128006, -0.3017241954803467, 0.03049774281680584, -0.504235029220581, -0.12279456853866576, -0.4301989078521729, -0.048545658588409424, -0.01855219341814518, 0.24894483387470245, -0.45106855034828186, -0.3539792597293854, 0.0814286470413208, -0.10358764976263046, 0.5078269839286804, 0.34237468242645264, 0.019255489110946655, 0.2356591820716858, 0.13848961889743805, 0.23745572566986084, -0.14123274385929108, -0.38422679901123047, 0.5842162370681763, -0.3305979371070862, -0.11287110298871994, -0.16013436019420624, -0.6977974772453308, 0.3825293481349945, 0.1959826946258545, -0.22818364202976227, -0.42470505833625793, 1.0698126554489136, -0.4730306565761566, -0.1415783017873764, 0.10333117097616196, -0.07961688190698624, -0.5807104706764221, -0.5459046363830566, -0.11044317483901978, -0.20669077336788175, 0.32172319293022156, 0.4720841646194458, 0.00649598240852356, 0.08873306959867477, 0.06665454059839249, 0.4578251838684082, -0.38167157769203186, -0.1805422455072403, -0.4356825351715088, 0.3060273826122284, 0.32779523730278015, 0.10022499412298204, 0.23607785999774933, 0.35730740427970886, -0.1182853952050209, -0.4230450093746186, -0.4697957932949066, -0.15743374824523926, 0.2256861925125122, -0.1637478768825531, 0.1363159865140915, -0.2991001605987549, -0.3628692626953125, 0.31704261898994446, -0.1525694578886032, 0.5816062092781067, 0.31743714213371277, 0.6066752076148987, 1.0537763833999634, 0.02668646909296513, 0.27577248215675354, 0.10227513313293456, -0.434469610452652, 0.10957074165344238, -0.41108980774879456, -0.6474291682243347, 0.048204969614744186, 0.14186860620975494, 0.22389788925647736]', '{\"30\": 0.2515412912418222, \"101\": 0.2515412912418222, \"大脑\": 0.22942897266784093, \"思维\": 0.7307978159025413, \"数学\": 1.0, \"标题\": 0.24001045857157924, \"生活\": 0.2225183706914858, \"能力\": 0.25839639981150636, \"能够\": 0.30179010131778855, \"良好\": 0.21649482025054556, \"解决\": 0.23439962315347135, \"训练\": 0.4005549271417799, \"进行\": 0.23813016246739965, \"锻炼\": 0.25299901934984176, \"问题\": 0.21582501823751504, \"难题\": 0.2454463945124705, \"每天花\": 0.26331621516892345, \"重要性\": 0.2139194090106528, \"好地解决\": 0.2176769697014466, \"逻辑推理\": 0.5148830973089502}', '2025-10-07 16:00:16');
INSERT INTO `content_feature` VALUES (134, 201, '[-0.004110187292098999, 0.0008654792909510434, -0.669137179851532, -0.029188642278313637, 0.4533441364765167, -0.22523252665996552, -0.19873274862766263, 0.16511386632919312, 0.10742930322885512, 0.4726787805557251, -0.32354187965393066, 0.5763841271400452, -0.239985391497612, 0.23682105541229248, 0.7538338303565979, 0.05430251359939575, -0.26151686906814575, 3.5947606563568115, 0.22626642882823944, -0.4200967848300934, 0.0159097108989954, 0.1889670491218567, -0.6379738450050354, -0.35772109031677246, 0.1292672902345657, 0.21647952497005463, 0.14264380931854248, 0.05929484963417053, -0.27717673778533936, -0.6016895174980164, -0.24033069610595703, 0.2875395715236664, 0.4558048546314239, -0.16727256774902344, -0.2693975567817688, -0.3632483184337616, -0.25153693556785583, 0.12360667437314989, -0.3144684433937073, -0.4827398955821991, 0.12135976552963255, -0.08798351883888245, -0.18574219942092896, 0.7375752329826355, 0.851513147354126, 0.3739937245845794, -0.424060732126236, 0.07709386199712753, 0.21145223081111908, 0.003768702270463109, -0.11995986849069595, -0.13693499565124512, -0.2369859963655472, -0.20685435831546783, 0.010496646165847778, -0.3121102750301361, -0.050797734409570694, 0.9028707146644592, -0.17980551719665527, -0.3410066068172455, -0.058163803070783615, -0.14660245180130005, -0.27386704087257385, 0.5900399088859558, -0.7467240691184998, 0.09114617854356766, -0.00236125779338181, -0.779207170009613, -0.1552940458059311, 0.03941841050982475, -0.11899670213460922, -0.04167558252811432, 0.02776114083826542, -0.09102631360292436, -0.05580808594822883, 0.01958528161048889, -0.06681378185749054, -0.10133091360330582, -0.14139416813850403, 0.6064557433128357, -0.26262691617012024, 0.04415510222315788, -0.34008702635765076, -0.024381408467888832, -0.3195965588092804, 0.2967436611652374, 0.5034483671188354, 0.3685885965824127, 0.3912447988986969, 0.05712840333580971, 0.055243413895368576, -0.5564050078392029, 0.796165406703949, -0.2866210639476776, -0.17455367743968964, 0.34561729431152344, -0.17857341468334198, -0.2204017639160156, 0.8100993633270264, 0.1302119940519333, -0.10855456441640854, 0.6031550765037537, -0.37838777899742126, 0.09637463092803956, -0.06854959577322006, -0.5397047996520996, -0.16908298432826996, 0.1085755005478859, 0.07353036850690842, -0.2973659336566925, -0.9525291919708252, 0.22762972116470337, -0.28713133931159973, -0.2697034776210785, 0.5087806582450867, 0.1723889708518982, -0.33910489082336426, 0.2009413242340088, 0.3951387703418732, 0.46712175011634827, 0.028431912884116173, -0.14590148627758026, 0.016315439715981483, -0.1095615103840828, -0.2791236937046051, 0.7719831466674805, -0.17737342417240143, -1.6574420928955078, -0.2922605276107788, -0.18865488469600675, -0.13043242692947388, 0.07846629619598389, -0.11819025874137878, -0.24099208414554596, 0.4847480356693268, -0.49985966086387634, -0.39291027188301086, 0.2464504241943359, 0.3022889792919159, 0.22165589034557345, -0.25095927715301514, 0.23293574154376984, -0.20685017108917236, 0.0797136053442955, 0.3413284122943878, 0.2442130297422409, 0.21192239224910736, 0.10816946625709534, 1.032256007194519, -0.6570025086402893, 0.14024046063423157, -0.2751639783382416, -0.3237449824810028, 0.5582351088523865, 0.3882039487361908, -0.3258006274700165, 0.011341333389282228, 0.059399232268333435, -0.2408155053853989, -0.6709614396095276, 0.9306426048278807, 0.38984957337379456, -0.1287439614534378, -0.183094784617424, -0.5597489476203918, 0.009602293372154236, -0.019881218671798703, -0.4249715805053711, 0.04698812961578369, -0.2310265451669693, -0.12818755209445953, 0.5261979699134827, 0.07342364639043808, 0.27259084582328796, 0.04989427328109741, -1.0618780851364136, -0.16762296855449677, -0.4919184148311615, -0.041102368384599686, -0.1864093542098999, 0.17238116264343262, 0.08512204885482788, -0.604496955871582, -0.530282199382782, -0.07117254287004471, -0.13104622066020966, -0.03137648105621338, -0.06744612753391266, 0.1282365471124649, -0.8231059908866882, 0.021856268867850304, 0.6462227702140808, 0.6677129864692688, 0.06314519792795181, -0.4618327915668487, 0.11213778704404832, 0.2723003029823303, -0.7388054728507996, -0.1373092532157898, 0.5108142495155334, -0.7897956967353821, -0.18821842968463895, -0.039807628840208054, -0.13672283291816711, 0.09726595878601074, 0.2892686128616333, -0.3384310305118561, 0.2134185880422592, 0.9854989051818848, -0.5642295479774475, -0.11497005075216292, 0.3989146947860718, 0.3874888420104981, -0.21840667724609375, 0.01511432696133852, -0.2597098648548126, 0.35743510723114014, 0.35764023661613464, 0.22719864547252655, -0.4614393413066864, 0.4505327641963959, 0.1776466965675354, 0.34466758370399475, -0.5531644225120544, -0.6514267325401306, -0.011339719407260418, 0.05307528004050255, 0.4272627532482147, -0.34131893515586853, -0.09553853422403336, 0.4416971206665039, -0.19168388843536377, 0.10819574445486067, 0.03459513187408447, -0.2849094867706299, 0.35224997997283936, -0.7842381000518799, 0.18246598541736603, 0.2052985429763794, -0.5254945755004883, 0.20470063388347623, -0.13388611376285553, 0.06626073271036148, 0.07672517746686935, 0.18283413350582123, 1.2216944694519043, -0.02776557207107544, -0.26355627179145813, -0.07439213246107101, -0.2966992259025574, -0.07492462545633316, -0.09052830934524536, -0.8013436198234558, -0.0012604942312464118, -0.010276496410369871, 0.18575751781463623]', '{\"201\": 0.7932079554869074, \"教程\": 0.6124458873701464, \"标题\": 0.5088396645409268, \"测试\": 0.917090470119164, \"科技\": 0.3873485531729992, \"视频\": 1.0}', '2025-10-07 16:00:18');

-- ----------------------------
-- Table structure for content_image
-- ----------------------------
DROP TABLE IF EXISTS `content_image`;
CREATE TABLE `content_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_content`(`content_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 83 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容图片关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of content_image
-- ----------------------------
INSERT INTO `content_image` VALUES (46, 39, 'http://127.0.0.1:9000/qz-sns/image/article/39/article_image_1.jpg', 0, '2025-09-24 12:21:23');
INSERT INTO `content_image` VALUES (47, 40, 'http://127.0.0.1:9000/qz-sns/image/article/40/article_image_1.jpg', 0, '2025-09-24 12:24:57');
INSERT INTO `content_image` VALUES (48, 41, 'http://127.0.0.1:9000/qz-sns/image/article/41/article_image_1.jpg', 0, '2025-09-24 12:27:19');
INSERT INTO `content_image` VALUES (49, 42, 'http://127.0.0.1:9000/qz-sns/image/article/42/article_image_1.jpg', 0, '2025-09-24 12:29:42');
INSERT INTO `content_image` VALUES (50, 44, 'http://127.0.0.1:9000/qz-sns/image/article/44/article_image_1.jpg', 0, '2025-09-24 12:32:47');
INSERT INTO `content_image` VALUES (51, 45, 'http://127.0.0.1:9000/qz-sns/image/article/45/article_image_1.jpg', 0, '2025-09-24 12:35:41');
INSERT INTO `content_image` VALUES (52, 45, 'http://127.0.0.1:9000/qz-sns/image/article/45/article_image_2.jpg', 1, '2025-09-24 12:35:41');
INSERT INTO `content_image` VALUES (53, 65, 'http://127.0.0.1:9000/qz-sns/image/article/65/59f27c14-d003-4766-9211-f3eb5eeb9ff2.jpg', 0, '2025-09-30 20:16:17');
INSERT INTO `content_image` VALUES (54, 66, 'http://127.0.0.1:9000/qz-sns/image/article/66/6598ee9b-4771-4955-99e2-b68d4e3c47e6.jpg', 0, '2025-09-30 20:22:30');
INSERT INTO `content_image` VALUES (55, 67, 'http://127.0.0.1:9000/qz-sns/image/article/67/58c97143-202f-43ea-ba45-8e8ab07618b6.jpg', 0, '2025-09-30 20:24:08');
INSERT INTO `content_image` VALUES (56, 68, 'http://127.0.0.1:9000/qz-sns/image/article/68/f2554f75-3780-4671-830b-17074c7b31c9.jpg', 0, '2025-09-30 20:25:59');
INSERT INTO `content_image` VALUES (57, 68, 'http://127.0.0.1:9000/qz-sns/image/article/68/2ad53ac8-8979-424b-88e6-f55df78f6268.jpg', 1, '2025-09-30 20:25:59');
INSERT INTO `content_image` VALUES (58, 68, 'http://127.0.0.1:9000/qz-sns/image/article/68/d2c53704-805e-4fc7-8de1-8bdb562c378b.jpg', 2, '2025-09-30 20:25:59');
INSERT INTO `content_image` VALUES (59, 69, 'http://127.0.0.1:9000/qz-sns/image/article/69/c92fe5dc-f359-43e0-9330-00375666723c.jpg', 0, '2025-09-30 20:28:20');
INSERT INTO `content_image` VALUES (60, 70, 'http://127.0.0.1:9000/qz-sns/image/article/70/b5b55779-347b-4c18-a2fa-f31089c617d9.jpg', 0, '2025-09-30 20:30:28');
INSERT INTO `content_image` VALUES (61, 70, 'http://127.0.0.1:9000/qz-sns/image/article/70/b7dcea3c-cd34-483c-b482-bc302dc749ef.jpg', 1, '2025-09-30 20:30:28');
INSERT INTO `content_image` VALUES (62, 71, 'https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611550752506.png', 0, '2025-10-04 22:29:50');
INSERT INTO `content_image` VALUES (63, 71, 'https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611550860295.png', 1, '2025-10-04 22:29:50');
INSERT INTO `content_image` VALUES (64, 71, 'https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611550968955.png', 2, '2025-10-04 22:29:50');
INSERT INTO `content_image` VALUES (65, 71, 'https://www.nationalreading.gov.cn/wzzt/2025qmyddh/cgfb/202504/W020250424611551076247.png', 3, '2025-10-04 22:29:50');
INSERT INTO `content_image` VALUES (66, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_202504230a58e9c79e7d4cc185c28964e84b299d.jpg', 0, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (67, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423af21ceb95d6c4b4696d9460d7eca086b.jpg', 1, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (68, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_202504233f626bdf51f642c2b94a74d43cc97f2a.jpg', 2, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (69, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423c0498a604f334d79a2bf1e532b3edc1d.jpg', 3, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (70, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423612df811fd36412bb4cbb4d088da0ce6.jpg', 4, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (71, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423047b468e13374db88f80b86050b32997.jpg', 5, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (72, 72, 'https://www.news.cn/ci/20250423/bba05e38aa6142c69502d9ed23baccbf/202504231e478a15110f4ff29a3c28bf05e647e2_20250423303e3ba29a56455694e632c1bc0e88a8.jpg', 6, '2025-10-04 22:34:09');
INSERT INTO `content_image` VALUES (73, 74, 'http://127.0.0.1:9000/qz-sns/image/article/74/article_image_1.jpg', 0, '2025-10-21 09:53:33');
INSERT INTO `content_image` VALUES (74, 74, 'http://127.0.0.1:9000/qz-sns/image/article/74/article_image_2.jpg', 1, '2025-10-21 09:53:33');
INSERT INTO `content_image` VALUES (75, 75, 'http://127.0.0.1:9000/qz-sns/image/article/75/article_image_1.jpg', 0, '2025-10-21 09:53:38');
INSERT INTO `content_image` VALUES (76, 75, 'http://127.0.0.1:9000/qz-sns/image/article/75/article_image_2.jpg', 1, '2025-10-21 09:53:38');
INSERT INTO `content_image` VALUES (77, 76, 'http://127.0.0.1:9000/qz-sns/image/article/76/article_image_1.jpg', 0, '2025-10-21 09:53:43');
INSERT INTO `content_image` VALUES (78, 76, 'http://127.0.0.1:9000/qz-sns/image/article/76/article_image_2.jpg', 1, '2025-10-21 09:53:43');
INSERT INTO `content_image` VALUES (79, 77, 'http://127.0.0.1:9000/qz-sns/image/article/77/article_image_1.jpg', 0, '2025-10-21 09:53:47');
INSERT INTO `content_image` VALUES (80, 77, 'http://127.0.0.1:9000/qz-sns/image/article/77/article_image_2.jpg', 1, '2025-10-21 09:53:47');
INSERT INTO `content_image` VALUES (81, 78, 'http://127.0.0.1:9000/qz-sns/image/article/78/article_image_1.jpg', 0, '2025-10-21 09:53:54');
INSERT INTO `content_image` VALUES (82, 78, 'http://127.0.0.1:9000/qz-sns/image/article/78/article_image_2.jpg', 1, '2025-10-21 09:53:54');

-- ----------------------------
-- Table structure for content_similarity
-- ----------------------------
DROP TABLE IF EXISTS `content_similarity`;
CREATE TABLE `content_similarity`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id1` bigint NOT NULL COMMENT '内容ID1',
  `content_id2` bigint NOT NULL COMMENT '内容ID2',
  `similarity_score` decimal(5, 4) NOT NULL DEFAULT 0.0000 COMMENT '相似度分数',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_content_pair`(`content_id1` ASC, `content_id2` ASC) USING BTREE,
  INDEX `idx_content1_score`(`content_id1` ASC, `similarity_score` ASC) USING BTREE,
  INDEX `idx_content2_score`(`content_id2` ASC, `similarity_score` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 841 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容相似度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of content_similarity
-- ----------------------------
INSERT INTO `content_similarity` VALUES (785, 70, 72, 0.7659, '2025-10-07 13:47:11');
INSERT INTO `content_similarity` VALUES (786, 69, 72, 0.8310, '2025-10-07 13:47:15');
INSERT INTO `content_similarity` VALUES (787, 68, 70, 0.8264, '2025-10-07 13:47:18');
INSERT INTO `content_similarity` VALUES (788, 68, 72, 0.7620, '2025-10-07 13:47:18');
INSERT INTO `content_similarity` VALUES (789, 67, 71, 0.8770, '2025-10-07 13:47:22');
INSERT INTO `content_similarity` VALUES (790, 67, 72, 0.8318, '2025-10-07 13:47:22');
INSERT INTO `content_similarity` VALUES (791, 66, 67, 0.8536, '2025-10-07 13:47:25');
INSERT INTO `content_similarity` VALUES (792, 66, 72, 0.8357, '2025-10-07 13:47:25');
INSERT INTO `content_similarity` VALUES (793, 66, 68, 0.8221, '2025-10-07 13:47:25');
INSERT INTO `content_similarity` VALUES (794, 65, 72, 0.8819, '2025-10-07 13:47:29');
INSERT INTO `content_similarity` VALUES (795, 65, 66, 0.8704, '2025-10-07 13:47:29');
INSERT INTO `content_similarity` VALUES (796, 65, 69, 0.8491, '2025-10-07 13:47:29');
INSERT INTO `content_similarity` VALUES (797, 64, 70, 0.5867, '2025-10-07 13:47:31');
INSERT INTO `content_similarity` VALUES (798, 64, 69, 0.5798, '2025-10-07 13:47:31');
INSERT INTO `content_similarity` VALUES (799, 64, 71, 0.5568, '2025-10-07 13:47:31');
INSERT INTO `content_similarity` VALUES (800, 64, 65, 0.5550, '2025-10-07 13:47:31');
INSERT INTO `content_similarity` VALUES (801, 63, 64, 0.7716, '2025-10-07 13:47:34');
INSERT INTO `content_similarity` VALUES (802, 63, 69, 0.6538, '2025-10-07 13:47:34');
INSERT INTO `content_similarity` VALUES (803, 63, 65, 0.6204, '2025-10-07 13:47:34');
INSERT INTO `content_similarity` VALUES (804, 63, 70, 0.6018, '2025-10-07 13:47:34');
INSERT INTO `content_similarity` VALUES (805, 62, 64, 0.7527, '2025-10-07 13:47:37');
INSERT INTO `content_similarity` VALUES (806, 62, 70, 0.7232, '2025-10-07 13:47:37');
INSERT INTO `content_similarity` VALUES (807, 62, 66, 0.6827, '2025-10-07 13:47:37');
INSERT INTO `content_similarity` VALUES (808, 62, 65, 0.6808, '2025-10-07 13:47:37');
INSERT INTO `content_similarity` VALUES (809, 62, 68, 0.6805, '2025-10-07 13:47:37');
INSERT INTO `content_similarity` VALUES (810, 61, 64, 0.8382, '2025-10-07 13:47:40');
INSERT INTO `content_similarity` VALUES (811, 61, 62, 0.7958, '2025-10-07 13:47:40');
INSERT INTO `content_similarity` VALUES (812, 61, 63, 0.7849, '2025-10-07 13:47:40');
INSERT INTO `content_similarity` VALUES (813, 61, 70, 0.6643, '2025-10-07 13:47:40');
INSERT INTO `content_similarity` VALUES (814, 61, 68, 0.6406, '2025-10-07 13:47:40');
INSERT INTO `content_similarity` VALUES (815, 60, 61, 0.8326, '2025-10-07 13:47:42');
INSERT INTO `content_similarity` VALUES (816, 60, 64, 0.8216, '2025-10-07 13:47:42');
INSERT INTO `content_similarity` VALUES (817, 60, 62, 0.7352, '2025-10-07 13:47:42');
INSERT INTO `content_similarity` VALUES (818, 60, 63, 0.7162, '2025-10-07 13:47:42');
INSERT INTO `content_similarity` VALUES (819, 60, 70, 0.6115, '2025-10-07 13:47:42');
INSERT INTO `content_similarity` VALUES (820, 60, 71, 0.5801, '2025-10-07 13:47:42');
INSERT INTO `content_similarity` VALUES (821, 59, 61, 0.8136, '2025-10-07 13:47:45');
INSERT INTO `content_similarity` VALUES (822, 59, 60, 0.7968, '2025-10-07 13:47:45');
INSERT INTO `content_similarity` VALUES (823, 59, 64, 0.7615, '2025-10-07 13:47:45');
INSERT INTO `content_similarity` VALUES (824, 59, 63, 0.7273, '2025-10-07 13:47:45');
INSERT INTO `content_similarity` VALUES (825, 59, 62, 0.7264, '2025-10-07 13:47:45');
INSERT INTO `content_similarity` VALUES (826, 59, 70, 0.5948, '2025-10-07 13:47:45');
INSERT INTO `content_similarity` VALUES (827, 101, 62, 0.7199, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (828, 101, 61, 0.7133, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (829, 101, 68, 0.7097, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (830, 101, 63, 0.6912, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (831, 101, 70, 0.6905, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (832, 101, 59, 0.6687, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (833, 101, 60, 0.6664, '2025-10-07 16:00:16');
INSERT INTO `content_similarity` VALUES (834, 201, 62, 0.7598, '2025-10-07 16:00:18');
INSERT INTO `content_similarity` VALUES (835, 201, 61, 0.7075, '2025-10-07 16:00:18');
INSERT INTO `content_similarity` VALUES (836, 201, 101, 0.7037, '2025-10-07 16:00:18');
INSERT INTO `content_similarity` VALUES (837, 201, 59, 0.6957, '2025-10-07 16:00:18');
INSERT INTO `content_similarity` VALUES (838, 201, 63, 0.6906, '2025-10-07 16:00:18');
INSERT INTO `content_similarity` VALUES (839, 201, 64, 0.6860, '2025-10-07 16:00:18');
INSERT INTO `content_similarity` VALUES (840, 201, 60, 0.6725, '2025-10-07 16:00:18');

-- ----------------------------
-- Table structure for family
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '家庭ID',
  `family_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '家庭名称',
  `creator_id` bigint NOT NULL COMMENT '创建者用户ID',
  `im_group_id` bigint NULL DEFAULT NULL COMMENT '关联的IM群组ID',
  `ai_bot_id` bigint NULL DEFAULT NULL COMMENT '家庭AI机器人用户ID',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:已解散',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator`(`creator_id` ASC) USING BTREE,
  INDEX `idx_im_group`(`im_group_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of family
-- ----------------------------
INSERT INTO `family` VALUES (1, '测试家庭', 32, 1001, -1, 1, '2025-10-10 07:25:28', '2025-10-10 07:25:28');

-- ----------------------------
-- Table structure for family_relation
-- ----------------------------
DROP TABLE IF EXISTS `family_relation`;
CREATE TABLE `family_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `relative_id` bigint NOT NULL COMMENT '亲属ID',
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关系类型',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_relative`(`user_id` ASC, `relative_id` ASC) USING BTREE,
  INDEX `idx_family`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of family_relation
-- ----------------------------
INSERT INTO `family_relation` VALUES (56, 1, 32, 34, '父子,子父', '2025-10-05 12:52:52');
INSERT INTO `family_relation` VALUES (57, 1, 32, 35, '父女,女父', '2025-10-05 16:04:20');
INSERT INTO `family_relation` VALUES (58, 1, 32, 33, '夫妻,夫妻', '2025-10-05 16:04:30');

-- ----------------------------
-- Table structure for im_conversation
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `target_id` bigint NOT NULL COMMENT '目标id（好友id或群id）',
  `conversation_type` tinyint(1) NOT NULL COMMENT '会话类型 1:私聊 2:群聊',
  `last_message_id` bigint NULL DEFAULT NULL COMMENT '最后一条消息id',
  `last_message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '最后一条消息内容',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int NULL DEFAULT 0 COMMENT '未读消息数',
  `is_top` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_mute` tinyint(1) NULL DEFAULT 0 COMMENT '是否免打扰',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_id` ASC, `conversation_type` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `last_message_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_conversation
-- ----------------------------

-- ----------------------------
-- Table structure for im_friend
-- ----------------------------
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `friend_id` bigint NOT NULL COMMENT '好友id',
  `friend_nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '好友昵称',
  `friend_head_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '好友头像',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_friend_id`(`friend_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '好友' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_friend
-- ----------------------------
INSERT INTO `im_friend` VALUES (1, 32, 33, '测试妈妈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom33&backgroundColor=f3e8ff', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (2, 32, 34, '测试儿子', 'https://api.dicebear.com/7.x/avataaars/svg?seed=son34&backgroundColor=dbeafe', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (3, 32, 35, '测试女儿', 'https://api.dicebear.com/7.x/avataaars/svg?seed=daughter35&backgroundColor=fce7f3', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (4, 33, 32, '测试爸爸', 'https://api.dicebear.com/7.x/bottts/svg?seed=dad32&backgroundColor=b6e3f4', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (5, 34, 32, '测试爸爸', 'https://api.dicebear.com/7.x/bottts/svg?seed=dad32&backgroundColor=b6e3f4', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (6, 35, 32, '测试爸爸', 'https://api.dicebear.com/7.x/bottts/svg?seed=dad32&backgroundColor=b6e3f4', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (7, 33, 34, '测试儿子', 'https://api.dicebear.com/7.x/avataaars/svg?seed=son34&backgroundColor=dbeafe', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (8, 33, 35, '测试女儿', 'https://api.dicebear.com/7.x/avataaars/svg?seed=daughter35&backgroundColor=fce7f3', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (9, 34, 33, '测试妈妈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom33&backgroundColor=f3e8ff', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (10, 35, 33, '测试妈妈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom33&backgroundColor=f3e8ff', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (11, 34, 35, '测试女儿', 'https://api.dicebear.com/7.x/avataaars/svg?seed=daughter35&backgroundColor=fce7f3', '2025-09-26 15:57:58');
INSERT INTO `im_friend` VALUES (12, 35, 34, '测试儿子', 'https://api.dicebear.com/7.x/avataaars/svg?seed=son34&backgroundColor=dbeafe', '2025-09-26 15:57:58');

-- ----------------------------
-- Table structure for im_group
-- ----------------------------
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群名字',
  `group_type` tinyint(1) NULL DEFAULT 1 COMMENT '群组类型 1:普通群 2:家庭群',
  `owner_id` bigint NOT NULL COMMENT '群主id',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID（仅家庭群有值）',
  `head_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '群头像',
  `head_image_thumb` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '群头像缩略图',
  `notice` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '群公告',
  `is_banned` tinyint(1) NULL DEFAULT 0 COMMENT '是否被封禁 0:否 1:是',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '被封禁原因',
  `dissolve` tinyint(1) NULL DEFAULT 0 COMMENT '是否已解散',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_family`(`family_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_group
-- ----------------------------
INSERT INTO `im_group` VALUES (1001, '测试家庭群', 2, 32, 1, 'https://api.dicebear.com/7.x/shapes/svg?seed=family1001', 'https://api.dicebear.com/7.x/shapes/svg?seed=family1001', '这是一个温馨的家庭群', 0, '', 0, '2025-09-26 15:57:58');

-- ----------------------------
-- Table structure for im_group_member
-- ----------------------------
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_id` bigint NOT NULL COMMENT '群id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `member_role` tinyint(1) NULL DEFAULT 1 COMMENT '成员角色 1:普通成员 2:AI机器人',
  `user_nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户昵称',
  `remark_nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '显示昵称备注',
  `head_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户头像',
  `remark_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '显示群名备注',
  `quit` tinyint(1) NULL DEFAULT 0 COMMENT '是否已退出',
  `quit_time` datetime NULL DEFAULT NULL COMMENT '退出时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群成员' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_group_member
-- ----------------------------
INSERT INTO `im_group_member` VALUES (9, 1001, 32, 1, '测试爸爸', '爸爸', 'https://api.dicebear.com/7.x/bottts/svg?seed=dad32&backgroundColor=b6e3f4', '我们家', 0, NULL, '2025-09-26 15:57:58');
INSERT INTO `im_group_member` VALUES (10, 1001, 33, 1, '测试妈妈', '妈妈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom33&backgroundColor=f3e8ff', '我们家', 0, NULL, '2025-09-26 15:57:58');
INSERT INTO `im_group_member` VALUES (11, 1001, 34, 1, '测试儿子', '儿子', 'https://api.dicebear.com/7.x/avataaars/svg?seed=son34&backgroundColor=dbeafe', '我们家', 0, NULL, '2025-09-26 15:57:58');
INSERT INTO `im_group_member` VALUES (12, 1001, 35, 1, '测试女儿', '女儿', 'https://api.dicebear.com/7.x/avataaars/svg?seed=daughter35&backgroundColor=fce7f3', '我们家', 0, NULL, '2025-09-26 15:57:58');
INSERT INTO `im_group_member` VALUES (13, 1001, -1, 2, '家庭AI助手', '', 'https://api.dicebear.com/7.x/bottts/svg?seed=aibot&backgroundColor=c7d2fe', '测试家庭群', 0, NULL, '2025-10-10 07:25:28');

-- ----------------------------
-- Table structure for im_group_message
-- ----------------------------
DROP TABLE IF EXISTS `im_group_message`;
CREATE TABLE `im_group_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_id` bigint NOT NULL COMMENT '群id',
  `send_id` bigint NOT NULL COMMENT '发送用户id',
  `send_nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '发送用户昵称',
  `send_head_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '发送者头像',
  `recv_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '接收用户id,逗号分隔，为空表示发给所有成员',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送内容',
  `at_user_ids` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '被@的用户id列表，逗号分隔',
  `receipt` tinyint NULL DEFAULT 0 COMMENT '是否回执消息',
  `receipt_ok` tinyint NULL DEFAULT 0 COMMENT '回执消息是否完成',
  `type` tinyint(1) NOT NULL COMMENT '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态 0:未发出 2:撤回',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_group_message
-- ----------------------------
INSERT INTO `im_group_message` VALUES (1, 1, 14, '张伟', '', '', '晚上我们一起吃饭吧', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (2, 1, 15, '李丽', '', '', '好的，我来做菜', '14', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (3, 1, 16, '小明', '', '', '我想吃红烧肉！', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (4, 1, 17, '小红', '', '', '我要吃糖醋排骨！', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (5, 1, 14, '张伟', '', '', '你们写完作业了吗？', '16,17', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (6, 1, 16, '小明', '', '', '我已经写完了', '14', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (7, 1, 17, '小红', '', '', '我还有一道数学题不会', '14,15', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (8, 1, 15, '李丽', '', '', '我来帮你', '17', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (9, 1, 14, '张伟', '', '', '周末我们去动物园吧', '', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (10, 1, 15, '李丽', '', '', '好主意', '', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (11, 1, 16, '小明', '', '', '太好了！', '', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (12, 1, 17, '小红', '', '', '我想看长颈鹿！', '', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (13, 1, 15, '李丽', '', '', 'https://example.com/photos/family_dinner.jpg', '', 0, 0, 1, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (14, 1, 16, '小明', '', '', '今天考试得了满分！', '', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (15, 1, 14, '张伟', '', '', '真棒！', '16', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (16, 1, 15, '李丽', '', '', '晚上奖励你吃冰淇淋', '16', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (17, 1, 17, '小红', '', '', '我也要吃！', '', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (18, 1, 15, '李丽', '', '', '你也要好好学习哦', '17', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (19, 1, 17, '小红', '', '', '好的妈妈', '15', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (20, 1, 14, '张伟', '', '', 'https://example.com/photos/zoo_visit.jpg', '', 0, 0, 1, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (21, 1, 14, '张伟', '', '', '动物园合影', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (22, 1, 14, '张伟', '', '', '今天下班早，我去接孩子们', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (23, 1, 15, '李丽', '', '', '太好了，那我可以加班了', '14', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (24, 1, 16, '小明', '', '', '爸爸，我们放学后想去书店', '14', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (25, 1, 14, '张伟', '', '', '没问题，等你们放学', '16,17', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (26, 1, 17, '小红', '', '', '谢谢爸爸！', '14', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (27, 1, 15, '李丽', '', '', '今天我做了红烧排骨', '', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (28, 1, 15, '李丽', '', '', 'https://example.com/photos/ribs.jpg', '', 0, 0, 1, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (29, 1, 16, '小明', '', '', '看起来好好吃！', '', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (30, 1, 17, '小红', '', '', '我最爱吃妈妈做的排骨了', '15', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (31, 1, 14, '张伟', '', '', '我马上回家', '', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (32, 1, 16, '小明', '', '', '老师布置了一个科学实验', '', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (33, 1, 16, '小明', '', '', '爸爸能帮我做吗？', '14', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (34, 1, 14, '张伟', '', '', '当然可以，是什么实验？', '16', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (35, 1, 16, '小明', '', '', '火山喷发实验', '14', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (36, 1, 14, '张伟', '', '', '好的，周末我们一起做', '16', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (37, 1, 15, '李丽', '', '', '小红明天要参加舞蹈比赛', '', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (38, 1, 17, '小红', '', '', '我有点紧张', '', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (39, 1, 14, '张伟', '', '', '不用紧张，你跳得很好', '17', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (40, 1, 15, '李丽', '', '', '我们会去给你加油的', '17', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (41, 1, 17, '小红', '', '', '谢谢爸爸妈妈', '14,15', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (42, 1, 16, '小明', '', '', '我也去为妹妹加油', '17', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (43, 1, 17, '小红', '', '', 'https://example.com/photos/dance_rehearsal.jpg', '', 0, 0, 1, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (44, 1, 17, '小红', '', '', '这是排练照片', '', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (45, 1, 15, '李丽', '', '', '真漂亮！', '17', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (46, 1, 14, '张伟', '', '', '这周末我们去爷爷家吗？', '', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (47, 1, 15, '李丽', '', '', '好的，我来准备礼物', '14', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (48, 1, 16, '小明', '', '', '我想带我的新玩具给表弟看', '', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (49, 1, 17, '小红', '', '', '我要带舞蹈比赛的奖杯给爷爷看', '', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (50, 1, 17, '小红', '', '', '我得了第一名！', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (51, 1, 17, '小红', '', '', 'https://example.com/photos/dance_trophy.jpg', '', 0, 0, 1, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (52, 1, 14, '张伟', '', '', '太棒了！我的小公主', '17', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (53, 1, 15, '李丽', '', '', '我们为你骄傲！', '17', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (54, 1, 16, '小明', '', '', '妹妹真厉害！', '17', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (55, 2, 19, '刘强', '', '', '这周末我们去农家乐怎么样？', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (56, 2, 18, '王芳', '', '', '好啊，可以采摘水果', '19', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (57, 2, 20, '郑涛', '', '', '太好了！我想去钓鱼', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (58, 2, 21, '陈雪', '', '', '我想摘草莓', '', 0, 0, 0, 0, '2025-07-21 10:56:48');
INSERT INTO `im_group_message` VALUES (59, 2, 19, '刘强', '', '', '周六出发，我订好了农家院', '', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (60, 2, 18, '王芳', '', '', '我来准备一些零食和水果', '', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (61, 2, 18, '王芳', '', '', '陈雪，记得带上防晒霜', '21', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (62, 2, 21, '陈雪', '', '', '知道了，妈妈', '18', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (63, 2, 20, '郑涛', '', '', '爸爸，我们带上钓鱼竿吗？', '19', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (64, 2, 19, '刘强', '', '', '对，我已经准备好了', '20', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (65, 2, 20, '郑涛', '', '', 'https://example.com/photos/fishing_rod.jpg', '', 0, 0, 1, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (66, 2, 20, '郑涛', '', '', '我自己的钓鱼竿', '', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (67, 2, 19, '刘强', '', '', '真不错', '20', 0, 0, 0, 0, '2025-07-22 10:56:48');
INSERT INTO `im_group_message` VALUES (68, 2, 18, '王芳', '', '', '郑涛，你明天有数学测验，记得复习', '20', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (69, 2, 20, '郑涛', '', '', '知道了，妈妈', '18', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (70, 2, 21, '陈雪', '', '', '妈妈，我的舞蹈服要洗', '18', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (71, 2, 18, '王芳', '', '', '好的，晚上我来洗', '21', 0, 0, 0, 0, '2025-07-23 10:56:48');
INSERT INTO `im_group_message` VALUES (72, 2, 19, '刘强', '', '', '今天公司加班，我会晚点回家', '', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (73, 2, 18, '王芳', '', '', '好的，我来接孩子们', '19', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (74, 2, 21, '陈雪', '', '', '妈妈，今天舞蹈课提前结束', '18', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (75, 2, 18, '王芳', '', '', '好的，我会准时到的', '21', 0, 0, 0, 0, '2025-07-24 10:56:48');
INSERT INTO `im_group_message` VALUES (76, 2, 20, '郑涛', '', '', '我数学测验得了A！', '', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (77, 2, 19, '刘强', '', '', '真棒，儿子！', '20', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (78, 2, 18, '王芳', '', '', '太好了，今晚吃你最爱的鱼香肉丝', '20', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (79, 2, 21, '陈雪', '', '', '哥哥真厉害', '20', 0, 0, 0, 0, '2025-07-25 10:56:48');
INSERT INTO `im_group_message` VALUES (80, 2, 19, '刘强', '', '', 'https://example.com/photos/farm_trip.jpg', '', 0, 0, 1, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (81, 2, 19, '刘强', '', '', '农家乐照片', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (82, 2, 18, '王芳', '', '', '孩子们玩得真开心', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (83, 2, 20, '郑涛', '', '', '我钓到了三条鱼！', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (84, 2, 21, '陈雪', '', '', '我摘了好多草莓', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (85, 2, 21, '陈雪', '', '', 'https://example.com/photos/strawberries.jpg', '', 0, 0, 1, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (86, 2, 18, '王芳', '', '', '周末真愉快', '', 0, 0, 0, 0, '2025-07-26 10:56:48');
INSERT INTO `im_group_message` VALUES (87, 1, 14, '张伟', '', '', '我下周要出差三天', '', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (88, 1, 15, '李丽', '', '', '需要我准备什么吗？', '14', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (89, 1, 14, '张伟', '', '', '不用，公司都安排好了', '15', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (90, 1, 16, '小明', '', '', '爸爸去哪里出差？', '14', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (91, 1, 14, '张伟', '', '', '去上海，给你带礼物回来', '16', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (92, 1, 16, '小明', '', '', '太好了！谢谢爸爸', '14', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (93, 1, 17, '小红', '', '', '我也要礼物！', '14', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (94, 1, 14, '张伟', '', '', '当然有你的份，小公主', '17', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (95, 1, 15, '李丽', '', '', '孩子们，记得复习功课', '16,17', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (96, 1, 16, '小明', '', '', '知道了，妈妈', '15', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (97, 1, 17, '小红', '', '', '我已经做完作业了', '15', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (98, 1, 15, '李丽', '', '', '真乖', '', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (99, 1, 14, '张伟', '', '', '周末天气不错，去公园玩吗？', '', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (100, 1, 16, '小明', '', '', '好啊！可以带足球吗？', '14', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (101, 1, 14, '张伟', '', '', '没问题，我们一起踢', '16', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (102, 1, 17, '小红', '', '', '我想喂鸽子', '', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (103, 1, 15, '李丽', '', '', '我带面包去', '17', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (104, 2, 19, '刘强', '', '', '我们家的空调需要修理了', '', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (105, 2, 18, '王芳', '', '', '我来联系维修师傅', '19', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (106, 2, 19, '刘强', '', '', '谢谢，亲爱的', '18', 0, 0, 0, 0, '2025-07-14 10:56:48');
INSERT INTO `im_group_message` VALUES (107, 2, 20, '郑涛', '', '', '爸爸，我的自行车链条松了', '19', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (108, 2, 19, '刘强', '', '', '晚上我帮你修', '20', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (109, 2, 20, '郑涛', '', '', '谢谢爸爸', '19', 0, 0, 0, 0, '2025-07-15 10:56:48');
INSERT INTO `im_group_message` VALUES (110, 2, 21, '陈雪', '', '', '妈妈，我的舞蹈服洗好了吗？', '18', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (111, 2, 18, '王芳', '', '', '已经洗好晾着了', '21', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (112, 2, 21, '陈雪', '', '', '谢谢妈妈', '18', 0, 0, 0, 0, '2025-07-16 10:56:48');
INSERT INTO `im_group_message` VALUES (113, 2, 19, '刘强', '', '', '周末天气好，去钓鱼吧', '', 0, 0, 0, 0, '2025-07-17 10:56:48');
INSERT INTO `im_group_message` VALUES (114, 2, 20, '郑涛', '', '', '太好了！我要去', '19', 0, 0, 0, 0, '2025-07-17 10:56:48');
INSERT INTO `im_group_message` VALUES (115, 2, 18, '王芳', '', '', '我和陈雪准备野餐', '', 0, 0, 0, 0, '2025-07-17 10:56:48');
INSERT INTO `im_group_message` VALUES (116, 2, 21, '陈雪', '', '', '我要帮妈妈做饭', '18', 0, 0, 0, 0, '2025-07-17 10:56:48');
INSERT INTO `im_group_message` VALUES (117, 2, 18, '王芳', '', '', '真乖', '21', 0, 0, 0, 0, '2025-07-17 10:56:48');
INSERT INTO `im_group_message` VALUES (118, 1001, 32, '测试爸爸', '', '', '大家晚上好！', '', 0, 0, 0, 0, '2025-09-26 11:57:58');
INSERT INTO `im_group_message` VALUES (119, 1001, 33, '测试妈妈', '', '', '晚上好亲爱的！', '32', 0, 0, 0, 0, '2025-09-26 11:57:58');
INSERT INTO `im_group_message` VALUES (120, 1001, 34, '测试儿子', '', '', '爸爸妈妈晚上好！', '', 0, 0, 0, 0, '2025-09-26 11:57:58');
INSERT INTO `im_group_message` VALUES (121, 1001, 35, '测试女儿', '', '', '全家晚上好～', '', 0, 0, 0, 0, '2025-09-26 11:57:58');
INSERT INTO `im_group_message` VALUES (122, 1001, 32, '测试爸爸', '', '', '今天我们一起看电影吧！', '', 0, 0, 0, 0, '2025-09-26 12:57:58');
INSERT INTO `im_group_message` VALUES (123, 1001, 34, '测试儿子', '', '', '好的爸爸！我想看动画片', '32', 0, 0, 0, 0, '2025-09-26 12:57:58');
INSERT INTO `im_group_message` VALUES (124, 1001, 35, '测试女儿', '', '', '我也想看！', '', 0, 0, 0, 0, '2025-09-26 12:57:58');
INSERT INTO `im_group_message` VALUES (125, 1001, 33, '测试妈妈', '', '', '那我准备一些零食', '', 0, 0, 0, 0, '2025-09-26 13:57:58');
INSERT INTO `im_group_message` VALUES (126, 1001, 32, '测试爸爸', '', '', '周末我们去公园玩吧', '', 0, 0, 0, 0, '2025-09-26 14:57:58');
INSERT INTO `im_group_message` VALUES (127, 1001, 34, '测试儿子', '', '', '太好了！', '', 0, 0, 0, 0, '2025-09-26 14:57:58');
INSERT INTO `im_group_message` VALUES (128, 1001, 35, '测试女儿', '', '', '我要带我的画笔！', '', 0, 0, 0, 0, '2025-09-26 14:57:58');
INSERT INTO `im_group_message` VALUES (129, 1001, 32, '黄暄', '', '', '很好', NULL, 0, 0, 0, 0, '2025-09-27 19:48:05');
INSERT INTO `im_group_message` VALUES (130, 1001, 32, '黄暄', '', '', '嘻嘻', NULL, 0, 0, 0, 0, '2025-09-27 19:48:12');

-- ----------------------------
-- Table structure for im_group_message_read
-- ----------------------------
DROP TABLE IF EXISTS `im_group_message_read`;
CREATE TABLE `im_group_message_read`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_id` bigint NOT NULL COMMENT '群id',
  `message_id` bigint NOT NULL COMMENT '消息id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_message`(`group_id` ASC, `message_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '群消息已读记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_group_message_read
-- ----------------------------

-- ----------------------------
-- Table structure for im_message_sequence
-- ----------------------------
DROP TABLE IF EXISTS `im_message_sequence`;
CREATE TABLE `im_message_sequence`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `sequence` bigint NOT NULL DEFAULT 0 COMMENT '消息序列号',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息序列号' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_message_sequence
-- ----------------------------

-- ----------------------------
-- Table structure for im_private_message
-- ----------------------------
DROP TABLE IF EXISTS `im_private_message`;
CREATE TABLE `im_private_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `send_id` bigint NOT NULL COMMENT '发送用户id',
  `recv_id` bigint NOT NULL COMMENT '接收用户id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送内容',
  `type` tinyint(1) NOT NULL COMMENT '消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示',
  `status` tinyint(1) NOT NULL COMMENT '状态 0:未读 1:已读 2:撤回 3:已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '已读时间',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_send_id`(`send_id` ASC) USING BTREE,
  INDEX `idx_recv_id`(`recv_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 225 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私聊消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_private_message
-- ----------------------------
INSERT INTO `im_private_message` VALUES (213, 32, 34, '儿子，今天学习怎么样？', 0, 1, NULL, '2025-09-25 15:57:58');
INSERT INTO `im_private_message` VALUES (214, 34, 32, '爸爸，今天学了很多新知识！', 0, 1, NULL, '2025-09-25 15:57:58');
INSERT INTO `im_private_message` VALUES (215, 32, 34, '很好，继续努力！', 0, 1, NULL, '2025-09-25 15:57:58');
INSERT INTO `im_private_message` VALUES (216, 32, 35, '女儿，今天开心吗？', 0, 1, NULL, '2025-09-26 13:57:58');
INSERT INTO `im_private_message` VALUES (217, 35, 32, '爸爸，很开心！今天画了一幅画', 0, 1, NULL, '2025-09-26 13:57:58');
INSERT INTO `im_private_message` VALUES (218, 32, 35, '真棒！爸爸很期待看到你的画', 0, 1, NULL, '2025-09-26 14:57:58');
INSERT INTO `im_private_message` VALUES (219, 32, 33, '今天孩子们表现都很好', 0, 1, NULL, '2025-09-26 12:57:58');
INSERT INTO `im_private_message` VALUES (220, 33, 32, '是的，他们都很乖', 0, 1, NULL, '2025-09-26 12:57:58');
INSERT INTO `im_private_message` VALUES (221, 33, 34, '儿子，记得按时吃饭', 0, 1, NULL, '2025-09-26 11:57:58');
INSERT INTO `im_private_message` VALUES (222, 34, 33, '知道了妈妈', 0, 1, NULL, '2025-09-26 11:57:58');
INSERT INTO `im_private_message` VALUES (223, 33, 35, '女儿，今天舞蹈课怎么样？', 0, 1, NULL, '2025-09-26 10:57:58');
INSERT INTO `im_private_message` VALUES (224, 35, 33, '很好呀妈妈，老师夸我了', 0, 1, NULL, '2025-09-26 10:57:58');

-- ----------------------------
-- Table structure for im_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `content` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '敏感词内容',
  `enabled` tinyint NULL DEFAULT 1 COMMENT '是否启用 0:未启用 1:启用',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enabled`(`enabled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '敏感词' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_sensitive_word
-- ----------------------------
INSERT INTO `im_sensitive_word` VALUES (1, 'fuck', 1, NULL, '2025-10-10 05:40:23');
INSERT INTO `im_sensitive_word` VALUES (2, 'shit', 1, NULL, '2025-10-10 05:40:23');
INSERT INTO `im_sensitive_word` VALUES (3, '傻逼', 1, NULL, '2025-10-10 05:40:23');
INSERT INTO `im_sensitive_word` VALUES (4, '操', 1, NULL, '2025-10-10 05:40:23');
INSERT INTO `im_sensitive_word` VALUES (5, '妈的', 1, NULL, '2025-10-10 05:40:23');

-- ----------------------------
-- Table structure for like_record
-- ----------------------------
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `type` tinyint(1) NOT NULL COMMENT '类型 1:内容 2:评论',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_target`(`user_id` ASC, `target_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of like_record
-- ----------------------------
INSERT INTO `like_record` VALUES (1, 100, 12, 1, '2025-05-10 22:40:59');
INSERT INTO `like_record` VALUES (10, 2, 8, 2, '2025-05-11 21:17:58');
INSERT INTO `like_record` VALUES (11, 2, 6, 2, '2025-05-11 21:18:00');
INSERT INTO `like_record` VALUES (12, 2, 7, 2, '2025-05-11 21:18:05');
INSERT INTO `like_record` VALUES (14, 2, 9, 2, '2025-05-11 21:18:41');
INSERT INTO `like_record` VALUES (15, 2, 11, 2, '2025-05-11 21:21:50');
INSERT INTO `like_record` VALUES (16, 2, 13, 2, '2025-05-24 22:30:27');
INSERT INTO `like_record` VALUES (18, 3, 12, 1, '2025-05-24 22:34:24');
INSERT INTO `like_record` VALUES (19, 3, 16, 2, '2025-05-24 22:36:39');
INSERT INTO `like_record` VALUES (20, 2, 13, 1, '2025-05-25 10:04:22');
INSERT INTO `like_record` VALUES (21, 2, 24, 2, '2025-05-25 10:52:41');
INSERT INTO `like_record` VALUES (22, 2, 19, 2, '2025-05-25 10:52:54');
INSERT INTO `like_record` VALUES (23, 2, 25, 2, '2025-05-25 10:53:12');
INSERT INTO `like_record` VALUES (24, 2, 23, 2, '2025-05-25 12:57:37');
INSERT INTO `like_record` VALUES (25, 3, 30, 2, '2025-05-25 20:55:25');
INSERT INTO `like_record` VALUES (27, 3, 13, 1, '2025-05-25 20:55:33');
INSERT INTO `like_record` VALUES (32, 32, 39, 1, '2025-09-27 15:20:53');
INSERT INTO `like_record` VALUES (35, 32, 35, 2, '2025-09-27 15:21:32');
INSERT INTO `like_record` VALUES (36, 32, 34, 2, '2025-09-27 15:21:33');
INSERT INTO `like_record` VALUES (37, 32, 48, 1, '2025-09-27 15:22:03');
INSERT INTO `like_record` VALUES (38, 32, 38, 2, '2025-09-27 15:22:12');
INSERT INTO `like_record` VALUES (39, 32, 46, 2, '2025-09-27 16:43:11');
INSERT INTO `like_record` VALUES (40, 32, 41, 2, '2025-09-27 16:43:16');
INSERT INTO `like_record` VALUES (41, 32, 39, 2, '2025-09-27 16:45:35');
INSERT INTO `like_record` VALUES (44, 32, 53, 2, '2025-10-04 14:02:02');
INSERT INTO `like_record` VALUES (46, 32, 66, 1, '2025-10-04 14:22:47');
INSERT INTO `like_record` VALUES (49, 32, 56, 2, '2025-10-04 14:46:33');
INSERT INTO `like_record` VALUES (50, 32, 72, 1, '2025-10-04 16:03:44');
INSERT INTO `like_record` VALUES (53, 32, 67, 1, '2025-10-05 04:27:32');
INSERT INTO `like_record` VALUES (54, 32, 71, 1, '2025-10-05 06:09:45');
INSERT INTO `like_record` VALUES (55, 32, 65, 1, '2025-10-05 06:19:30');
INSERT INTO `like_record` VALUES (56, 32, 61, 1, '2025-10-05 08:02:16');

-- ----------------------------
-- Table structure for medal
-- ----------------------------
DROP TABLE IF EXISTS `medal`;
CREATE TABLE `medal`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '勋章名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '勋章描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '勋章图标URL',
  `condition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '获得条件',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '勋章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of medal
-- ----------------------------

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` tinyint(1) NOT NULL COMMENT '类型 1:点赞 2:评论 3:回复 4:系统',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `from_user_id` bigint NULL DEFAULT NULL COMMENT '来源用户ID',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读 0:未读 1:已读',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification
-- ----------------------------

-- ----------------------------
-- Table structure for paragraph_comment_count
-- ----------------------------
DROP TABLE IF EXISTS `paragraph_comment_count`;
CREATE TABLE `paragraph_comment_count`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容 ID',
  `paragraph_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '段落标识符',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_content_para`(`content_id` ASC, `paragraph_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '段落评论计数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of paragraph_comment_count
-- ----------------------------
INSERT INTO `paragraph_comment_count` VALUES (1, 12, 'para-1746364185583-0', 4, '2025-05-11 20:52:23', '2025-06-12 22:18:59');
INSERT INTO `paragraph_comment_count` VALUES (2, 12, 'para-1746364185583-24', 2, '2025-05-11 21:20:30', '2025-05-11 21:21:55');
INSERT INTO `paragraph_comment_count` VALUES (3, 12, 'para-1746364185583-14', 2, '2025-05-24 22:30:04', '2025-06-12 22:23:25');
INSERT INTO `paragraph_comment_count` VALUES (4, 13, 'para-1748138393295-0', 6, '2025-05-25 10:03:57', '2025-05-25 21:08:29');
INSERT INTO `paragraph_comment_count` VALUES (10, 39, 'para-1758687682842-0', 9, '2025-09-24 13:47:30', '2025-09-27 16:43:09');
INSERT INTO `paragraph_comment_count` VALUES (11, 39, 'para-1758687682842-2', 1, '2025-09-24 14:03:05', '2025-09-24 14:03:05');
INSERT INTO `paragraph_comment_count` VALUES (12, 69, 'para-1759235299705-0', 1, '2025-10-04 15:51:10', '2025-10-04 15:51:10');
INSERT INTO `paragraph_comment_count` VALUES (13, 71, 'para-1759588190397-2', 1, '2025-10-05 06:06:38', '2025-10-05 06:06:38');
INSERT INTO `paragraph_comment_count` VALUES (14, 71, 'para-1759588190397-10', 2, '2025-10-05 13:08:59', '2025-10-05 13:09:05');

-- ----------------------------
-- Table structure for recommendation_log
-- ----------------------------
DROP TABLE IF EXISTS `recommendation_log`;
CREATE TABLE `recommendation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '推荐策略：cf,content,hot,mixed',
  `score` decimal(6, 4) NULL DEFAULT NULL COMMENT '推荐分数',
  `position` int NOT NULL COMMENT '推荐位置',
  `is_clicked` tinyint(1) NULL DEFAULT 0 COMMENT '是否点击',
  `is_finished` tinyint(1) NULL DEFAULT 0 COMMENT '是否完整消费',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_strategy`(`strategy` ASC, `created_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 544 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推荐记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of recommendation_log
-- ----------------------------
INSERT INTO `recommendation_log` VALUES (271, 9, 17, 'mixed', 1.0000, 1, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (272, 9, 18, 'mixed', 1.0000, 2, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (273, 9, 20, 'mixed', 1.0000, 3, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (274, 9, 19, 'mixed', 1.0000, 4, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (275, 9, 12, 'mixed', 1.0000, 5, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (276, 9, 13, 'mixed', 1.0000, 6, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (277, 9, 14, 'mixed', 1.0000, 7, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (278, 9, 7, 'mixed', 1.0000, 8, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (279, 9, 9, 'mixed', 1.0000, 9, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (280, 9, 11, 'mixed', 1.0000, 10, 0, 0, '2025-07-23 16:44:40');
INSERT INTO `recommendation_log` VALUES (281, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (282, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (283, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (284, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (285, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (286, 9, 7, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (287, 9, 11, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 16:44:42');
INSERT INTO `recommendation_log` VALUES (288, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 16:44:45');
INSERT INTO `recommendation_log` VALUES (289, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 16:44:45');
INSERT INTO `recommendation_log` VALUES (290, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 16:44:45');
INSERT INTO `recommendation_log` VALUES (299, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (300, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (301, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (302, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (303, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (304, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (305, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (306, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (307, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (308, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 16:45:09');
INSERT INTO `recommendation_log` VALUES (309, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (310, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (311, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (312, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (313, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (314, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (315, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (316, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (317, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (318, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 16:45:12');
INSERT INTO `recommendation_log` VALUES (319, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 16:45:15');
INSERT INTO `recommendation_log` VALUES (320, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 16:45:15');
INSERT INTO `recommendation_log` VALUES (321, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 16:45:15');
INSERT INTO `recommendation_log` VALUES (322, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 16:45:15');
INSERT INTO `recommendation_log` VALUES (323, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 16:45:15');
INSERT INTO `recommendation_log` VALUES (324, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 16:45:17');
INSERT INTO `recommendation_log` VALUES (325, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 16:45:17');
INSERT INTO `recommendation_log` VALUES (326, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 16:45:17');
INSERT INTO `recommendation_log` VALUES (327, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (328, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (329, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (330, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (331, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (332, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (333, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (334, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (335, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (336, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 19:54:49');
INSERT INTO `recommendation_log` VALUES (337, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (338, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (339, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (340, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (341, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (342, 9, 7, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (343, 9, 11, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 19:54:52');
INSERT INTO `recommendation_log` VALUES (344, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:54:55');
INSERT INTO `recommendation_log` VALUES (345, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:54:55');
INSERT INTO `recommendation_log` VALUES (346, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:54:55');
INSERT INTO `recommendation_log` VALUES (355, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (356, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (357, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (358, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (359, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (360, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (361, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (362, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (363, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (364, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 19:55:19');
INSERT INTO `recommendation_log` VALUES (365, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (366, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (367, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (368, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (369, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (370, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (371, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (372, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (373, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (374, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 19:55:22');
INSERT INTO `recommendation_log` VALUES (375, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:55:25');
INSERT INTO `recommendation_log` VALUES (376, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:55:25');
INSERT INTO `recommendation_log` VALUES (377, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:55:25');
INSERT INTO `recommendation_log` VALUES (378, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 19:55:25');
INSERT INTO `recommendation_log` VALUES (379, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 19:55:25');
INSERT INTO `recommendation_log` VALUES (380, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 19:55:27');
INSERT INTO `recommendation_log` VALUES (381, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 19:55:27');
INSERT INTO `recommendation_log` VALUES (382, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 19:55:27');
INSERT INTO `recommendation_log` VALUES (383, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (384, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (385, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (386, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (387, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (388, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (389, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (390, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (391, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (392, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 20:02:35');
INSERT INTO `recommendation_log` VALUES (393, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (394, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (395, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (396, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (397, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (398, 9, 7, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (399, 9, 11, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:02:38');
INSERT INTO `recommendation_log` VALUES (400, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:02:41');
INSERT INTO `recommendation_log` VALUES (401, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:02:41');
INSERT INTO `recommendation_log` VALUES (402, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:02:41');
INSERT INTO `recommendation_log` VALUES (411, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (412, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (413, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (414, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (415, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (416, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (417, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (418, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (419, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (420, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 20:03:05');
INSERT INTO `recommendation_log` VALUES (421, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (422, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (423, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (424, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (425, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (426, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (427, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (428, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (429, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (430, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 20:03:08');
INSERT INTO `recommendation_log` VALUES (431, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:03:11');
INSERT INTO `recommendation_log` VALUES (432, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:03:11');
INSERT INTO `recommendation_log` VALUES (433, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:03:11');
INSERT INTO `recommendation_log` VALUES (434, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:03:11');
INSERT INTO `recommendation_log` VALUES (435, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:03:11');
INSERT INTO `recommendation_log` VALUES (436, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:03:13');
INSERT INTO `recommendation_log` VALUES (437, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:03:13');
INSERT INTO `recommendation_log` VALUES (438, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:03:13');
INSERT INTO `recommendation_log` VALUES (439, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (440, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (441, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (442, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (443, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (444, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (445, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (446, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (447, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (448, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 20:15:02');
INSERT INTO `recommendation_log` VALUES (449, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (450, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (451, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (452, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (453, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (454, 9, 7, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (455, 9, 11, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:15:05');
INSERT INTO `recommendation_log` VALUES (456, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:08');
INSERT INTO `recommendation_log` VALUES (457, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:08');
INSERT INTO `recommendation_log` VALUES (458, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:08');
INSERT INTO `recommendation_log` VALUES (467, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (468, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (469, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (470, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (471, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (472, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (473, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (474, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (475, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (476, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 20:15:31');
INSERT INTO `recommendation_log` VALUES (477, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (478, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (479, 9, 20, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (480, 9, 19, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (481, 9, 12, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (482, 9, 13, 'mixed_recent_repeat', 1.0000, 6, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (483, 9, 14, 'mixed_recent_repeat', 1.0000, 7, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (484, 9, 7, 'mixed_recent_repeat', 1.0000, 8, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (485, 9, 9, 'mixed_recent_repeat', 1.0000, 9, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (486, 9, 11, 'mixed_recent_repeat', 1.0000, 10, 0, 0, '2025-07-23 20:15:35');
INSERT INTO `recommendation_log` VALUES (487, 9, 17, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:37');
INSERT INTO `recommendation_log` VALUES (488, 9, 18, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:37');
INSERT INTO `recommendation_log` VALUES (489, 9, 19, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:37');
INSERT INTO `recommendation_log` VALUES (490, 9, 12, 'mixed_recent_repeat', 1.0000, 4, 0, 0, '2025-07-23 20:15:37');
INSERT INTO `recommendation_log` VALUES (491, 9, 14, 'mixed_recent_repeat', 1.0000, 5, 0, 0, '2025-07-23 20:15:37');
INSERT INTO `recommendation_log` VALUES (492, 9, 9, 'mixed_recent_repeat', 1.0000, 1, 0, 0, '2025-07-23 20:15:39');
INSERT INTO `recommendation_log` VALUES (493, 9, 20, 'mixed_recent_repeat', 1.0000, 2, 0, 0, '2025-07-23 20:15:39');
INSERT INTO `recommendation_log` VALUES (494, 9, 13, 'mixed_recent_repeat', 1.0000, 3, 0, 0, '2025-07-23 20:15:39');
INSERT INTO `recommendation_log` VALUES (495, 32, 64, 'mixed', 0.8000, 1, 0, 0, '2025-10-07 13:50:07');
INSERT INTO `recommendation_log` VALUES (496, 32, 62, 'mixed', 0.8000, 2, 0, 0, '2025-10-07 13:50:07');
INSERT INTO `recommendation_log` VALUES (497, 32, 63, 'mixed', 0.8000, 3, 0, 0, '2025-10-07 13:50:07');
INSERT INTO `recommendation_log` VALUES (498, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:50:22');
INSERT INTO `recommendation_log` VALUES (499, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:50:22');
INSERT INTO `recommendation_log` VALUES (500, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:50:22');
INSERT INTO `recommendation_log` VALUES (501, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:50:24');
INSERT INTO `recommendation_log` VALUES (502, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:50:24');
INSERT INTO `recommendation_log` VALUES (503, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:50:24');
INSERT INTO `recommendation_log` VALUES (504, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:50:26');
INSERT INTO `recommendation_log` VALUES (505, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:50:26');
INSERT INTO `recommendation_log` VALUES (506, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:50:26');
INSERT INTO `recommendation_log` VALUES (507, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:50:50');
INSERT INTO `recommendation_log` VALUES (508, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:50:50');
INSERT INTO `recommendation_log` VALUES (509, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:50:50');
INSERT INTO `recommendation_log` VALUES (510, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:51:05');
INSERT INTO `recommendation_log` VALUES (511, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:51:05');
INSERT INTO `recommendation_log` VALUES (512, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:51:05');
INSERT INTO `recommendation_log` VALUES (513, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:51:38');
INSERT INTO `recommendation_log` VALUES (514, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:51:38');
INSERT INTO `recommendation_log` VALUES (515, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:51:38');
INSERT INTO `recommendation_log` VALUES (516, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:53:32');
INSERT INTO `recommendation_log` VALUES (517, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:53:32');
INSERT INTO `recommendation_log` VALUES (518, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:53:32');
INSERT INTO `recommendation_log` VALUES (519, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:53:45');
INSERT INTO `recommendation_log` VALUES (520, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:53:45');
INSERT INTO `recommendation_log` VALUES (521, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:53:45');
INSERT INTO `recommendation_log` VALUES (522, 32, 64, 'mixed_recent_repeat', 0.8000, 1, 0, 0, '2025-10-07 13:57:48');
INSERT INTO `recommendation_log` VALUES (523, 32, 62, 'mixed_recent_repeat', 0.8000, 2, 0, 0, '2025-10-07 13:57:48');
INSERT INTO `recommendation_log` VALUES (524, 32, 63, 'mixed_recent_repeat', 0.8000, 3, 0, 0, '2025-10-07 13:57:48');
INSERT INTO `recommendation_log` VALUES (525, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 14:02:43');
INSERT INTO `recommendation_log` VALUES (526, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 14:02:49');
INSERT INTO `recommendation_log` VALUES (527, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 14:05:23');
INSERT INTO `recommendation_log` VALUES (528, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 14:06:28');
INSERT INTO `recommendation_log` VALUES (529, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 14:07:43');
INSERT INTO `recommendation_log` VALUES (530, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 14:12:37');
INSERT INTO `recommendation_log` VALUES (531, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 15:54:23');
INSERT INTO `recommendation_log` VALUES (532, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 15:54:26');
INSERT INTO `recommendation_log` VALUES (533, 32, 63, 'content_based_recent_repeat', 0.5000, 1, 0, 0, '2025-10-07 15:54:28');
INSERT INTO `recommendation_log` VALUES (534, 9, 71, 'hot', 9.0000, 1, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (535, 9, 70, 'hot', 6.2000, 2, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (536, 9, 68, 'hot', 4.3000, 3, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (537, 9, 64, 'hot', 2.2000, 4, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (538, 9, 69, 'hot', 2.1000, 5, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (539, 9, 72, 'hot', 1.9000, 6, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (540, 9, 66, 'hot', 1.7000, 7, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (541, 9, 61, 'hot', 1.3000, 8, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (542, 9, 65, 'hot', 1.3000, 9, 0, 0, '2025-10-07 16:00:31');
INSERT INTO `recommendation_log` VALUES (543, 9, 67, 'hot', 1.3000, 10, 0, 0, '2025-10-07 16:00:31');

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '所属分类',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, '升学', NULL, 0, 1, '2025-06-28 19:52:10');
INSERT INTO `tag` VALUES (2, '教育', NULL, 0, 1, '2025-06-28 19:52:10');
INSERT INTO `tag` VALUES (3, '学习', NULL, 0, 1, '2025-06-28 19:52:10');
INSERT INTO `tag` VALUES (4, '考试', NULL, 0, 1, '2025-06-28 19:52:10');
INSERT INTO `tag` VALUES (5, '在线教育', NULL, 0, 1, '2025-06-28 19:52:10');
INSERT INTO `tag` VALUES (6, '职业发展', NULL, 0, 1, '2025-06-28 19:52:10');
INSERT INTO `tag` VALUES (7, '技能培训', NULL, 0, 1, '2025-06-28 19:52:10');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像URL',
  `avatar_thumb` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像缩略图URL',
  `sex` tinyint(1) NULL DEFAULT 0 COMMENT '性别 0:男 1:女',
  `user_type` tinyint(1) NULL DEFAULT 1 COMMENT '用户类型 1:普通用户 2:管理员',
  `role` tinyint(1) NULL DEFAULT 1 COMMENT '角色 1:家长 2:孩子 3:AI机器人',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `interests` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '兴趣爱好，逗号分隔',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
  `wechat_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `im_server_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户连接的IM服务器ID',
  `online_status` tinyint(1) NULL DEFAULT 0 COMMENT '在线状态 0:离线 1:在线',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后在线时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `idx_wechat`(`wechat_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (-999, 'test_admin', 'testadmin@example.com', '13800138999', '$2a$10$1qAz2wSx3eDc4rFv5tGb5eTrKKqYDBOzh8nPL.jvkkV3d3lAk5rse', '测试管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin999&backgroundColor=e0e7ff', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin999&backgroundColor=e0e7ff', 0, 2, 1, '1985-01-01', '阅读,教育,科技', 1, 'wx_test_admin', NULL, 0, NULL, '2025-09-27 00:00:00', '2025-09-27 00:00:00');
INSERT INTO `user` VALUES (-1, 'ai_family_bot', 'aibot@system.local', NULL, '$2a$10$disabled', '家庭AI助手', 'https://api.dicebear.com/7.x/bottts/svg?seed=aibot&backgroundColor=c7d2fe', 'https://api.dicebear.com/7.x/bottts/svg?seed=aibot&backgroundColor=c7d2fe', 0, 1, 3, NULL, NULL, 1, NULL, NULL, 1, NULL, '2025-10-10 07:25:28', '2025-10-10 07:25:28');
INSERT INTO `user` VALUES (32, 'user_6295', '3441578327@qq.com', '18852122635', '$2a$10$XDWcd/TOqQa.4pfpDJMdOO0fXjKU/Im1CYCJA/QZl3A2HPqvZyQsy', '黄暄', 'http://127.0.0.1:9000/qz-sns/image/register/avatar/a3fb0b2d-9bcc-4bb4-b5c1-ef33b38620df.jpg', '', 0, 1, 1, NULL, '文学,科普,历史', 1, NULL, NULL, 0, NULL, '2025-09-22 17:40:09', '2025-10-05 18:42:19');
INSERT INTO `user` VALUES (33, 'test_mom33', 'testmom33@example.com', '13800138033', '$2a$10$1qAz2wSx3eDc4rFv5tGb5eTrKKqYDBOzh8nPL.jvkkV3d3lAk5rse', '测试妈妈', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom33&backgroundColor=f3e8ff', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mom33&backgroundColor=f3e8ff', 1, 1, 1, '1987-08-20', '音乐,烹饪,园艺', 1, 'wx_test_mom33', NULL, 0, NULL, '2025-09-26 15:55:00', '2025-09-26 15:55:00');
INSERT INTO `user` VALUES (34, 'test_son34', 'testson34@example.com', '13800138034', '$2a$10$1qAz2wSx3eDc4rFv5tGb5eTrKKqYDBOzh8nPL.jvkkV3d3lAk5rse', '测试儿子', 'https://api.dicebear.com/7.x/avataaars/svg?seed=son34&backgroundColor=dbeafe', 'https://api.dicebear.com/7.x/avataaars/svg?seed=son34&backgroundColor=dbeafe', 0, 1, 2, '2010-05-12', '游戏,动画,足球', 1, 'wx_test_son34', NULL, 0, NULL, '2025-09-26 15:55:00', '2025-09-26 15:55:00');
INSERT INTO `user` VALUES (35, 'test_daughter35', 'testdaughter35@example.com', '13800138035', '$2a$10$1qAz2wSx3eDc4rFv5tGb5eTrKKqYDBOzh8nPL.jvkkV3d3lAk5rse', '测试女儿', 'https://api.dicebear.com/7.x/avataaars/svg?seed=daughter35&backgroundColor=fce7f3', 'https://api.dicebear.com/7.x/avataaars/svg?seed=daughter35&backgroundColor=fce7f3', 1, 1, 2, '2012-09-08', '舞蹈,画画,阅读', 1, 'wx_test_daughter35', NULL, 0, NULL, '2025-09-26 15:55:00', '2025-09-26 15:55:00');
INSERT INTO `user` VALUES (37, 'user_4968', '19052616802@temp.com', '19052616802', '$2a$10$5PwEXw4PcpVf2laEf8llw.9Hhzk7vMdKXY938poB4Vv.ikbZaF1Ra', 'xiaoming', 'http://127.0.0.1:9000/qz-sns/image/register/avatar/66a51bcb-e9d5-4984-9a12-149b4f38fb96.jpg', '', 0, 1, 1, NULL, '科普,历史', 1, NULL, NULL, 0, NULL, '2025-10-05 10:18:55', '2025-10-05 10:18:55');

-- ----------------------------
-- Table structure for user_behavior
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `behavior_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '行为类型：view,like,comment,share,collect',
  `duration` int NULL DEFAULT 0 COMMENT '行为持续时间(秒)，主要用于浏览行为',
  `progress` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '进度百分比(0-100)，用于视频/长文',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源：recommend,search,follow,hot等',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_content`(`user_id` ASC, `content_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_content_behavior`(`content_id` ASC, `behavior_type` ASC) USING BTREE,
  INDEX `idx_behavior_time`(`behavior_type` ASC, `created_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 180 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户行为记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_behavior
-- ----------------------------

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:取消收藏',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_content`(`user_id` ASC, `content_id` ASC) USING BTREE,
  INDEX `idx_user_content`(`user_id` ASC, `content_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_time` ASC) USING BTREE,
  INDEX `idx_content`(`content_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------
INSERT INTO `user_favorite` VALUES (6, 32, 39, '', '2025-09-27 17:09:31', 1);
INSERT INTO `user_favorite` VALUES (7, 32, 40, '', '2025-09-27 19:22:09', 1);
INSERT INTO `user_favorite` VALUES (8, 32, 44, '', '2025-09-27 19:22:15', 1);
INSERT INTO `user_favorite` VALUES (11, 32, 67, '', '2025-10-05 00:31:09', 1);

-- ----------------------------
-- Table structure for user_medal
-- ----------------------------
DROP TABLE IF EXISTS `user_medal`;
CREATE TABLE `user_medal`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `medal_id` bigint NOT NULL COMMENT '勋章ID',
  `get_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户勋章关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_medal
-- ----------------------------

-- ----------------------------
-- Table structure for user_statistics
-- ----------------------------
DROP TABLE IF EXISTS `user_statistics`;
CREATE TABLE `user_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `register_days` int NULL DEFAULT 0 COMMENT '注册天数',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后在线时间',
  `login_days` int NULL DEFAULT 0 COMMENT '累计登录天数',
  `father_chat_count` int NULL DEFAULT 0 COMMENT '与父亲聊天消息数',
  `mother_chat_count` int NULL DEFAULT 0 COMMENT '与母亲聊天消息数',
  `last_father_chat_time` datetime NULL DEFAULT NULL COMMENT '最后与父亲聊天时间',
  `last_mother_chat_time` datetime NULL DEFAULT NULL COMMENT '最后与母亲聊天时间',
  `father_chat_initiative` int NULL DEFAULT 0 COMMENT '主动与父亲聊天次数',
  `mother_chat_initiative` int NULL DEFAULT 0 COMMENT '主动与母亲聊天次数',
  `group_message_count` int NULL DEFAULT 0 COMMENT '群聊消息数',
  `group_active_days` int NULL DEFAULT 0 COMMENT '群聊活跃天数',
  `ai_chat_count` int NULL DEFAULT 0 COMMENT 'AI对话次数',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户统计数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for user_view_history
-- ----------------------------
DROP TABLE IF EXISTS `user_view_history`;
CREATE TABLE `user_view_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `duration` int NULL DEFAULT 0 COMMENT '浏览时长(秒)',
  `progress` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '浏览进度(%)',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
  `view_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_content`(`user_id` ASC, `content_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `view_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户浏览历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_view_history
-- ----------------------------
INSERT INTO `user_view_history` VALUES (1, 9, 1, 180, 100.00, 'recommend', '2024-06-28 10:00:00');
INSERT INTO `user_view_history` VALUES (2, 9, 3, 240, 100.00, 'recommend', '2024-06-28 11:00:00');
INSERT INTO `user_view_history` VALUES (3, 9, 8, 150, 80.00, 'search', '2024-06-28 14:00:00');
INSERT INTO `user_view_history` VALUES (4, 9, 15, 300, 100.00, 'hot', '2024-06-28 15:00:00');
INSERT INTO `user_view_history` VALUES (5, 9, 2, 200, 90.00, 'recommend', '2024-06-27 09:00:00');
INSERT INTO `user_view_history` VALUES (6, 9, 5, 120, 60.00, 'search', '2024-06-27 13:00:00');
INSERT INTO `user_view_history` VALUES (7, 9, 10, 180, 100.00, 'recommend', '2024-06-27 16:00:00');
INSERT INTO `user_view_history` VALUES (8, 9, 4, 360, 100.00, 'recommend', '2024-06-26 10:00:00');
INSERT INTO `user_view_history` VALUES (9, 9, 6, 480, 80.00, 'search', '2024-06-26 14:00:00');
INSERT INTO `user_view_history` VALUES (10, 9, 16, 420, 100.00, 'hot', '2024-06-26 19:00:00');
INSERT INTO `user_view_history` VALUES (11, 10, 2, 300, 100.00, 'recommend', '2024-06-28 09:00:00');
INSERT INTO `user_view_history` VALUES (12, 10, 4, 400, 100.00, 'recommend', '2024-06-28 10:00:00');
INSERT INTO `user_view_history` VALUES (13, 10, 7, 250, 100.00, 'search', '2024-06-28 11:00:00');
INSERT INTO `user_view_history` VALUES (14, 10, 11, 200, 90.00, 'recommend', '2024-06-28 14:00:00');
INSERT INTO `user_view_history` VALUES (15, 10, 17, 350, 100.00, 'hot', '2024-06-28 16:00:00');
INSERT INTO `user_view_history` VALUES (16, 10, 5, 180, 100.00, 'recommend', '2024-06-27 10:00:00');
INSERT INTO `user_view_history` VALUES (17, 10, 9, 360, 80.00, 'search', '2024-06-27 15:00:00');
INSERT INTO `user_view_history` VALUES (18, 10, 18, 280, 100.00, 'hot', '2024-06-27 18:00:00');
INSERT INTO `user_view_history` VALUES (19, 11, 8, 200, 100.00, 'recommend', '2024-06-28 08:00:00');
INSERT INTO `user_view_history` VALUES (20, 11, 10, 240, 100.00, 'recommend', '2024-06-28 09:30:00');
INSERT INTO `user_view_history` VALUES (21, 11, 12, 180, 100.00, 'search', '2024-06-28 12:00:00');
INSERT INTO `user_view_history` VALUES (22, 11, 14, 150, 80.00, 'recommend', '2024-06-28 15:00:00');
INSERT INTO `user_view_history` VALUES (23, 11, 19, 300, 100.00, 'hot', '2024-06-28 17:00:00');
INSERT INTO `user_view_history` VALUES (24, 11, 9, 420, 100.00, 'recommend', '2024-06-27 11:00:00');
INSERT INTO `user_view_history` VALUES (25, 11, 13, 360, 90.00, 'search', '2024-06-27 14:00:00');
INSERT INTO `user_view_history` VALUES (26, 11, 20, 480, 100.00, 'hot', '2024-06-27 20:00:00');
INSERT INTO `user_view_history` VALUES (27, 32, 39, 0, 0.00, NULL, '2025-09-27 17:33:36');
INSERT INTO `user_view_history` VALUES (29, 32, 40, 0, 0.00, NULL, '2025-09-27 19:22:11');
INSERT INTO `user_view_history` VALUES (30, 32, 41, 0, 0.00, NULL, '2025-09-27 16:26:52');
INSERT INTO `user_view_history` VALUES (31, 32, 42, 0, 0.00, NULL, '2025-09-27 16:27:03');
INSERT INTO `user_view_history` VALUES (32, 32, 43, 0, 0.00, NULL, '2025-09-27 19:22:23');
INSERT INTO `user_view_history` VALUES (33, 32, 44, 0, 0.00, NULL, '2025-09-27 19:22:14');
INSERT INTO `user_view_history` VALUES (35, 32, 45, 0, 0.00, NULL, '2025-09-27 16:26:59');
INSERT INTO `user_view_history` VALUES (37, 32, 65, 0, 0.00, NULL, '2025-10-05 07:45:09');
INSERT INTO `user_view_history` VALUES (39, 32, 70, 0, 0.00, NULL, '2025-10-05 07:45:14');
INSERT INTO `user_view_history` VALUES (40, 32, 66, 0, 0.00, NULL, '2025-10-05 07:45:10');
INSERT INTO `user_view_history` VALUES (42, 32, 68, 0, 0.00, NULL, '2025-10-07 14:12:40');
INSERT INTO `user_view_history` VALUES (43, 32, 67, 0, 0.00, NULL, '2025-10-05 07:45:11');
INSERT INTO `user_view_history` VALUES (45, 32, 72, 0, 0.00, NULL, '2025-10-05 13:29:22');
INSERT INTO `user_view_history` VALUES (47, 32, 69, 0, 0.00, NULL, '2025-10-21 09:59:29');
INSERT INTO `user_view_history` VALUES (48, 32, 71, 0, 0.00, NULL, '2025-10-05 13:29:25');
INSERT INTO `user_view_history` VALUES (49, 32, 64, 0, 0.00, NULL, '2025-10-07 14:02:05');
INSERT INTO `user_view_history` VALUES (50, 32, 62, 0, 0.00, NULL, '2025-10-07 22:02:11');

SET FOREIGN_KEY_CHECKS = 1;

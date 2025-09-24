/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : qz_sns

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 24/09/2025 12:38:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内容图片关联表' ROW_FORMAT = DYNAMIC;

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

SET FOREIGN_KEY_CHECKS = 1;

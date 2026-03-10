/*
 Navicat Premium Dump SQL

 Source Server         : acpasser
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : localhost:3306
 Source Schema         : zhihu

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 10/03/2026 20:14:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
  `user_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'GUEST' COMMENT '用户类型',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱地址',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像地址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` int NOT NULL DEFAULT '0' COMMENT '删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_answer
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_answer`;
CREATE TABLE `zhihu_answer` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `answer_id` bigint NOT NULL,
  `answer_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'normal' COMMENT '类型（默认：normal）',
  `author_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '答主token',
  `voteup_count` int unsigned NOT NULL DEFAULT '0' COMMENT '赞同数',
  `thanks_count` int unsigned NOT NULL DEFAULT '0',
  `excerpt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回答摘要',
  `answer_created_time` datetime NOT NULL COMMENT '回答创建时间（linux秒）',
  `answer_updated_time` datetime NOT NULL COMMENT '回答更新时间（linux秒）',
  `comment_permission` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `is_copyable` tinyint NOT NULL,
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `can_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'json字符串',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `question_id` bigint NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  UNIQUE KEY `answer_id_index` (`answer_id`) USING BTREE,
  KEY `author_token_index` (`author_token`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4754 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_article
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_article`;
CREATE TABLE `zhihu_article` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `excerpt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '摘要',
  `author_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作者token',
  `article_created_time` datetime NOT NULL COMMENT '文章创建时间',
  `article_updated_time` datetime NOT NULL COMMENT '文章更新时间',
  `voteup_count` int unsigned NOT NULL COMMENT '投票数',
  `comment_count` int unsigned NOT NULL COMMENT '评论数',
  `comment_permission` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  UNIQUE KEY `article_id_index` (`article_id`) USING BTREE,
  KEY `author_token_index` (`author_token`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5166 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_collection
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_collection`;
CREATE TABLE `zhihu_collection` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `collection_id` bigint NOT NULL,
  `creator_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人token（注：动态中只有收藏叫creator）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `answer_count` int unsigned NOT NULL COMMENT '内容数',
  `follower_count` int unsigned NOT NULL COMMENT '关注数',
  `comment_count` int unsigned NOT NULL COMMENT '评论数',
  `public_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否公开',
  `collection_created_time` datetime NOT NULL COMMENT '收藏夹创建时间',
  `collection_updated_time` datetime NOT NULL COMMENT '收藏夹更新时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_follow_relation
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_follow_relation`;
CREATE TABLE `zhihu_follow_relation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `from_token` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `to_tokens` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '关注对象，逗号分隔',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  KEY `create_time_index` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for zhihu_pin
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_pin`;
CREATE TABLE `zhihu_pin` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pin_id` bigint NOT NULL,
  `author_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `excerpt_title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `comment_count` int unsigned NOT NULL DEFAULT '0' COMMENT '评论数',
  `like_count` int unsigned NOT NULL DEFAULT '0' COMMENT '赞同数',
  `favorite_count` int unsigned NOT NULL DEFAULT '0' COMMENT '收藏数',
  `repin_count` int unsigned NOT NULL DEFAULT '0' COMMENT '转发/转载次数',
  `creation_disclaimer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `comment_permission` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pin_created_time` datetime NOT NULL COMMENT '问题创建时间',
  `topic_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '绑定的话题id，逗号分隔',
  `admin_closed_comment` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否被管理员关闭评论',
  `admin_close_repin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否由管理员关闭了转发/转载功能',
  `contain_ai_content` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否由管理员关闭了转发/转载功能',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  UNIQUE KEY `pin_id_index` (`pin_id`) USING BTREE,
  KEY `author_token_index` (`author_token`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=368 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_question
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_question`;
CREATE TABLE `zhihu_question` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `author_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '答主token，''''表示匿名用户',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `excerpt` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '摘要',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `question_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'normal' COMMENT '类型（默认：normal）',
  `question_created_time` datetime NOT NULL COMMENT '问题创建时间',
  `answer_count` int unsigned NOT NULL COMMENT '回答数',
  `comment_count` int unsigned NOT NULL COMMENT '评论数',
  `follower_count` int unsigned NOT NULL COMMENT '关注者',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `bound_topic_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '绑定的话题id，逗号分隔（这个id貌似有问题，对应不上话题url中的数字）',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_index` (`id`),
  UNIQUE KEY `question_id_index` (`question_id`),
  KEY `author_token_index` (`author_token`)
) ENGINE=InnoDB AUTO_INCREMENT=5589 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_topic
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_topic`;
CREATE TABLE `zhihu_topic` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `topic_id` bigint NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `topic_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NORMAL' COMMENT '类型（默认：NORMAL）',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  UNIQUE KEY `topic_id_index` (`topic_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for zhihu_user
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_user`;
CREATE TABLE `zhihu_user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `token` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `user_type` varchar(16) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'people' COMMENT '用户类型',
  `index_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '知乎个人主页链接',
  `description` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '个人简介',
  `headline` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '签名',
  `ip_info` varchar(16) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT 'IP属地',
  `gender` tinyint NOT NULL DEFAULT '-1' COMMENT '性别 (1: male; 0: female; -1: unknown)',
  `follower_count` int NOT NULL DEFAULT '0' COMMENT '粉丝数',
  `following_count` int NOT NULL DEFAULT '0' COMMENT '关注数',
  `mutual_followees_count` int NOT NULL DEFAULT '0' COMMENT '互关数',
  `answer_count` int NOT NULL DEFAULT '0' COMMENT '回答',
  `question_count` int NOT NULL DEFAULT '0' COMMENT '提问',
  `articles_count` int NOT NULL DEFAULT '0' COMMENT '文章数',
  `columns_count` int NOT NULL DEFAULT '0' COMMENT '专栏数',
  `zvideo_count` int NOT NULL DEFAULT '0' COMMENT '视频数',
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '收藏夹数',
  `favorited_count` int NOT NULL DEFAULT '0' COMMENT '被收藏次数',
  `pins_count` int NOT NULL DEFAULT '0' COMMENT '想法',
  `voteup_count` int NOT NULL DEFAULT '0' COMMENT '被赞同次数',
  `thanked_count` int NOT NULL DEFAULT '0' COMMENT '被喜欢次数',
  `following_columns_count` int NOT NULL DEFAULT '0' COMMENT '关注的专栏',
  `following_topic_count` int NOT NULL DEFAULT '0' COMMENT '关注的话题',
  `following_question_count` int NOT NULL DEFAULT '0' COMMENT '关注的问题',
  `business` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '所在行业',
  `locations` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '现居住地',
  `employments` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '职业-公司',
  `educations` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT '教育经历-学校',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  UNIQUE KEY `token_index` (`token`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9342 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for zhihu_user_interaction
-- ----------------------------
DROP TABLE IF EXISTS `zhihu_user_interaction`;
CREATE TABLE `zhihu_user_interaction` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `interaction_id` bigint NOT NULL,
  `actor_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `author_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '''''表示匿名用户',
  `is_following` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'actor关注author',
  `is_followed` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'actor被anthor关注',
  `type` varchar(10) NOT NULL DEFAULT '' COMMENT '互动类型',
  `action_text` varchar(20) NOT NULL DEFAULT '' COMMENT '互动类型文本',
  `target_id` bigint NOT NULL COMMENT '互动事件id（question、answer、article...）',
  `target_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '互动类型（question、answer、article...）',
  `verb` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '知乎字段（暂时不清楚意思）',
  `interaction_time` datetime NOT NULL COMMENT '互动时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id_index` (`id`) USING BTREE,
  UNIQUE KEY `interaction_id_index` (`interaction_id`) USING BTREE,
  KEY `actor_token_index` (`actor_token`),
  KEY `author_token_index` (`author_token`)
) ENGINE=InnoDB AUTO_INCREMENT=18993 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

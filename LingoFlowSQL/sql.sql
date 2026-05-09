-- 1. 创建数据库（如果不存在则创建）
-- 指定 utf8mb4 字符集，这样后续即使文章里有生僻字或 Emoji 表情也能完美存储。
CREATE DATABASE IF NOT EXISTS `lingo_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 切换到刚刚创建的数据库
USE `lingo_db`;

为了重新执行时清空数据，先删除旧表（注意删除顺序，如果有外键约束的话）
DROP TABLE IF EXISTS `lingo_message`;
DROP TABLE IF EXISTS `lingo_correction_ticket`;
DROP TABLE IF EXISTS `lingo_vocabulary`;
DROP TABLE IF EXISTS `lingo_article`;
DROP TABLE IF EXISTS `lingo_user`;

-- 3. 创建用户档案表
-- 表必备三字段：id, create_time, update_time
-- id 设为 bigint unsigned 单表自增。
CREATE TABLE `lingo_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password_hash` varchar(100) NOT NULL COMMENT '加密后的密码',
  `target_language` varchar(20) DEFAULT 'EN' COMMENT '目标学习语言(如EN, JP)',
  `current_level` varchar(50) DEFAULT 'V1000' COMMENT '当前能力等级(如词汇量1000, N4)',
  `user_role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '用户角色：USER普通用户, ADMIN管理员',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否逻辑删除：1是，0否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户档案表';

-- 4. 创建智能文章表
-- 对于可能超长的文章内容，由于 varchar 长度不应超过 5000，这里独立定义为 text 类型。
CREATE TABLE `lingo_article` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `original_text` text NOT NULL COMMENT '原始长文本',
  `adapted_text` text NOT NULL COMMENT 'AI降级重写后的文本',
  `target_language` varchar(20) NOT NULL COMMENT '文章语种',
  `difficulty_level` varchar(50) NOT NULL COMMENT '设定的目标难度',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否逻辑删除：1是，0否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能文章表';

-- 5. 创建语境生词本表
-- 表达是与否概念的字段，严格使用 is_xxx 命名，且类型为无符号的 tinyint。
CREATE TABLE `lingo_vocabulary` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
  `article_id` bigint unsigned NOT NULL COMMENT '来源文章ID',
  `word` varchar(100) NOT NULL COMMENT '生词/短语',
  `translation` varchar(255) NOT NULL COMMENT 'AI结合语境给出的释义',
  `context_sentence` varchar(1000) NOT NULL COMMENT '在原文章中的上下文例句',
  `is_mastered` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否已掌握：1是，0否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_word` (`user_id`,`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语境生词本表';

-- 6. 创建纠错工单表
CREATE TABLE `lingo_correction_ticket` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '提交用户ID',
  `type` varchar(20) NOT NULL COMMENT '纠错类型(article, vocabulary)',
  `target_id` bigint unsigned NOT NULL COMMENT '目标ID',
  `original_content` text NOT NULL COMMENT '纠错时的原始内容',
  `user_suggestion` text COMMENT '用户建议的修正内容',
  `admin_remark` text COMMENT '管理员处理备注',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING, ACCEPTED, REJECTED)',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纠错工单表';

-- 7. 创建系统消息表
CREATE TABLE `lingo_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否已读：1是，0否',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否逻辑删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息表';

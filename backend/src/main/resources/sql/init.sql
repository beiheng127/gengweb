CREATE DATABASE IF NOT EXISTS gengbaike DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gengbaike;

CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `role` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1-普通用户 2-审核员 3-管理员',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `meme` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模因ID',
  `title` varchar(255) NOT NULL COMMENT '梗名称',
  `origin_time` datetime NOT NULL COMMENT '起源时间',
  `boom_time` datetime NOT NULL COMMENT '爆火时间',
  `content` text COMMENT '文字描述',
  `media_type` tinyint(4) NOT NULL COMMENT '1-图片 2-视频 3-音频',
  `media_url` varchar(512) NOT NULL COMMENT 'OSS媒体URL',
  `audio_url` varchar(512) DEFAULT NULL COMMENT '音频URL',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `dislike_count` int(11) NOT NULL DEFAULT 0 COMMENT '点踩数',
  `current_version` int(11) NOT NULL DEFAULT 1 COMMENT '当前版本',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0-草稿 1-待审核 2-已通过 3-已驳回',
  `create_user_id` bigint(20) NOT NULL COMMENT '创建用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_title` (`title`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_like_count` (`like_count`),
  FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模因核心表';

CREATE TABLE IF NOT EXISTS `meme_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `meme_id` bigint(20) NOT NULL COMMENT '关联模因ID',
  `version` int(11) NOT NULL COMMENT '版本号',
  `title` varchar(255) NOT NULL COMMENT '该版本名称',
  `content` text COMMENT '该版本文字描述',
  `media_url` varchar(512) NOT NULL COMMENT '该版本媒体URL',
  `audio_url` varchar(512) DEFAULT NULL COMMENT '该版本音频URL',
  `audit_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0-待审核 1-通过 2-驳回',
  `audit_user_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `reject_reason` varchar(512) DEFAULT NULL COMMENT '驳回原因',
  `create_user_id` bigint(20) NOT NULL COMMENT '修改人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meme_version` (`meme_id`, `version`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模因版本表';

CREATE TABLE IF NOT EXISTS `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1-系统 2-用户自定义',
  `use_count` int(11) NOT NULL DEFAULT 0 COMMENT '使用次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE IF NOT EXISTS `meme_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `meme_id` bigint(20) NOT NULL COMMENT '模因ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meme_tag` (`meme_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模因标签关联表';

CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `meme_id` bigint(20) NOT NULL COMMENT '模因ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_meme_id` (`meme_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE IF NOT EXISTS `user_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `target_type` tinyint(4) NOT NULL COMMENT '1-模因 2-评论',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID',
  `type` tinyint(4) NOT NULL COMMENT '1-点赞 2-点踩',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户点赞点踩表';

INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'admin@gengbaike.com', '管理员', 3, 1),
('auditor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'auditor@gengbaike.com', '审核员', 2, 1),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'user1@gengbaike.com', '测试用户', 1, 1);

INSERT INTO `tag` (`name`, `type`, `use_count`) VALUES
('搞笑', 1, 0),
('热门', 1, 0),
('经典', 1, 0),
('网络热梗', 1, 0);

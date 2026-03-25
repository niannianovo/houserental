-- ============================================
-- 房屋租赁系统 数据库建表脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS houserental DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE houserental;

-- -------------------------------------------
-- 1. 用户表
-- -------------------------------------------
CREATE TABLE `user` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `account` VARCHAR(50) NOT NULL COMMENT '账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（MD5）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `is_admin` TINYINT NOT NULL DEFAULT 0 COMMENT '是否管理员（0否 1是）',
    `current_role` TINYINT DEFAULT 0 COMMENT '当前身份（0租客 1房东）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    `is_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '实名认证（0未认证 1已认证）',
    `is_email_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证（0/1）',
    `id_card_front` VARCHAR(255) DEFAULT NULL COMMENT '身份证正面照URL',
    `id_card_back` VARCHAR(255) DEFAULT NULL COMMENT '身份证反面照URL',
    `report_credit` INT NOT NULL DEFAULT 100 COMMENT '举报信誉分',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '账号状态（0正常 1禁用）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -------------------------------------------
-- 2. 房源表
-- -------------------------------------------
CREATE TABLE `house` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `owner_id` INT NOT NULL COMMENT '房东ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `address` VARCHAR(255) NOT NULL COMMENT '地址',
    `area` DECIMAL(10,2) DEFAULT NULL COMMENT '面积（平方米）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '月租金',
    `deposit` DECIMAL(10,2) DEFAULT NULL COMMENT '押金',
    `room_count` INT DEFAULT NULL COMMENT '室',
    `hall_count` INT DEFAULT NULL COMMENT '厅',
    `floor` VARCHAR(20) DEFAULT NULL COMMENT '楼层',
    `house_type` TINYINT DEFAULT 0 COMMENT '类型（0整租 1合租）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待审核 1已上架 2已下架 3已出租）',
    `verify_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态（0未审核 1已通过 2已驳回 3标记虚假）',
    `verify_score` INT NOT NULL DEFAULT 0 COMMENT '真实性评分（0-100）',
    `is_certified` TINYINT NOT NULL DEFAULT 0 COMMENT '房源认证（0未认证 1已认证）',
    `images` TEXT DEFAULT NULL COMMENT '图片URL（逗号分隔）',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `contact_count` INT NOT NULL DEFAULT 0 COMMENT '沟通数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源表';

-- -------------------------------------------
-- 3. 浏览记录表
-- -------------------------------------------
CREATE TABLE `house_view_log` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `house_id` INT NOT NULL COMMENT '房源ID',
    `user_id` INT DEFAULT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览记录表';

-- -------------------------------------------
-- 4. 收藏表
-- -------------------------------------------
CREATE TABLE `house_favorite` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `house_id` INT NOT NULL COMMENT '房源ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_house` (`user_id`, `house_id`),
    KEY `idx_house_id` (`house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- -------------------------------------------
-- 5. 房源评论表
-- -------------------------------------------
CREATE TABLE `house_comment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `house_id` INT NOT NULL COMMENT '房源ID',
    `user_id` INT NOT NULL COMMENT '评论人ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分（1-5星）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1被举报待审核 2已删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源评论表';

-- -------------------------------------------
-- 6. 房源备注表
-- -------------------------------------------
CREATE TABLE `house_note` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `house_id` INT NOT NULL COMMENT '房源ID',
    `content` TEXT NOT NULL COMMENT '备注内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_house` (`user_id`, `house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源备注表';

-- -------------------------------------------
-- 7. 用户互评表
-- -------------------------------------------
CREATE TABLE `user_review` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL COMMENT '关联租约ID',
    `reviewer_id` INT NOT NULL COMMENT '评价人ID',
    `target_id` INT NOT NULL COMMENT '被评价人ID',
    `reviewer_role` TINYINT NOT NULL COMMENT '评价方向（0租客评房东 1房东评租客）',
    `content` TEXT DEFAULT NULL COMMENT '评价内容',
    `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分（1-5星）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1被举报待审核 2已删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_reviewer` (`order_id`, `reviewer_id`),
    KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户互评表';

-- -------------------------------------------
-- 8. 拉黑表
-- -------------------------------------------
CREATE TABLE `user_block` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '拉黑发起人',
    `blocked_user_id` INT NOT NULL COMMENT '被拉黑的人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拉黑表';

-- -------------------------------------------
-- 9. 用户偏好表
-- -------------------------------------------
CREATE TABLE `user_preference` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `preferred_areas` VARCHAR(255) DEFAULT NULL COMMENT '偏好区域（逗号分隔）',
    `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT '偏好最低价',
    `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT '偏好最高价',
    `preferred_house_type` TINYINT DEFAULT NULL COMMENT '偏好类型（0整租 1合租）',
    `preferred_room_count` INT DEFAULT NULL COMMENT '偏好几居',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好表';

-- -------------------------------------------
-- 10. 搜索历史表
-- -------------------------------------------
CREATE TABLE `search_history` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `keyword` VARCHAR(100) DEFAULT NULL COMMENT '搜索关键词',
    `filters` TEXT DEFAULT NULL COMMENT '筛选条件（JSON）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- -------------------------------------------
-- 11. 聊天会话表
-- -------------------------------------------
CREATE TABLE `conversation` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `house_id` INT DEFAULT NULL COMMENT '关联房源ID（客服会话为空）',
    `user1_id` INT NOT NULL COMMENT '发起方',
    `user2_id` INT NOT NULL COMMENT '接收方',
    `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0租客↔房东 1租客↔客服 2房东↔客服）',
    `last_message` VARCHAR(255) DEFAULT NULL COMMENT '最后一条消息',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user1_id` (`user1_id`),
    KEY `idx_user2_id` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- -------------------------------------------
-- 12. 聊天消息表
-- -------------------------------------------
CREATE TABLE `chat_message` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `conversation_id` INT NOT NULL COMMENT '会话ID',
    `sender_id` INT NOT NULL COMMENT '发送人ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `msg_type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0文本 1图片 2表情 3文件 9系统消息）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态（0未读 1已读）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- -------------------------------------------
-- 13. 快捷回复表
-- -------------------------------------------
CREATE TABLE `quick_reply` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '房东ID',
    `title` VARCHAR(50) NOT NULL COMMENT '标签',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快捷回复表';

-- -------------------------------------------
-- 14. 租约表
-- -------------------------------------------
CREATE TABLE `rental_order` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `house_id` INT NOT NULL COMMENT '房源ID',
    `tenant_id` INT NOT NULL COMMENT '租客ID',
    `owner_id` INT NOT NULL COMMENT '房东ID',
    `start_date` DATE NOT NULL COMMENT '租期开始',
    `end_date` DATE NOT NULL COMMENT '租期结束',
    `monthly_rent` DECIMAL(10,2) NOT NULL COMMENT '月租金',
    `contract_file` VARCHAR(255) DEFAULT NULL COMMENT '合同文件URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待签约 1进行中 2已到期 3续租中 4退租申请中 5已退租）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_house_id` (`house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租约表';

-- -------------------------------------------
-- 15. 租约操作日志表
-- -------------------------------------------
CREATE TABLE `rental_order_log` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL COMMENT '租约ID',
    `operator_id` INT NOT NULL COMMENT '操作人ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作（签约/续租/退租申请/退租确认/退租拒绝）',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租约操作日志表';

-- -------------------------------------------
-- 16. 租金支付表
-- -------------------------------------------
CREATE TABLE `rent_payment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `order_id` INT NOT NULL COMMENT '租约ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `pay_month` VARCHAR(10) NOT NULL COMMENT '所属月份（如 2026-03）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待缴 1已缴 2逾期）',
    `pay_method` TINYINT DEFAULT NULL COMMENT '支付方式（0线下 1支付宝 2微信）',
    `pay_proof` VARCHAR(255) DEFAULT NULL COMMENT '支付凭证图片URL',
    `confirm_status` TINYINT NOT NULL DEFAULT 0 COMMENT '房东确认（0待确认 1已确认）',
    `pay_time` DATETIME DEFAULT NULL COMMENT '实际支付时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租金支付表';

-- -------------------------------------------
-- 17. 认证申请表
-- -------------------------------------------
CREATE TABLE `verify_request` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '申请人ID',
    `type` TINYINT NOT NULL COMMENT '类型（0实名认证 1房源认证）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    `id_card_front` VARCHAR(255) DEFAULT NULL COMMENT '身份证正面URL',
    `id_card_back` VARCHAR(255) DEFAULT NULL COMMENT '身份证反面URL',
    `house_id` INT DEFAULT NULL COMMENT '房源ID（房源认证）',
    `property_cert` VARCHAR(255) DEFAULT NULL COMMENT '房产证照片URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待审核 1通过 2驳回）',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    `admin_id` INT DEFAULT NULL COMMENT '审核人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证申请表';

-- -------------------------------------------
-- 18. 通用举报表
-- -------------------------------------------
CREATE TABLE `report` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `reporter_id` INT NOT NULL COMMENT '举报人ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型（0房源 1房源评论 2用户互评）',
    `target_id` INT NOT NULL COMMENT '目标ID',
    `reason` TEXT NOT NULL COMMENT '举报原因',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待处理 1举报成立 2举报不成立）',
    `admin_id` INT DEFAULT NULL COMMENT '处理人',
    `result` VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用举报表';

-- -------------------------------------------
-- 19. 通知表
-- -------------------------------------------
CREATE TABLE `notification` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '接收人ID',
    `type` TINYINT NOT NULL COMMENT '类型（0降价 1公告 2审核结果 3举报结果 4支付提醒 5合同提醒）',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` TEXT DEFAULT NULL COMMENT '内容',
    `related_id` INT DEFAULT NULL COMMENT '关联ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态（0未读 1已读）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- -------------------------------------------
-- 20. 系统公告表
-- -------------------------------------------
CREATE TABLE `announcement` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `admin_id` INT NOT NULL COMMENT '发布人',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0草稿 1已发布 2已下架）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- -------------------------------------------
-- 21. 管理员操作日志表
-- -------------------------------------------
CREATE TABLE `admin_log` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `admin_id` INT NOT NULL COMMENT '操作人',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型（user/house/comment/review/announcement）',
    `target_id` INT DEFAULT NULL COMMENT '目标ID',
    `detail` TEXT DEFAULT NULL COMMENT '操作详情',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- -------------------------------------------
-- 22. 预约看房表
-- -------------------------------------------
CREATE TABLE `appointment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `house_id` INT NOT NULL COMMENT '房源ID',
    `user_id` INT NOT NULL COMMENT '预约人ID',
    `appointment_time` DATETIME NOT NULL COMMENT '预约时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0待确认 1已确认 2已取消 3已完成）',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约看房表';

-- -------------------------------------------
-- 初始数据：插入默认管理员账号
-- 账号: admin  密码: 123456 (MD5加密)
-- -------------------------------------------
INSERT INTO `user` (`account`, `password`, `nickname`, `is_admin`, `status`, `email`, `is_email_verified`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 0, 'admin@system.com', 1);

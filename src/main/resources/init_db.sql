-- 创建数据库
CREATE DATABASE IF NOT EXISTS classroom_attendance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE classroom_attendance;

-- 班级表
CREATE TABLE IF NOT EXISTS `class` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_name` VARCHAR(50) NOT NULL COMMENT '班级名称',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业名称',
    `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
    `teacher` VARCHAR(50) DEFAULT NULL COMMENT '班主任',
    `student_count` INT DEFAULT 0 COMMENT '班级人数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_class_name` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 插入测试数据
INSERT INTO `class` (`class_name`, `major`, `grade`, `teacher`, `student_count`) VALUES
('计算机科学与技术1班', '计算机科学与技术', '2022', '张老师', 45),
('软件工程2班', '软件工程', '2022', '李老师', 42),
('人工智能1班', '人工智能', '2023', '王老师', 38),
('数据科学1班', '数据科学与大数据技术', '2023', '赵老师', 40);

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '角色（admin/teacher/student）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 学生信息表
CREATE TABLE IF NOT EXISTS `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_no` VARCHAR(20) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户ID',
    `face_image` VARCHAR(255) DEFAULT NULL COMMENT '人脸图片路径',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `teacher_id` BIGINT DEFAULT NULL COMMENT '教师ID',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
    `classroom` VARCHAR(50) DEFAULT NULL COMMENT '教室',
    `start_time` TIME DEFAULT NULL COMMENT '开始时间',
    `end_time` TIME DEFAULT NULL COMMENT '结束时间',
    `week_day` VARCHAR(20) DEFAULT NULL COMMENT '星期（周一/周二...）',
    `start_week` INT DEFAULT 1 COMMENT '开始周',
    `end_week` INT DEFAULT 18 COMMENT '结束周',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 考勤记录表
CREATE TABLE IF NOT EXISTS `attendance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `class_id` BIGINT NOT NULL COMMENT '班级ID',
    `attendance_date` DATE NOT NULL COMMENT '考勤日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'present' COMMENT '状态（present/absent/late/leave）',
    `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
    `confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '识别置信度',
    `image_path` VARCHAR(255) DEFAULT NULL COMMENT '抓拍图片路径',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_attendance_date` (`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 行为识别记录表
CREATE TABLE IF NOT EXISTS `behavior` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT DEFAULT NULL COMMENT '学生ID',
    `class_id` BIGINT NOT NULL COMMENT '班级ID',
    `course_id` BIGINT DEFAULT NULL COMMENT '课程ID',
    `behavior_type` VARCHAR(50) NOT NULL COMMENT '行为类型（sleeping/phone/eating/etc.）',
    `behavior_time` DATETIME NOT NULL COMMENT '行为发生时间',
    `confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '识别置信度',
    `image_path` VARCHAR(255) DEFAULT NULL COMMENT '抓拍图片路径',
    `handled` TINYINT DEFAULT 0 COMMENT '是否已处理（0-未处理，1-已处理）',
    `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_behavior_time` (`behavior_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为识别记录表';

-- 文件记录表
CREATE TABLE IF NOT EXISTS `file_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name` VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_extension` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `upload_by` BIGINT DEFAULT NULL COMMENT '上传者用户ID',
    `category` VARCHAR(50) DEFAULT 'general' COMMENT '文件分类',
    `ref_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_upload_by` (`upload_by`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- 请假申请表
CREATE TABLE IF NOT EXISTS `leave_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `course_id` BIGINT DEFAULT NULL COMMENT '课程ID',
    `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
    `leave_type` VARCHAR(20) NOT NULL DEFAULT 'personal' COMMENT '请假类型（sick/personal）',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `reason` VARCHAR(500) NOT NULL COMMENT '请假事由',
    `proof_image` VARCHAR(255) DEFAULT NULL COMMENT '证明材料图片路径',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态（pending/approved/rejected）',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approve_remark` VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 通知消息表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '通知类型（attendance/behavior/leave/system）',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
    `ref_id` BIGINT DEFAULT NULL COMMENT '关联数据ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

-- 插入默认管理员账号（密码：admin123，实际使用时需要加密）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `email`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 'admin@example.com'),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', 'teacher', 'teacher1@example.com');

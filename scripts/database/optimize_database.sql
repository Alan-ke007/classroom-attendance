-- ============================================
-- 数据库优化脚本
-- 1. 为所有学生创建用户登录账号
-- 2. 删除测试账号
-- 3. 学生记录关联用户ID
-- 4. 行为记录关联实际课程
-- ============================================

USE classroom_attendance;

SET SQL_SAFE_UPDATES = 0;

-- ============================================
-- 第一步：删除旧的测试账号
-- ============================================
DELETE FROM `user` WHERE username = 'teststudent';

-- ============================================
-- 第二步：为每个学生创建用户账号
-- 用户名 = 学号，密码 = 123456 (BCrypt加密)
-- ============================================
INSERT INTO `user` (username, password, real_name, role, email, phone, create_time)
SELECT
    s.student_no,
    '$2b$10$P/cEldTnK3o/Uovn84QB6eCjS7flStbeABFNx.nzYotT87jYMPqbW',
    s.name,
    'student',
    s.email,
    s.phone,
    NOW()
FROM `student` s
LEFT JOIN `user` u ON u.username = s.student_no
WHERE u.id IS NULL
  AND s.deleted = 0;

-- ============================================
-- 第三步：更新学生记录，关联用户ID
-- ============================================
UPDATE `student` s
JOIN `user` u ON u.username = s.student_no AND u.role = 'student'
SET s.user_id = u.id
WHERE s.user_id IS NULL
   OR s.user_id = 0;

-- ============================================
-- 第四步：清理不关联实际课程的行为记录
-- ============================================
DELETE FROM `behavior`
WHERE course_id IS NULL
   OR student_id IS NULL;

-- ============================================
-- 第五步：生成真实行为记录
-- 为每个学生在对应课程的上课时间内生成行为（最近一周）
-- ============================================
INSERT IGNORE INTO `behavior` (student_id, class_id, course_id, behavior_type, behavior_time, confidence, handled, handle_remark, create_time)
SELECT
    s.id,
    s.class_id,
    c.id,
    bt.type,
    TIMESTAMP(d.att_date, ADDTIME(c.start_time, SEC_TO_TIME(FLOOR(RAND() * 2700) + 600))),
    ROUND(0.75 + RAND() * 0.24, 2),
    CASE WHEN bt.type IN ('using_phone', 'bowing_head') AND RAND() < 0.3 THEN 1 ELSE 0 END,
    CASE WHEN bt.type IN ('using_phone', 'bowing_head') AND RAND() < 0.3 THEN '系统自动检测' ELSE NULL END,
    NOW()
FROM `student` s
JOIN `course` c ON c.class_id = s.class_id
CROSS JOIN (
    SELECT 'raising_hand' AS type, 0.20 AS prob
    UNION SELECT 'reading', 0.25
    UNION SELECT 'writing', 0.25
    UNION SELECT 'using_phone', 0.12
    UNION SELECT 'bowing_head', 0.10
    UNION SELECT 'leaning_over', 0.05
    UNION SELECT 'standing_up', 0.03
) bt
CROSS JOIN (
    SELECT '2026-05-19' AS att_date
    UNION SELECT '2026-05-18'
    UNION SELECT '2026-05-16'
    UNION SELECT '2026-05-15'
    UNION SELECT '2026-05-14'
    UNION SELECT '2026-05-13'
    UNION SELECT '2026-05-12'
) d
WHERE s.class_id IN (1, 2, 3, 4, 5, 6, 7)
  -- 只在该课程对应的星期几生成行为
  AND DAYOFWEEK(d.att_date) = CASE c.week_day
    WHEN '周一' THEN 2 WHEN '周二' THEN 3 WHEN '周三' THEN 4
    WHEN '周四' THEN 5 WHEN '周五' THEN 6 WHEN '周六' THEN 7 WHEN '周日' THEN 1
  END
  AND RAND() < bt.prob * 0.5;

-- ============================================
-- 第六步：补充今日实时行为数据（用于行为监控演示页）
-- ============================================
INSERT IGNORE INTO `behavior` (student_id, class_id, course_id, behavior_type, behavior_time, confidence, handled, handle_remark, create_time)
SELECT
    s.id,
    s.class_id,
    c.id,
    bt2.type,
    -- 当前课程时间内的行为
    TIMESTAMP(CURDATE(), ADDTIME(c.start_time, SEC_TO_TIME(FLOOR(RAND() * 2700) + 300))),
    ROUND(0.78 + RAND() * 0.21, 2),
    0,
    NULL,
    NOW()
FROM `student` s
JOIN `course` c ON c.class_id = s.class_id
CROSS JOIN (
    SELECT 'using_phone' AS type
    UNION SELECT 'bowing_head'
    UNION SELECT 'leaning_over'
    UNION SELECT 'raising_hand'
    UNION SELECT 'reading'
    UNION SELECT 'writing'
) bt2
WHERE s.class_id IN (2, 3, 5)
  -- 只选择当前星期几有课的课程
  AND c.week_day = CASE DAYOFWEEK(CURDATE())
    WHEN 2 THEN '周一' WHEN 3 THEN '周二' WHEN 4 THEN '周三'
    WHEN 5 THEN '周四' WHEN 6 THEN '周五' WHEN 7 THEN '周六' WHEN 1 THEN '周日'
  END
  -- 只选择当前正在进行的课程
  AND c.start_time <= CURTIME()
  AND c.end_time >= ADDTIME(CURTIME(), '-00:30:00')
  AND RAND() < 0.08;

COMMIT;

SET SQL_SAFE_UPDATES = 1;

-- ============================================
-- 验证结果
-- ============================================
SELECT '--- 验证结果 ---' AS '';

-- 检查学生用户账号数量
SELECT '学生用户账号数' AS 项目, COUNT(*) AS 数量
FROM `user` WHERE role = 'student';

-- 检查学生关联情况
SELECT '学生已关联用户ID' AS 项目, COUNT(*) AS 数量
FROM `student` WHERE user_id IS NOT NULL AND user_id > 0;

-- 检查学生未关联用户ID
SELECT '学生未关联用户ID' AS 项目, COUNT(*) AS 数量
FROM `student` WHERE user_id IS NULL OR user_id = 0;

-- 检查行为记录关联课程情况
SELECT '行为记录(有课程)' AS 项目, COUNT(*) AS 数量
FROM `behavior` WHERE course_id IS NOT NULL;

-- 各班级课程数量
SELECT CONCAT('班级', class_id, '课程数: ', COUNT(*)) AS 课程分布
FROM `course` GROUP BY class_id ORDER BY class_id;

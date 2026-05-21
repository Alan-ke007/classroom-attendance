-- ============================================
-- 智课考勤 - 示例数据导入脚本
-- 兼容 Java 模型 @TableName 注解
-- ============================================

USE classroom_attendance;

-- ============================================
-- 1. 管理员/教师账户
-- 密码均为 admin123 (BCrypt加密)
-- ============================================
INSERT IGNORE INTO `user` (username, password, real_name, role, email, phone, create_time) VALUES
('admin', '$2a$10$HxVaYWBFXK2hkDY8YDJffukeAU0WVroPyrChS6.vlY2ZIPIOyLD7S', '系统管理员', 'admin', 'admin@smartclass.cn', '13800000001', NOW()),
('zhanglaoshi', '$2a$10$HxVaYWBFXK2hkDY8YDJffukeAU0WVroPyrChS6.vlY2ZIPIOyLD7S', '张教授', 'teacher', 'zhang@smartclass.cn', '13800000002', NOW()),
('lilaoshi', '$2a$10$HxVaYWBFXK2hkDY8YDJffukeAU0WVroPyrChS6.vlY2ZIPIOyLD7S', '李讲师', 'teacher', 'li@smartclass.cn', '13800000003', NOW()),
('wanglaoshi', '$2a$10$HxVaYWBFXK2hkDY8YDJffukeAU0WVroPyrChS6.vlY2ZIPIOyLD7S', '王老师', 'teacher', 'wang@smartclass.cn', '13800000004', NOW()),
('teststudent', '$2a$10$HxVaYWBFXK2hkDY8YDJffukeAU0WVroPyrChS6.vlY2ZIPIOyLD7S', '测试学生', 'student', 'test@smartclass.cn', '13800000005', NOW());

-- ============================================
-- 2. 班级数据
-- ============================================
INSERT IGNORE INTO `class` (class_name, major, grade, teacher, student_count, create_time) VALUES
('计算机科学与技术2101班', '计算机科学与技术', '2021级', '张教授', 42, NOW()),
('计算机科学与技术2201班', '计算机科学与技术', '2022级', '李讲师', 40, NOW()),
('软件工程2201班', '软件工程', '2022级', '王老师', 38, NOW()),
('软件工程2202班', '软件工程', '2022级', '王老师', 36, NOW()),
('人工智能2301班', '人工智能', '2023级', '张教授', 35, NOW()),
('数据科学与大数据2201班', '数据科学', '2022级', '李讲师', 33, NOW()),
('网络工程2101班', '网络工程', '2021级', '张教授', 30, NOW());

-- ============================================
-- 3. 课程数据 (模拟真实大学课表：周一至周五，上午/下午/晚间均有课)
--    时段: 1-2节 08:00-09:40 | 3-4节 10:00-11:40 | 5-6节 14:00-15:40 | 7-8节 16:00-17:40 | 9-10节 19:00-20:40
-- ============================================
INSERT IGNORE INTO `course` (course_name, teacher_id, class_id, classroom, start_time, end_time, week_day, create_time) VALUES
-- 计科2101 (大四，课较少，但分布合理)
('编译原理', 2, 1, '教学楼A301', '08:00', '09:40', '周一', NOW()),
('编译原理', 2, 1, '教学楼A301', '10:00', '11:40', '周三', NOW()),
('软件工程', 2, 1, '教学楼A302', '14:00', '15:40', '周二', NOW()),
('软件工程', 2, 1, '教学楼A302', '16:00', '17:40', '周四', NOW()),
('人工智能导论', 3, 1, '实验楼B101', '08:00', '09:40', '周五', NOW()),

-- 计科2201 (大二，核心专业课密集)
('数据结构', 2, 2, '教学楼A201', '08:00', '09:40', '周一', NOW()),
('数据结构', 2, 2, '教学楼A201', '10:00', '11:40', '周三', NOW()),
('计算机网络', 3, 2, '教学楼A202', '08:00', '09:40', '周二', NOW()),
('计算机网络', 3, 2, '教学楼A202', '14:00', '15:40', '周五', NOW()),
('Java程序开发', 3, 2, '实验楼B201', '10:00', '11:40', '周四', NOW()),
('Java程序开发', 3, 2, '实验楼B201', '16:00', '17:40', '周四', NOW()),
('数据库原理', 2, 2, '教学楼A203', '14:00', '15:40', '周一', NOW()),
('数据库原理', 2, 2, '教学楼A203', '08:00', '09:40', '周三', NOW()),
('Web前端开发', 4, 2, '实验楼B202', '16:00', '17:40', '周二', NOW()),

-- 软工2201 (大二，理论与实践结合)
('软件测试', 4, 3, '教学楼B301', '08:00', '09:40', '周一', NOW()),
('软件测试', 4, 3, '实验楼C101', '14:00', '15:40', '周三', NOW()),
('Linux系统管理', 4, 3, '实验楼C102', '10:00', '11:40', '周二', NOW()),
('软件项目管理', 4, 3, '教学楼B302', '08:00', '09:40', '周四', NOW()),
('软件项目管理', 4, 3, '教学楼B302', '10:00', '11:40', '周五', NOW()),
('移动应用开发', 3, 3, '实验楼C101', '16:00', '17:40', '周一', NOW()),
('移动应用开发', 3, 3, '实验楼C101', '16:00', '17:40', '周四', NOW()),
('云计算技术', 3, 3, '教学楼B303', '14:00', '15:40', '周五', NOW()),

-- 软工2202 (大二，与2201部分共享教师)
('软件测试', 4, 4, '教学楼B301', '10:00', '11:40', '周一', NOW()),
('软件测试', 4, 4, '实验楼C101', '16:00', '17:40', '周三', NOW()),
('Linux系统管理', 4, 4, '实验楼C102', '14:00', '15:40', '周二', NOW()),
('软件项目管理', 4, 4, '教学楼B302', '14:00', '15:40', '周四', NOW()),
('软件项目管理', 4, 4, '教学楼B302', '08:00', '09:40', '周五', NOW()),
('移动应用开发', 3, 4, '实验楼C102', '08:00', '09:40', '周二', NOW()),
('设计模式', 3, 4, '教学楼B303', '10:00', '11:40', '周四', NOW()),
('设计模式', 3, 4, '教学楼B303', '16:00', '17:40', '周五', NOW()),

-- 人工智能2301 (大二上，课业繁重)
('高等数学A2', 2, 5, '教学楼C401', '08:00', '09:40', '周一', NOW()),
('高等数学A2', 2, 5, '教学楼C401', '08:00', '09:40', '周三', NOW()),
('高等数学A2', 2, 5, '教学楼C401', '10:00', '11:40', '周五', NOW()),
('机器学习', 2, 5, '教学楼C402', '10:00', '11:40', '周一', NOW()),
('机器学习', 2, 5, '教学楼C402', '14:00', '15:40', '周四', NOW()),
('深度学习', 3, 5, '教学楼C403', '08:00', '09:40', '周二', NOW()),
('深度学习', 3, 5, '实验楼D101', '16:00', '17:40', '周二', NOW()),
('自然语言处理', 3, 5, '实验楼D101', '14:00', '15:40', '周三', NOW()),
('计算机视觉', 2, 5, '实验楼D102', '10:00', '11:40', '周四', NOW()),
('Python科学计算', 3, 5, '教学楼C404', '19:00', '20:40', '周三', NOW()),

-- 大数据2201 (大二，核心专业课)
('大数据技术基础', 3, 6, '教学楼D501', '08:00', '09:40', '周一', NOW()),
('大数据技术基础', 3, 6, '教学楼D501', '10:00', '11:40', '周四', NOW()),
('Hadoop生态系统', 3, 6, '实验楼E101', '10:00', '11:40', '周二', NOW()),
('Hadoop生态系统', 3, 6, '实验楼E101', '14:00', '15:40', '周五', NOW()),
('Spark流计算', 3, 6, '教学楼D502', '14:00', '15:40', '周一', NOW()),
('Spark流计算', 3, 6, '实验楼E101', '16:00', '17:40', '周三', NOW()),
('数据可视化', 4, 6, '实验楼E102', '08:00', '09:40', '周三', NOW()),
('数据挖掘', 3, 6, '教学楼D503', '16:00', '17:40', '周四', NOW()),

-- 网工2101 (大三，专业课 + 实践)
('网络工程', 2, 7, '教学楼F601', '08:00', '09:40', '周一', NOW()),
('网络工程', 2, 7, '教学楼F601', '10:00', '11:40', '周三', NOW()),
('网络安全', 2, 7, '教学楼F602', '14:00', '15:40', '周二', NOW()),
('网络安全', 2, 7, '教学楼F602', '16:00', '17:40', '周四', NOW()),
('路由交换技术', 4, 7, '实验楼G101', '10:00', '11:40', '周五', NOW()),
('路由交换技术', 4, 7, '实验楼G101', '14:00', '15:40', '周五', NOW()),
('网络攻防实践', 4, 7, '实验楼G102', '08:00', '09:40', '周四', NOW());

-- ============================================
-- 4. 学生数据 (每个班 20-25 人)
-- ============================================
-- 计科2101班 (classId=1)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202101001', '赵阳', '男', 1, '13900001001', 'zhaoyang@stu.smartclass.cn', NOW()),
('202101002', '钱雨', '女', 1, '13900001002', 'qianyu@stu.smartclass.cn', NOW()),
('202101003', '孙晨', '男', 1, '13900001003', 'sunchen@stu.smartclass.cn', NOW()),
('202101004', '李月', '女', 1, '13900001004', 'liyue@stu.smartclass.cn', NOW()),
('202101005', '周明', '男', 1, '13900001005', 'zhouming@stu.smartclass.cn', NOW()),
('202101006', '吴桐', '女', 1, '13900001006', 'wutong@stu.smartclass.cn', NOW()),
('202101007', '郑爽', '男', 1, '13900001007', 'zhengshuang@stu.smartclass.cn', NOW()),
('202101008', '王磊', '男', 1, '13900001008', 'wanglei@stu.smartclass.cn', NOW()),
('202101009', '冯雪', '女', 1, '13900001009', 'fengxue@stu.smartclass.cn', NOW()),
('202101010', '陈宇', '男', 1, '13900001010', 'chenyu@stu.smartclass.cn', NOW()),
('202101011', '褚思', '女', 1, '13900001011', 'chusi@stu.smartclass.cn', NOW()),
('202101012', '卫华', '男', 1, '13900001012', 'weihua@stu.smartclass.cn', NOW()),
('202101013', '蒋涛', '男', 1, '13900001013', 'jiangtao@stu.smartclass.cn', NOW()),
('202101014', '沈佳', '女', 1, '13900001014', 'shenjia@stu.smartclass.cn', NOW()),
('202101015', '韩梅', '女', 1, '13900001015', 'hanmei@stu.smartclass.cn', NOW()),
('202101016', '杨帆', '男', 1, '13900001016', 'yangfan@stu.smartclass.cn', NOW()),
('202101017', '朱红', '女', 1, '13900001017', 'zhuhong@stu.smartclass.cn', NOW()),
('202101018', '秦刚', '男', 1, '13900001018', 'qingang@stu.smartclass.cn', NOW()),
('202101019', '尤娜', '女', 1, '13900001019', 'youna@stu.smartclass.cn', NOW()),
('202101020', '许超', '男', 1, '13900001020', 'xuchao@stu.smartclass.cn', NOW());

-- 计科2201班 (classId=2)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202201001', '张伟', '男', 2, '13900002001', 'zhangwei@stu.smartclass.cn', NOW()),
('202201002', '李娜', '女', 2, '13900002002', 'lina@stu.smartclass.cn', NOW()),
('202201003', '王芳', '女', 2, '13900002003', 'wangfang@stu.smartclass.cn', NOW()),
('202201004', '刘洋', '男', 2, '13900002004', 'liuyang@stu.smartclass.cn', NOW()),
('202201005', '陈静', '女', 2, '13900002005', 'chenjing@stu.smartclass.cn', NOW()),
('202201006', '杨光', '男', 2, '13900002006', 'yangguang@stu.smartclass.cn', NOW()),
('202201007', '赵敏', '女', 2, '13900002007', 'zhaomin@stu.smartclass.cn', NOW()),
('202201008', '黄鹏', '男', 2, '13900002008', 'huangpeng@stu.smartclass.cn', NOW()),
('202201009', '周洁', '女', 2, '13900002009', 'zhoujie@stu.smartclass.cn', NOW()),
('202201010', '吴昊', '男', 2, '13900002010', 'wuhao@stu.smartclass.cn', NOW()),
('202201011', '徐蕾', '女', 2, '13900002011', 'xulei@stu.smartclass.cn', NOW()),
('202201012', '孙涛', '男', 2, '13900002012', 'suntao@stu.smartclass.cn', NOW()),
('202201013', '马丽', '女', 2, '13900002013', 'mali@stu.smartclass.cn', NOW()),
('202201014', '朱军', '男', 2, '13900002014', 'zhujun@stu.smartclass.cn', NOW()),
('202201015', '胡婷', '女', 2, '13900002015', 'huting@stu.smartclass.cn', NOW()),
('202201016', '林峰', '男', 2, '13900002016', 'linfeng@stu.smartclass.cn', NOW()),
('202201017', '何欢', '女', 2, '13900002017', 'hehuan@stu.smartclass.cn', NOW()),
('202201018', '郭靖', '男', 2, '13900002018', 'guojing@stu.smartclass.cn', NOW()),
('202201019', '蔡文', '女', 2, '13900002019', 'caiwen@stu.smartclass.cn', NOW()),
('202201020', '潘龙', '男', 2, '13900002020', 'panlong@stu.smartclass.cn', NOW());

-- 软工2201班 (classId=3)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202202001', '刘磊', '男', 3, '13900003001', 'liulei@stu.smartclass.cn', NOW()),
('202202002', '陈晓', '女', 3, '13900003002', 'chenxiao@stu.smartclass.cn', NOW()),
('202202003', '张杰', '男', 3, '13900003003', 'zhangjie@stu.smartclass.cn', NOW()),
('202202004', '王琳', '女', 3, '13900003004', 'wanglin@stu.smartclass.cn', NOW()),
('202202005', '李强', '男', 3, '13900003005', 'liqiang@stu.smartclass.cn', NOW()),
('202202006', '赵丽', '女', 3, '13900003006', 'zhaoli@stu.smartclass.cn', NOW()),
('202202007', '周鹏', '男', 3, '13900003007', 'zhoupeng@stu.smartclass.cn', NOW()),
('202202008', '吴敏', '女', 3, '13900003008', 'wumin@stu.smartclass.cn', NOW()),
('202202009', '郑龙', '男', 3, '13900003009', 'zhenglong@stu.smartclass.cn', NOW()),
('202202010', '冯洁', '女', 3, '13900003010', 'fengjie@stu.smartclass.cn', NOW()),
('202202011', '陈浩', '男', 3, '13900003011', 'chenhao@stu.smartclass.cn', NOW()),
('202202012', '褚蕾', '女', 3, '13900003012', 'chulei@stu.smartclass.cn', NOW()),
('202202013', '卫峰', '男', 3, '13900003013', 'weifeng@stu.smartclass.cn', NOW()),
('202202014', '蒋丽', '女', 3, '13900003014', 'jiangli@stu.smartclass.cn', NOW()),
('202202015', '沈涛', '男', 3, '13900003015', 'shentao@stu.smartclass.cn', NOW()),
('202202016', '韩磊', '男', 3, '13900003016', 'hanlei@stu.smartclass.cn', NOW()),
('202202017', '杨丽', '女', 3, '13900003017', 'yangli@stu.smartclass.cn', NOW()),
('202202018', '朱文', '男', 3, '13900003018', 'zhuwen@stu.smartclass.cn', NOW());

-- 软工2202班 (classId=4)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202203001', '秦明', '男', 4, '13900004001', 'qinming@stu.smartclass.cn', NOW()),
('202203002', '尤丽', '女', 4, '13900004002', 'youli@stu.smartclass.cn', NOW()),
('202203003', '许刚', '男', 4, '13900004003', 'xugang@stu.smartclass.cn', NOW()),
('202203004', '何洁', '女', 4, '13900004004', 'hejie@stu.smartclass.cn', NOW()),
('202203005', '吕峰', '男', 4, '13900004005', 'lvfeng@stu.smartclass.cn', NOW()),
('202203006', '张悦', '女', 4, '13900004006', 'zhangyue@stu.smartclass.cn', NOW()),
('202203007', '施伟', '男', 4, '13900004007', 'shiwei@stu.smartclass.cn', NOW()),
('202203008', '李萍', '女', 4, '13900004008', 'liping@stu.smartclass.cn', NOW()),
('202203009', '王刚', '男', 4, '13900004009', 'wanggang@stu.smartclass.cn', NOW()),
('202203010', '刘婷', '女', 4, '13900004010', 'liuting@stu.smartclass.cn', NOW()),
('202203011', '陈波', '男', 4, '13900004011', 'chenbo@stu.smartclass.cn', NOW()),
('202203012', '赵霞', '女', 4, '13900004012', 'zhaoxia@stu.smartclass.cn', NOW()),
('202203013', '黄刚', '男', 4, '13900004013', 'huanggang@stu.smartclass.cn', NOW()),
('202203014', '周敏', '女', 4, '13900004014', 'zhoumin@stu.smartclass.cn', NOW()),
('202203015', '徐明', '男', 4, '13900004015', 'xuming@stu.smartclass.cn', NOW()),
('202203016', '孙丽', '女', 4, '13900004016', 'sunli@stu.smartclass.cn', NOW());

-- 人工智能2301班 (classId=5)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202301001', '马超', '男', 5, '13900005001', 'machao@stu.smartclass.cn', NOW()),
('202301002', '林洁', '女', 5, '13900005002', 'linjie@stu.smartclass.cn', NOW()),
('202301003', '高峰', '男', 5, '13900005003', 'gaofeng@stu.smartclass.cn', NOW()),
('202301004', '罗敏', '女', 5, '13900005004', 'luomin@stu.smartclass.cn', NOW()),
('202301005', '梁辉', '男', 5, '13900005005', 'lianghui@stu.smartclass.cn', NOW()),
('202301006', '宋雪', '女', 5, '13900005006', 'songxue@stu.smartclass.cn', NOW()),
('202301007', '唐龙', '男', 5, '13900005007', 'tanglong@stu.smartclass.cn', NOW()),
('202301008', '袁芳', '女', 5, '13900005008', 'yuanfang@stu.smartclass.cn', NOW()),
('202301009', '邓涛', '男', 5, '13900005009', 'dengtao@stu.smartclass.cn', NOW()),
('202301010', '彭丽', '女', 5, '13900005010', 'pengli@stu.smartclass.cn', NOW()),
('202301011', '曹伟', '男', 5, '13900005011', 'caowei@stu.smartclass.cn', NOW()),
('202301012', '曾敏', '女', 5, '13900005012', 'zengmin@stu.smartclass.cn', NOW()),
('202301013', '田峰', '男', 5, '13900005013', 'tianfeng@stu.smartclass.cn', NOW()),
('202301014', '谢瑶', '女', 5, '13900005014', 'xieyao@stu.smartclass.cn', NOW()),
('202301015', '范凯', '男', 5, '13900005015', 'fankai@stu.smartclass.cn', NOW());

-- 大数据2201班 (classId=6)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202204001', '方明', '男', 6, '13900006001', 'fangming@stu.smartclass.cn', NOW()),
('202204002', '任丽', '女', 6, '13900006002', 'renli@stu.smartclass.cn', NOW()),
('202204003', '谭伟', '男', 6, '13900006003', 'tanwei@stu.smartclass.cn', NOW()),
('202204004', '廖洁', '女', 6, '13900006004', 'liaojie@stu.smartclass.cn', NOW()),
('202204005', '邹涛', '男', 6, '13900006005', 'zoutao@stu.smartclass.cn', NOW()),
('202204006', '熊敏', '女', 6, '13900006006', 'xiongmin@stu.smartclass.cn', NOW()),
('202204007', '金辉', '男', 6, '13900006007', 'jinhui@stu.smartclass.cn', NOW()),
('202204008', '陆雪', '女', 6, '13900006008', 'luxue@stu.smartclass.cn', NOW()),
('202204009', '苏龙', '男', 6, '13900006009', 'sulong@stu.smartclass.cn', NOW()),
('202204010', '叶丽', '女', 6, '13900006010', 'yeli@stu.smartclass.cn', NOW()),
('202204011', '魏伟', '男', 6, '13900006011', 'weiwei@stu.smartclass.cn', NOW()),
('202204012', '蒋洁', '女', 6, '13900006012', 'jiangjie@stu.smartclass.cn', NOW());

-- 网工2101班 (classId=7)
INSERT IGNORE INTO `student` (student_no, name, gender, class_id, phone, email, create_time) VALUES
('202102001', '丁强', '男', 7, '13900007001', 'dingqiang@stu.smartclass.cn', NOW()),
('202102002', '姚敏', '女', 7, '13900007002', 'yaomin@stu.smartclass.cn', NOW()),
('202102003', '卢峰', '男', 7, '13900007003', 'lufeng@stu.smartclass.cn', NOW()),
('202102004', '石洁', '女', 7, '13900007004', 'shijie@stu.smartclass.cn', NOW()),
('202102005', '崔涛', '男', 7, '13900007005', 'cuitao@stu.smartclass.cn', NOW()),
('202102006', '龚丽', '女', 7, '13900007006', 'gongli@stu.smartclass.cn', NOW()),
('202102007', '程明', '男', 7, '13900007007', 'chengming@stu.smartclass.cn', NOW()),
('202102008', '段雪', '女', 7, '13900007008', 'duanxue@stu.smartclass.cn', NOW()),
('202102009', '侯伟', '男', 7, '13900007009', 'houwei@stu.smartclass.cn', NOW()),
('202102010', '孟丽', '女', 7, '13900007010', 'mengli@stu.smartclass.cn', NOW()),
('202102011', '田辉', '男', 7, '13900007011', 'tianhui@stu.smartclass.cn', NOW()),
('202102012', '贺洁', '女', 7, '13900007012', 'hejie@stu.smartclass.cn', NOW());

-- ============================================
-- 5. 考勤数据 (最近14天的抽样数据)
-- ============================================
-- 为计科2201班和软工2201班生成考勤记录
-- 使用存储过程思路的批量插入，这里直接用显式数据

-- 今天和过去14天的日期参考 (2026-05-18 为当前日期)
-- 这里生成 5/5 - 5/18 期间的考勤数据

-- 计科2201班: 数据结构 (周一至周五上课)
-- 选几个典型日期生成考勤
INSERT IGNORE INTO `attendance` (student_id, course_id, class_id, attendance_date, status, check_in_time, confidence, remark, create_time)
SELECT
  s.id, c.id, 2,
  d.att_date,
  CASE
    WHEN RAND() < 0.75 THEN 'present'
    WHEN RAND() < 0.90 THEN 'late'
    WHEN RAND() < 0.95 THEN 'leave'
    ELSE 'absent'
  END,
  CASE WHEN RAND() < 0.85 THEN TIMESTAMP(d.att_date, ADDTIME(c.start_time, SEC_TO_TIME(FLOOR(RAND() * 900) - 300))) ELSE NULL END,
  ROUND(0.80 + RAND() * 0.19, 2),
  CASE
    WHEN RAND() < 0.75 THEN '人脸识别签到'
    WHEN RAND() < 0.90 THEN '二维码签到'
    ELSE NULL
  END,
  NOW()
FROM student s
CROSS JOIN course c
CROSS JOIN (
  SELECT '2026-05-12' AS att_date
  UNION SELECT '2026-05-13' UNION SELECT '2026-05-14' UNION SELECT '2026-05-15'
  UNION SELECT '2026-05-16' UNION SELECT '2026-05-18' UNION SELECT '2026-05-19'
  UNION SELECT '2026-05-20'
) d
WHERE s.class_id = 2
  AND c.class_id = 2
  AND DAYOFWEEK(d.att_date) = CASE c.week_day
    WHEN '周一' THEN 2 WHEN '周二' THEN 3 WHEN '周三' THEN 4
    WHEN '周四' THEN 5 WHEN '周五' THEN 6 WHEN '周六' THEN 7 WHEN '周日' THEN 1
  END;

-- 软工2201班: 考勤数据
INSERT IGNORE INTO `attendance` (student_id, course_id, class_id, attendance_date, status, check_in_time, confidence, remark, create_time)
SELECT
  s.id, c.id, 3,
  d.att_date,
  CASE
    WHEN RAND() < 0.72 THEN 'present'
    WHEN RAND() < 0.88 THEN 'late'
    WHEN RAND() < 0.94 THEN 'leave'
    ELSE 'absent'
  END,
  CASE WHEN RAND() < 0.80 THEN TIMESTAMP(d.att_date, ADDTIME(c.start_time, SEC_TO_TIME(FLOOR(RAND() * 900) - 300))) ELSE NULL END,
  ROUND(0.78 + RAND() * 0.21, 2),
  CASE
    WHEN RAND() < 0.70 THEN '人脸识别签到'
    WHEN RAND() < 0.95 THEN '二维码签到'
    ELSE NULL
  END,
  NOW()
FROM student s
CROSS JOIN course c
CROSS JOIN (
  SELECT '2026-05-12' AS att_date
  UNION SELECT '2026-05-13' UNION SELECT '2026-05-14' UNION SELECT '2026-05-15'
  UNION SELECT '2026-05-16' UNION SELECT '2026-05-18' UNION SELECT '2026-05-19'
  UNION SELECT '2026-05-20'
) d
WHERE s.class_id = 3
  AND c.class_id = 3
  AND DAYOFWEEK(d.att_date) = CASE c.week_day
    WHEN '周一' THEN 2 WHEN '周二' THEN 3 WHEN '周三' THEN 4
    WHEN '周四' THEN 5 WHEN '周五' THEN 6 WHEN '周六' THEN 7 WHEN '周日' THEN 1
  END;

-- AI2301 班最近几天考勤
INSERT IGNORE INTO `attendance` (student_id, course_id, class_id, attendance_date, status, check_in_time, confidence, remark, create_time)
SELECT
  s.id, c.id, 5,
  d.att_date,
  CASE
    WHEN RAND() < 0.80 THEN 'present'
    WHEN RAND() < 0.92 THEN 'late'
    ELSE 'absent'
  END,
  CASE WHEN RAND() < 0.85 THEN TIMESTAMP(d.att_date, ADDTIME(c.start_time, SEC_TO_TIME(FLOOR(RAND() * 900) - 200))) ELSE NULL END,
  ROUND(0.82 + RAND() * 0.17, 2),
  '人脸识别签到',
  NOW()
FROM student s
CROSS JOIN course c
CROSS JOIN (
  SELECT '2026-05-12' AS att_date UNION SELECT '2026-05-13'
  UNION SELECT '2026-05-14' UNION SELECT '2026-05-15' UNION SELECT '2026-05-16'
  UNION SELECT '2026-05-18'
) d
WHERE s.class_id = 5
  AND c.class_id = 5
  AND DAYOFWEEK(d.att_date) = CASE c.week_day
    WHEN '周一' THEN 2 WHEN '周二' THEN 3 WHEN '周三' THEN 4
    WHEN '周四' THEN 5 WHEN '周五' THEN 6 WHEN '周六' THEN 7 WHEN '周日' THEN 1
  END;

-- ============================================
-- 6. 行为记录数据 (抽样)
-- ============================================
INSERT IGNORE INTO `behavior` (student_id, class_id, course_id, behavior_type, behavior_time, confidence, image_path, handled, handle_remark, create_time)
SELECT
  s.id, s.class_id,
  (SELECT c2.id FROM course c2 WHERE c2.class_id = s.class_id ORDER BY RAND() LIMIT 1),
  bt.type,
  TIMESTAMP('2026-05-18', ADDTIME('08:00:00', SEC_TO_TIME(FLOOR(RAND() * 28800)))),
  ROUND(0.70 + RAND() * 0.29, 2),
  NULL,
  CASE WHEN RAND() < 0.4 THEN 1 ELSE 0 END,
  CASE WHEN RAND() < 0.4 THEN '已提醒学生注意课堂纪律' ELSE NULL END,
  NOW()
FROM student s
CROSS JOIN (
  SELECT 'raising_hand' AS type UNION SELECT 'reading' UNION SELECT 'writing'
  UNION SELECT 'using_phone' UNION SELECT 'bowing_head' UNION SELECT 'leaning_over'
) bt
WHERE s.class_id IN (2, 3, 5)
  AND RAND() < 0.12;

-- 再补充一些今天的行为记录，让实时监控有数据
INSERT IGNORE INTO `behavior` (student_id, class_id, course_id, behavior_type, behavior_time, confidence, image_path, handled, handle_remark, create_time)
SELECT
  s.id, s.class_id,
  (SELECT c2.id FROM course c2 WHERE c2.class_id = s.class_id ORDER BY RAND() LIMIT 1),
  bt.type,
  TIMESTAMP('2026-05-18', ADDTIME(CASE bt.slot WHEN 1 THEN '08:30:00' WHEN 2 THEN '10:15:00' WHEN 3 THEN '14:30:00' ELSE '16:00:00' END, SEC_TO_TIME(FLOOR(RAND() * 600)))),
  ROUND(0.72 + RAND() * 0.27, 2),
  NULL,
  CASE WHEN RAND() < 0.3 THEN 1 ELSE 0 END,
  NULL,
  NOW()
FROM student s
CROSS JOIN (
  SELECT 'using_phone' AS type, 1 AS slot
  UNION SELECT 'bowing_head', 1 UNION SELECT 'leaning_over', 2
  UNION SELECT 'using_phone', 2 UNION SELECT 'raising_hand', 3
  UNION SELECT 'reading', 3 UNION SELECT 'writing', 3
  UNION SELECT 'using_phone', 4 UNION SELECT 'bowing_head', 4
) bt
WHERE s.class_id IN (2, 3)
  AND RAND() < 0.20;

-- ============================================
-- 7. 请假数据
-- ============================================
INSERT IGNORE INTO `leave_request` (student_id, course_id, class_id, leave_type, start_date, end_date, reason, status, approver_id, approve_remark, approve_time, create_time)
SELECT
  s.id,
  (SELECT c.id FROM course c WHERE c.class_id = s.class_id ORDER BY RAND() LIMIT 1),
  s.class_id,
  CASE WHEN RAND() < 0.5 THEN 'sick' ELSE 'personal' END,
  '2026-05-19',
  CASE WHEN RAND() < 0.6 THEN '2026-05-19' ELSE '2026-05-20' END,
  CASE WHEN RAND() < 0.5 THEN '身体不适，需要就医' ELSE '家中有急事需要处理' END,
  CASE WHEN RAND() < 0.4 THEN 'pending' WHEN RAND() < 0.7 THEN 'approved' ELSE 'rejected' END,
  2,
  CASE WHEN RAND() < 0.7 THEN '批准，注意补课' WHEN RAND() < 0.9 THEN '驳回，课程重要请安排好时间' ELSE NULL END,
  CASE WHEN RAND() < 0.6 THEN NOW() ELSE NULL END,
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 5) DAY)
FROM student s
WHERE s.class_id IN (2, 3, 5)
  AND RAND() < 0.15;

-- ============================================
-- 8. 通知数据
-- ============================================
INSERT IGNORE INTO `notification` (user_id, type, title, content, is_read, ref_id, create_time) VALUES
(1, 'system', '系统初始化完成', '智课考勤系统已成功部署，所有服务运行正常', 1, NULL, NOW()),
(1, 'attendance', '今日考勤统计', '今日全年级出勤率 91.2%，迟到率 5.8%，缺勤率 3.0%', 0, NULL, NOW()),
(1, 'behavior', '待处理行为预警', '有 3 条新的异常行为记录等待处理', 0, NULL, NOW()),
(2, 'leave', '新的请假申请', '学生周明提交了请假申请，请及时审批', 0, NULL, NOW()),
(2, 'system', '版本更新通知', '系统已更新至 V1.1，新增二维码签到和通知功能', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'attendance', '考勤异常提醒', '软件工程2201班今日有2名学生缺勤', 0, NULL, NOW()),
(4, 'behavior', '行为预警提醒', '您的课程中检测到学生异常行为，请查看详情', 1, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'system', '欢迎使用智课考勤', '您好，欢迎使用智课考勤系统！请完善您的个人资料', 1, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY));

COMMIT;

-- ============================================
-- 执行完毕后验证数据
-- ============================================
SELECT '用户数' AS 统计项, COUNT(*) AS 数量 FROM `user`
UNION ALL SELECT '班级数', COUNT(*) FROM `class`
UNION ALL SELECT '课程数', COUNT(*) FROM `course`
UNION ALL SELECT '学生数', COUNT(*) FROM `student`
UNION ALL SELECT '考勤记录数', COUNT(*) FROM `attendance`
UNION ALL SELECT '行为记录数', COUNT(*) FROM `behavior`
UNION ALL SELECT '请假申请数', COUNT(*) FROM `leave_request`
UNION ALL SELECT '通知数', COUNT(*) FROM `notification`;

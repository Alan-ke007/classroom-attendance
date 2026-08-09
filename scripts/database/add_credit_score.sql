-- Migration: 添加学风分字段到student表
ALTER TABLE `student`
    ADD COLUMN `credit_score` INT DEFAULT 100 COMMENT '学风分（0-200，初始100）',
    ADD COLUMN `credit_earned` INT DEFAULT 0 COMMENT '累计加分',
    ADD COLUMN `credit_deducted` INT DEFAULT 0 COMMENT '累计扣分';

-- attendance 表新增人脸核验相关列（PRD §2.4，向后兼容，可空新增列）
-- 落库脚本（本地有 MySQL 时手动执行；不自动执行）
-- 说明：
--   face_status 复用现有 confidence(BigDecimal) 作 face_confidence（无需新增列）
--   face_status 默认 NULL（历史记录视为未核验），新增签到按 F8/F9 填 VERIFIED/NEED_REVIEW
-- 回滚（如需）：ALTER TABLE attendance DROP COLUMN face_status, DROP COLUMN face_source;

ALTER TABLE attendance
  ADD COLUMN face_status VARCHAR(20) NULL COMMENT 'VERIFIED/NEED_REVIEW/REJECTED',
  ADD COLUMN face_source VARCHAR(20) NULL COMMENT '人脸核验来源标记，如 miniapp_checkin';

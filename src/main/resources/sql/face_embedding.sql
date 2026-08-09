-- 人脸特征库（PII，仅后端可读写；算法服务零 PII 持久化）
-- 落库脚本（本地有 MySQL 时手动执行；不自动执行）
-- 对应实体：com.classroom.attendance.modules.face.entity.FaceEmbedding
-- 说明：student_id 为主键且唯一，保证 1:1 比对时取本人唯一档案（PRD F4）。
-- embedding = 512×float32 小端 = 2048 字节（与 InsightFace buffalo_l 输出 tobytes() 对应）。

CREATE TABLE IF NOT EXISTS face_embedding (
  student_id    BIGINT          NOT NULL,
  embedding     VARBINARY(2048) NOT NULL,
  face_count    TINYINT         NOT NULL DEFAULT 1,
  source        VARCHAR(20)     NOT NULL DEFAULT 'enroll',
  model_version VARCHAR(32)     NULL,
  image_ref     VARCHAR(255)    NULL,
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       INT(1)          NOT NULL DEFAULT 0,
  PRIMARY KEY (student_id),
  UNIQUE KEY uk_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人脸特征库（PII，仅后端可读写）';

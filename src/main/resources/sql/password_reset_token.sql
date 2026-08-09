-- 密码重置令牌表（C4 安全加固：服务端哈希存储，一次性 + 15 分钟 TTL）
-- 落库脚本（本地有 MySQL 时手动执行；不自动执行）
-- 说明：
--   token_hash = SHA-256(原始令牌)，服务端只存哈希，原始令牌不下发前端
--   used = 0 未用 / 1 已用（一次性，消费后立即作废）
--   expiry 过期时间（生成时 now + 15m）
-- 回滚（如需）：DROP TABLE password_reset_token;

CREATE TABLE IF NOT EXISTS password_reset_token (
  id          BIGINT      NOT NULL AUTO_INCREMENT,
  username    VARCHAR(64) NOT NULL,
  token_hash  VARCHAR(64) NOT NULL,
  expiry      DATETIME    NOT NULL,
  used        TINYINT     NOT NULL DEFAULT 0,
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_token_hash (token_hash),
  KEY idx_username (username),
  KEY idx_expiry (expiry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密码重置令牌（C4，哈希存储，一次性）';

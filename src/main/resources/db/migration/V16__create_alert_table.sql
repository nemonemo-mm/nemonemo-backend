-- V16__create_alert_table.sql
-- 앱 내 알림(알림함) 저장용 alert 테이블 생성

CREATE TABLE alert (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    team_id     BIGINT REFERENCES team(id) ON DELETE CASCADE,
    type        VARCHAR(50) NOT NULL,
    title       VARCHAR(100),
    body        TEXT NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alert_user_created_at
    ON alert(user_id, created_at DESC);



-- V17__add_alert_read_at.sql
-- alert 테이블에 읽은 시각(read_at) 컬럼 추가

ALTER TABLE alert
    ADD COLUMN IF NOT EXISTS read_at TIMESTAMPTZ;



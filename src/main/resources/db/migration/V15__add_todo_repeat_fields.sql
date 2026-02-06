-- V15__add_todo_repeat_fields.sql
-- todo 테이블에 반복 일정 관련 필드 추가

ALTER TABLE todo
    ADD COLUMN IF NOT EXISTS repeat_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS repeat_interval INTEGER DEFAULT 1,
    ADD COLUMN IF NOT EXISTS repeat_days INTEGER[],
    ADD COLUMN IF NOT EXISTS repeat_month_day INTEGER,
    ADD COLUMN IF NOT EXISTS repeat_end_date TIMESTAMPTZ;

COMMENT ON COLUMN todo.repeat_type IS '반복 유형 (NONE, DAILY, WEEKLY, MONTHLY, YEARLY)';
COMMENT ON COLUMN todo.repeat_interval IS '반복 간격 (일/주/개월/년 단위)';
COMMENT ON COLUMN todo.repeat_days IS '반복 요일 (0=일,1=월,...) - WEEKLY에서 사용';
COMMENT ON COLUMN todo.repeat_month_day IS '월간/연간 반복 시 날짜 (1-31)';
COMMENT ON COLUMN todo.repeat_end_date IS '반복 종료일 (없으면 무기한)';



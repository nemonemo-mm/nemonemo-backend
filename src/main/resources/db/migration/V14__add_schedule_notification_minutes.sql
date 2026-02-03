-- V14__add_schedule_notification_minutes.sql
-- 스케줄 테이블에 알림 시간 필드 추가

ALTER TABLE schedule
    ADD COLUMN IF NOT EXISTS notification_minutes INTEGER[];

COMMENT ON COLUMN schedule.notification_minutes IS '스케줄 사전 알림 시간 (분 단위 배열, 예: [10, 30, 60])';


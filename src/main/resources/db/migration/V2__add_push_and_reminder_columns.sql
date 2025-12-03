-- V2__add_push_and_reminder_columns.sql
-- 스키마 변경: ENUM 확장, 컬럼 추가, 테이블 제거 등

-- =========================================================
-- 1. ENUM 타입 변경: auth_provider 에 'APPLE' 추가
-- =========================================================
ALTER TYPE auth_provider ADD VALUE IF NOT EXISTS 'APPLE';

-- =========================================================
-- 2. app_user 테이블 컬럼 추가
--    enable_push_notification BOOLEAN NOT NULL DEFAULT TRUE
-- =========================================================
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS enable_push_notification BOOLEAN NOT NULL DEFAULT TRUE;

-- =========================================================
-- 3. schedule 테이블 수정
--    - place VARCHAR(255) NULL
--    - reminder_offset_minutes INTEGER NULL
-- =========================================================
ALTER TABLE schedule
    ADD COLUMN IF NOT EXISTS place VARCHAR(255),
    ADD COLUMN IF NOT EXISTS reminder_offset_minutes INTEGER;

-- =========================================================
-- 4. todo 테이블 수정
--    - reminder_offset_minutes INTEGER NULL
--    - assignee_member_id 는 여전히 NULL 허용 (기존 정의 유지, 제약 추가 없음)
-- =========================================================
ALTER TABLE todo
    ADD COLUMN IF NOT EXISTS reminder_offset_minutes INTEGER;

-- =========================================================
-- 5. todo_schedule_link 테이블 제거
-- =========================================================
DROP TABLE IF EXISTS todo_schedule_link;

-- =========================================================
-- 6. notification_setting 테이블 수정
--    - enable_team_alarm BOOLEAN NOT NULL DEFAULT TRUE
--    - enable_todo_alarm BOOLEAN NOT NULL DEFAULT TRUE
--    기존 컬럼/제약은 그대로 유지
-- =========================================================
ALTER TABLE notification_setting
    ADD COLUMN IF NOT EXISTS enable_team_alarm BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS enable_todo_alarm BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE notification_setting
    DROP COLUMN IF EXISTS enable_due_alarm;

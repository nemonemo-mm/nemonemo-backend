-- V3__add_notification_settings.sql
-- 알림 설정 관련 테이블 및 필드 추가
-- 개인 알림 설정, 팀 알림 설정 확장, 디바이스 토큰 관리

-- =========================================================
-- 1. 개인 알림 설정 (personal_notification_setting) 테이블 생성
-- =========================================================

CREATE TABLE personal_notification_setting (
    id                                 BIGSERIAL PRIMARY KEY,
    user_id                            BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    enable_all_personal_notifications  BOOLEAN NOT NULL DEFAULT FALSE,
    enable_schedule_change_notification BOOLEAN NOT NULL DEFAULT TRUE,
    enable_schedule_pre_notification   BOOLEAN NOT NULL DEFAULT FALSE,
    schedule_pre_notification_minutes  INTEGER[],  -- 10, 30, 60 (분)
    enable_todo_change_notification   BOOLEAN NOT NULL DEFAULT TRUE,
    enable_todo_deadline_notification BOOLEAN NOT NULL DEFAULT FALSE,
    todo_deadline_notification_minutes INTEGER[],  -- 10, 30, 60 (분)
    enable_notice_notification        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_personal_notification_setting_user UNIQUE (user_id)
);

-- =========================================================
-- 2. 팀 알림 설정 (notification_setting) 테이블 확장
-- =========================================================

-- 새로운 필드 추가
ALTER TABLE notification_setting
    ADD COLUMN IF NOT EXISTS enable_schedule_change_notification BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS enable_schedule_pre_notification BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS schedule_pre_notification_minutes INTEGER[],
    ADD COLUMN IF NOT EXISTS enable_todo_change_notification BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS enable_todo_deadline_notification BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS todo_deadline_notification_minutes INTEGER[],
    ADD COLUMN IF NOT EXISTS enable_team_member_notification BOOLEAN NOT NULL DEFAULT TRUE;

-- =========================================================
-- 3. 디바이스 토큰 (device_token) 테이블 생성
-- =========================================================

CREATE TABLE device_token (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    device_token VARCHAR(500) NOT NULL,
    device_type  VARCHAR(20),
    device_info  VARCHAR(255),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_device_token_token UNIQUE (device_token)
);

-- 사용자별 디바이스 토큰 조회를 위한 인덱스
CREATE INDEX idx_device_token_user_id ON device_token(user_id);



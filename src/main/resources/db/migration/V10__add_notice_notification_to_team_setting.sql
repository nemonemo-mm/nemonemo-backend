-- V10__add_notice_notification_to_team_setting.sql
-- 팀별 알림 설정에 공지 알림 필드 추가

ALTER TABLE notification_setting
    ADD COLUMN enable_notice_notification BOOLEAN NOT NULL DEFAULT TRUE;


-- V11__add_performance_indexes.sql
-- 성능 최적화를 위한 인덱스 추가

-- =========================================================
-- 1. Notice 테이블 인덱스
-- =========================================================

-- 팀별 공지 조회 최적화 (가장 자주 사용되는 쿼리)
CREATE INDEX IF NOT EXISTS idx_notice_team_id 
    ON notice(team_id);

-- 공지 생성일 기준 정렬 최적화
CREATE INDEX IF NOT EXISTS idx_notice_created_at_desc 
    ON notice(created_at DESC);

-- 팀별 + 생성일 기준 복합 인덱스 (가장 효율적)
CREATE INDEX IF NOT EXISTS idx_notice_team_created_at 
    ON notice(team_id, created_at DESC);

-- 작성자별 공지 조회 최적화
CREATE INDEX IF NOT EXISTS idx_notice_author_id 
    ON notice(author_id);

-- =========================================================
-- 2. TeamMember 테이블 인덱스
-- =========================================================

-- 팀별 멤버 조회 최적화
CREATE INDEX IF NOT EXISTS idx_team_member_team_id 
    ON team_member(team_id);

-- 사용자별 팀 조회 최적화
CREATE INDEX IF NOT EXISTS idx_team_member_user_id 
    ON team_member(user_id);

-- 팀 + 사용자 복합 인덱스 (팀원 확인 쿼리 최적화)
CREATE INDEX IF NOT EXISTS idx_team_member_team_user 
    ON team_member(team_id, user_id);

-- =========================================================
-- 3. Notification Setting 인덱스
-- =========================================================

-- 사용자별 + 팀별 알림 설정 조회 최적화
CREATE INDEX IF NOT EXISTS idx_notification_setting_user_team 
    ON notification_setting(user_id, team_id);

-- =========================================================
-- 4. Device Token 인덱스
-- =========================================================

-- 사용자별 디바이스 토큰 조회 최적화
CREATE INDEX IF NOT EXISTS idx_device_token_user_id 
    ON device_token(user_id);


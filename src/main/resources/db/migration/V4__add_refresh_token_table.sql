-- V4__add_refresh_token_table.sql
-- 리프레시 토큰 관리용 테이블 추가
-- - 소셜 로그인 / 이메일 로그인 공통으로 사용 가능
-- - 토큰 재발급, 로그아웃 시 이 테이블을 기준으로 검증/무효화

-- =========================================================
-- 1. refresh_token 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS refresh_token (
    id           BIGSERIAL PRIMARY KEY,
    -- 어떤 유저의 리프레시 토큰인지
    user_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    -- 실제 리프레시 토큰 문자열 (또는 해시 값)
    token        VARCHAR(255) NOT NULL,
    -- (선택) 디바이스 정보: OS, 앱 버전, 디바이스 이름 등
    device_info  VARCHAR(255),
    -- 토큰 만료 시각
    expires_at   TIMESTAMPTZ NOT NULL,
    -- 생성 시각
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 한 유저가 동일한 토큰 문자열을 중복 저장하지 않도록 유니크 제약
CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_token_user_token
    ON refresh_token(user_id, token);



-- =========================================================
-- 2. app_user 테이블 수정 
-- 앱 알림 설정 컬럼 삭제
-- =========================================================
ALTER TABLE app_user
    DROP COLUMN IF EXISTS enable_push_notification;

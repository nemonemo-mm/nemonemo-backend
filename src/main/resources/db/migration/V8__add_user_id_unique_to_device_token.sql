-- V8__add_user_id_unique_to_device_token.sql
-- 단일 디바이스 토큰 지원: 사용자당 1개의 토큰만 유지하도록 변경

-- =========================================================
-- 1. 기존 중복 데이터 정리: 같은 user_id에 여러 토큰이 있는 경우 최신 것만 유지
-- =========================================================

-- updated_at이 가장 최신인 토큰만 남기고 나머지 삭제
DELETE FROM device_token dt1
WHERE EXISTS (
    SELECT 1
    FROM device_token dt2
    WHERE dt2.user_id = dt1.user_id
      AND dt2.updated_at > dt1.updated_at
);

-- updated_at이 같고 created_at이 더 최신인 경우 처리
DELETE FROM device_token dt1
WHERE EXISTS (
    SELECT 1
    FROM device_token dt2
    WHERE dt2.user_id = dt1.user_id
      AND dt2.updated_at = dt1.updated_at
      AND dt2.created_at > dt1.created_at
);

-- =========================================================
-- 2. user_id에 대한 UNIQUE 제약조건 추가
-- =========================================================

ALTER TABLE device_token
    ADD CONSTRAINT uq_device_token_user_id UNIQUE (user_id);































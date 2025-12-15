-- V2__alter_column_lengths.sql
-- 필드 길이 제한 변경 마이그레이션
-- 사용자 이름, 팀 이름, 닉네임, 포지션 이름을 10자로 제한
-- 이미지 URL 및 일반 URL을 1000자로 확장

-- =========================================================
-- 1. 사용자 (app_user) 테이블
-- =========================================================

-- 사용자 이름: 100자 → 10자
ALTER TABLE app_user 
    ALTER COLUMN name TYPE VARCHAR(10);

-- 이미지 URL: 255자 → 1000자
ALTER TABLE app_user 
    ALTER COLUMN image_url TYPE VARCHAR(1000);

-- =========================================================
-- 2. 팀 (team) 테이블
-- =========================================================

-- 팀 이름: 100자 → 10자
ALTER TABLE team 
    ALTER COLUMN name TYPE VARCHAR(10);

-- 초대 코드: 50자 → 20자
ALTER TABLE team 
    ALTER COLUMN invite_code TYPE VARCHAR(20);

-- 이미지 URL: 255자 → 1000자
ALTER TABLE team 
    ALTER COLUMN image_url TYPE VARCHAR(1000);

-- =========================================================
-- 3. 팀 멤버 (team_member) 테이블
-- =========================================================

-- 닉네임: 100자 → 10자
ALTER TABLE team_member 
    ALTER COLUMN nickname TYPE VARCHAR(10);

-- =========================================================
-- 4. 일정 (schedule) 테이블
-- =========================================================

-- 장소: 255자 → 100자
ALTER TABLE schedule 
    ALTER COLUMN place TYPE VARCHAR(100);

-- URL: 500자 → 1000자
ALTER TABLE schedule 
    ALTER COLUMN url TYPE VARCHAR(1000);

-- =========================================================
-- 5. 투두 (todo) 테이블
-- =========================================================

-- URL: 500자 → 1000자
ALTER TABLE todo 
    ALTER COLUMN url TYPE VARCHAR(1000);


-- V12__remove_default_positions.sql
-- 기본 포지션(MEMBER) 제거 및 관련 데이터 정리

-- =========================================================
-- 1. 기본 포지션을 사용하는 팀 멤버들의 포지션을 null로 변경
-- =========================================================

UPDATE team_member
SET position_id = NULL
WHERE position_id IN (
    SELECT id 
    FROM position 
    WHERE is_default = true OR name = 'MEMBER'
);

-- =========================================================
-- 2. 기본 포지션 삭제
-- =========================================================

DELETE FROM position
WHERE is_default = true OR name = 'MEMBER';


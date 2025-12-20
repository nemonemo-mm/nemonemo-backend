-- V9__update_todo_schema.sql
-- 투두 테이블 스키마 업데이트
-- 워크플로우에 맞게 place 필드 추가, start_at, due_at 제거

-- =========================================================
-- 1. 투두 (todo) 테이블
-- =========================================================

-- place 필드 추가
ALTER TABLE todo 
    ADD COLUMN IF NOT EXISTS place VARCHAR(100);

-- start_at 컬럼 제거 (워크플로우에 없음)
ALTER TABLE todo 
    DROP COLUMN IF EXISTS start_at;

-- due_at 컬럼 제거 (워크플로우에 없음)
ALTER TABLE todo 
    DROP COLUMN IF EXISTS due_at;

-- end_at을 NOT NULL로 변경 (필수 필드)
ALTER TABLE todo 
    ALTER COLUMN end_at SET NOT NULL;







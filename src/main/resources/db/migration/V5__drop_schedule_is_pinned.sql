-- V5__cleanup_unused_columns.sql
-- 사용하지 않는 컬럼 정리

-- 1) 일정 고정 여부 컬럼 제거
ALTER TABLE schedule
    DROP COLUMN IF EXISTS is_pinned;

-- 2) 일정 리마인더 오프셋 컬럼 제거
ALTER TABLE schedule
    DROP COLUMN IF EXISTS reminder_offset_minutes;

-- 3) 투두 리마인더 오프셋 컬럼 제거
ALTER TABLE todo
    DROP COLUMN IF EXISTS reminder_offset_minutes;

-- 4) 월간 n째 주 요일 반복 관련 컬럼 제거
ALTER TABLE schedule
    DROP COLUMN IF EXISTS repeat_week_ordinal;

ALTER TABLE schedule
    DROP COLUMN IF EXISTS repeat_week_day;

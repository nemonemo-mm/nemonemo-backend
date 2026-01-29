-- V6__shrink_title_length_to_20.sql
-- 일정/투두 제목 길이를 20자로 축소

ALTER TABLE schedule
    ALTER COLUMN title TYPE VARCHAR(20);

ALTER TABLE todo
    ALTER COLUMN title TYPE VARCHAR(20);






























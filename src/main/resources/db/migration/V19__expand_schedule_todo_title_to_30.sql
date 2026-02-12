-- V19__expand_schedule_todo_title_to_30.sql
-- 일정/투두 제목 길이를 30자로 확장

ALTER TABLE schedule
    ALTER COLUMN title TYPE VARCHAR(30);

ALTER TABLE todo
    ALTER COLUMN title TYPE VARCHAR(30);

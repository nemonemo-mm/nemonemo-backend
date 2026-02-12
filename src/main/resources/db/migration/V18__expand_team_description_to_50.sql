-- V18__expand_team_description_to_50.sql
-- 팀 소개(description) 길이를 50자로 확장

ALTER TABLE team
    ALTER COLUMN description TYPE VARCHAR(50);

-- V7__shrink_team_description_to_20.sql
-- 팀 소개(description) 길이를 20자로 축소

ALTER TABLE team
    ALTER COLUMN description TYPE VARCHAR(20);




































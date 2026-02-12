-- V20__increase_position_limit_to_8.sql
-- 팀당 포지션 최대 개수를 8개로 확장

CREATE OR REPLACE FUNCTION check_position_count()
RETURNS TRIGGER AS $$
DECLARE
    position_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO position_count
    FROM position
    WHERE team_id = NEW.team_id;
    
    IF position_count + 1 > 8 THEN
        RAISE EXCEPTION '팀당 포지션은 최대 8개까지 추가할 수 있습니다.';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

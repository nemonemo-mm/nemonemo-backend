-- V4__schedule_todo_positions.sql
-- 스케줄/투두 포지션 연결 테이블 및 개인 조회용 인덱스 추가

-- =========================================================
-- 1. 스케줄 - 포지션 조인 테이블
-- =========================================================

CREATE TABLE schedule_position (
    schedule_id BIGINT NOT NULL REFERENCES schedule(id) ON DELETE CASCADE,
    position_id BIGINT NOT NULL REFERENCES position(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    PRIMARY KEY (schedule_id, position_id)
);

-- =========================================================
-- 2. 투두 - 포지션 조인 테이블
-- =========================================================

CREATE TABLE todo_position (
    todo_id    BIGINT NOT NULL REFERENCES todo(id) ON DELETE CASCADE,
    position_id BIGINT NOT NULL REFERENCES position(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    PRIMARY KEY (todo_id, position_id)
);

-- =========================================================
-- 3. 개인 일정/투두 조회 최적화를 위한 인덱스
-- =========================================================

CREATE INDEX IF NOT EXISTS idx_schedule_attendee_member_id
    ON schedule_attendee(member_id);

CREATE INDEX IF NOT EXISTS idx_todo_attendee_member_id
    ON todo_attendee(member_id);



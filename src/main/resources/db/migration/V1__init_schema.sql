-- V1__init_schema.sql
-- 최종 데이터베이스 스키마 초기화
-- PostgreSQL + Flyway용

-- =========================================================
-- 0. 공통 ENUM 타입 정의
-- =========================================================

-- 투두 상태: TODO / DONE
CREATE TYPE todo_status AS ENUM ('TODO', 'DONE');

-- 인증 제공자: 로컬(이메일+비번), 구글, 애플 로그인
CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE', 'APPLE');


-- =========================================================
-- 1. 사용자 (계정/인증)
-- =========================================================
CREATE TABLE app_user (
    id               BIGSERIAL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255),
    name             VARCHAR(100) NOT NULL,
    provider         auth_provider NOT NULL DEFAULT 'LOCAL',
    provider_id      VARCHAR(255),
    image_url        VARCHAR(255),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- =========================================================
-- 2. 팀 / 그룹
-- =========================================================

-- 2-1) 팀(그룹) 테이블
CREATE TABLE team (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    invite_code           VARCHAR(50) NOT NULL UNIQUE,
    owner_id              BIGINT NOT NULL REFERENCES app_user(id),
    description           TEXT,
    image_url             VARCHAR(255),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2-2) 팀 내 포지션 (직군)
CREATE TABLE position (
    id               BIGSERIAL PRIMARY KEY,
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    name             VARCHAR(10) NOT NULL,
    color_hex        VARCHAR(9),
    is_default       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_position_team_name UNIQUE (team_id, name),
    CONSTRAINT chk_position_name_length CHECK (LENGTH(name) <= 10)
);

-- 팀당 포지션 최대 6개 제약조건 (기본값 MEMBER 포함하면 7개)
CREATE OR REPLACE FUNCTION check_position_count()
RETURNS TRIGGER AS $$
DECLARE
    position_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO position_count
    FROM position
    WHERE team_id = NEW.team_id;
    
    IF position_count + 1 > 7 THEN
        RAISE EXCEPTION '팀당 포지션은 최대 6개까지 추가할 수 있습니다. (기본값 MEMBER 포함 시 7개)';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_position_count
    BEFORE INSERT ON position
    FOR EACH ROW
    EXECUTE FUNCTION check_position_count();

-- 2-3) 팀 멤버 테이블
CREATE TABLE team_member (
    id               BIGSERIAL PRIMARY KEY,
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    user_id          BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    nickname         VARCHAR(100),
    position_id      BIGINT REFERENCES position(id),
    is_admin         BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_team_member_team_user UNIQUE (team_id, user_id)
);


-- =========================================================
-- 3. 일정 / 마감 (Schedule)
-- =========================================================

CREATE TABLE schedule (
    id                   BIGSERIAL PRIMARY KEY,
    team_id              BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    title                VARCHAR(200) NOT NULL,
    description          TEXT,
    place                VARCHAR(255),
    start_at             TIMESTAMPTZ NOT NULL,
    end_at               TIMESTAMPTZ NOT NULL,
    reminder_offset_minutes INTEGER,
    is_all_day           BOOLEAN NOT NULL DEFAULT FALSE,
    is_pinned            BOOLEAN NOT NULL DEFAULT FALSE,
    url                  VARCHAR(500),
    repeat_type          VARCHAR(20),  -- DAILY, WEEKLY, MONTHLY, YEARLY
    repeat_interval      INTEGER DEFAULT 1,
    repeat_days          INTEGER[],
    repeat_month_day     INTEGER,
    repeat_week_ordinal  INTEGER,
    repeat_week_day      INTEGER,
    repeat_end_date      TIMESTAMPTZ,
    parent_schedule_id   BIGINT REFERENCES schedule(id) ON DELETE CASCADE,
    created_by_id        BIGINT NOT NULL REFERENCES app_user(id),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 일정 참석자 테이블
CREATE TABLE schedule_attendee (
    schedule_id BIGINT NOT NULL REFERENCES schedule(id) ON DELETE CASCADE,
    member_id   BIGINT NOT NULL REFERENCES team_member(id) ON DELETE CASCADE,
    PRIMARY KEY (schedule_id, member_id)
);


-- =========================================================
-- 4. 투두 / 진행 (To-Do / Progress)
-- =========================================================

CREATE TABLE todo (
    id                   BIGSERIAL PRIMARY KEY,
    team_id              BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    title                VARCHAR(200) NOT NULL,
    description          TEXT,
    status               todo_status NOT NULL DEFAULT 'TODO',
    start_at             TIMESTAMPTZ,
    end_at               TIMESTAMPTZ,
    due_at               TIMESTAMPTZ,
    reminder_offset_minutes INTEGER,
    url                  VARCHAR(500),
    created_by_id        BIGINT NOT NULL REFERENCES app_user(id),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 투두 참석자 테이블
CREATE TABLE todo_attendee (
    todo_id    BIGINT NOT NULL REFERENCES todo(id) ON DELETE CASCADE,
    member_id  BIGINT NOT NULL REFERENCES team_member(id) ON DELETE CASCADE,
    PRIMARY KEY (todo_id, member_id)
);


-- =========================================================
-- 5. 공지사항 (공지)
-- =========================================================

CREATE TABLE notice (
    id               BIGSERIAL PRIMARY KEY,
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    title            VARCHAR(200) NOT NULL,
    content          TEXT NOT NULL,
    author_id        BIGINT NOT NULL REFERENCES app_user(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- =========================================================
-- 6. 알림 (Notification 설정)
-- =========================================================

CREATE TABLE notification_setting (
    id                           BIGSERIAL PRIMARY KEY,
    user_id                      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    team_id                      BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    enable_team_alarm            BOOLEAN NOT NULL DEFAULT TRUE,
    enable_schedule_start_alarm   BOOLEAN NOT NULL DEFAULT TRUE,
    schedule_start_before_minutes INTEGER,  -- 10, 30, 60 (분)
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notification_setting_user_team UNIQUE (user_id, team_id)
);


-- =========================================================
-- 7. 리프레시 토큰
-- =========================================================

CREATE TABLE refresh_token (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token        VARCHAR(255) NOT NULL,
    device_info  VARCHAR(255),
    expires_at   TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_refresh_token_user_token
    ON refresh_token(user_id, token);


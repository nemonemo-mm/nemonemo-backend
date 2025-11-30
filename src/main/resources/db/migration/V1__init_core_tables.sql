-- V1__init_core_tables.sql
-- 프로젝트 기본 도메인 테이블 생성 스크립트
-- PostgreSQL + Flyway용

-- =========================================================
-- 0. 공통 ENUM 타입 정의
-- =========================================================

-- 투두 상태: TODO / DONE
CREATE TYPE todo_status AS ENUM ('TODO', 'DONE');

-- 인증 제공자: 로컬(이메일+비번), 구글 로그인
CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE');


-- =========================================================
-- 1. 사용자 (계정/인증)
-- =========================================================
CREATE TABLE app_user (
    id               BIGSERIAL PRIMARY KEY,
    -- 로그인에 사용할 이메일 (중복 불가)
    email            VARCHAR(255) NOT NULL UNIQUE,
    -- 비밀번호 해시 (bcrypt 등). 
    -- 구글 로그인만 사용하는 계정일 경우 NULL 가능하도록 설계
    password_hash    VARCHAR(255),
    -- 사용자 이름 (실명 / 닉네임 등 계정 단위 이름)
    name             VARCHAR(100) NOT NULL,
    -- 인증 제공자: LOCAL(이메일+비번) / GOOGLE
    provider         auth_provider NOT NULL DEFAULT 'LOCAL',
    -- 구글 계정과 연동 시 식별자 저장용 (sub 등)
    provider_id      VARCHAR(255),
    -- 생성/수정 시각
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- updated_at 자동 갱신 트리거를 넣고 싶다면 나중에 별도 마이그레이션에서 추가해도 됨


-- =========================================================
-- 2. 팀 / 그룹
-- =========================================================

-- 2-1) 팀(그룹) 테이블
CREATE TABLE team (
    id               BIGSERIAL PRIMARY KEY,
    -- 그룹 이름 (예: "캡스톤 팀 A", "사이드 프로젝트 팀")
    name             VARCHAR(100) NOT NULL,
    -- 그룹 초대코드 (사용자가 초대코드로 참여할 때 사용)
    invite_code      VARCHAR(50) NOT NULL UNIQUE,
    -- 그룹을 처음 만든 유저 (팀장 개념)
    owner_id         BIGINT NOT NULL REFERENCES app_user(id),
    -- 소프트 삭제 플래그 (실제 삭제 대신 숨길 수도 있음)
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2-2) 팀 내 역할/직군 카테고리
-- 예: Design / FE / BE / Marketing / No Position 등
CREATE TABLE role_category (
    id               BIGSERIAL PRIMARY KEY,
    -- 어떤 팀에 속한 카테고리인지
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    -- 카테고리 이름 (예: "Design", "BE", "No Position")
    name             VARCHAR(50) NOT NULL,
    -- 카테고리 색상 (HEX 코드 등, 예: #FFAA00)
    color_hex        VARCHAR(9),
    -- 기본 카테고리 여부 (예: "No Position" 같은 것)
    is_default       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- 한 팀 내에서 같은 이름의 카테고리가 중복되지 않도록 제약
    CONSTRAINT uq_role_category_team_name UNIQUE (team_id, name)
);

-- 2-3) 팀 멤버 테이블 (유저가 팀에 속해 있는 정보)
CREATE TABLE team_member (
    id               BIGSERIAL PRIMARY KEY,
    -- 어느 팀에 속한 멤버인지
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    -- 어떤 유저인지
    user_id          BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    -- 팀 내에서 보여줄 닉네임 (계정 이름과 별개)
    nickname         VARCHAR(100),
    -- 팀 내 역할/카테고리 (Design / FE / BE 등)
    role_category_id BIGINT REFERENCES role_category(id),
    -- 팀 관리자 여부 (팀장 / 부팀장 등, 그룹 내보내기 권한 등)
    is_admin         BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- 한 유저가 하나의 팀에 중복 가입되지 않도록 제약
    CONSTRAINT uq_team_member_team_user UNIQUE (team_id, user_id)
);


-- =========================================================
-- 3. 일정 / 마감 (Schedule)
-- =========================================================

CREATE TABLE schedule (
    id               BIGSERIAL PRIMARY KEY,
    -- 어느 팀의 일정인지
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    -- 일정 제목 (예: "중간 발표", "주간 회의")
    title            VARCHAR(200) NOT NULL,
    -- 일정 설명 (선택)
    description      TEXT,
    -- 시작 시각 (하루 종일 일정이면 날짜 기준으로 사용)
    start_at         TIMESTAMPTZ NOT NULL,
    -- 종료 시각 (start_at <= end_at 가 되도록 서비스에서 검증)
    end_at           TIMESTAMPTZ NOT NULL,
    -- 하루 종일 일정 여부
    is_all_day       BOOLEAN NOT NULL DEFAULT FALSE,
    -- 주요 일정 체크(핀 기능) - true면 상단에 고정 표시 등
    is_pinned        BOOLEAN NOT NULL DEFAULT FALSE,
    -- 일정을 생성한 유저
    created_by_id    BIGINT NOT NULL REFERENCES app_user(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 캘린더 시각화(월간 그리드, 색상 표시)는
-- 이 schedule 데이터를 조회해서 프론트에서 처리하면 됨.
-- 색상은 팀 멤버의 카테고리 색상, 혹은 일정 타입별 색상 등으로 매핑 가능.


-- =========================================================
-- 4. 투두 / 진행 (To-Do / Progress)
-- =========================================================

CREATE TABLE todo (
    id               BIGSERIAL PRIMARY KEY,
    -- 어느 팀에 속한 투두인지 (개인 투두가 아니라 "그룹 단위" 기준)
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    -- 투두 제목 (예: "백엔드 API 설계", "UI 시안 1차")
    title            VARCHAR(200) NOT NULL,
    -- 상세 내용
    description      TEXT,
    -- 상태: TODO / DONE
    status           todo_status NOT NULL DEFAULT 'TODO',
    -- 투두 마감일 (일정과 별개로 투두에 직접 마감 설정)
    due_at           TIMESTAMPTZ,
    -- 투두 생성자
    created_by_id    BIGINT NOT NULL REFERENCES app_user(id),
    -- 담당자: 팀 멤버 기준으로 설정 (user가 아니라 team_member로 두면 팀별 정보 분리 가능)
    assignee_member_id BIGINT REFERENCES team_member(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 일정 ↔ 투두 연결 (선택 기능)
-- 하나의 일정에 여러 투두를 연결하거나,
-- 하나의 투두가 여러 일정과 연결되는 상황까지 고려한 N:N 구조
CREATE TABLE todo_schedule_link (
    todo_id          BIGINT NOT NULL REFERENCES todo(id) ON DELETE CASCADE,
    schedule_id      BIGINT NOT NULL REFERENCES schedule(id) ON DELETE CASCADE,
    PRIMARY KEY (todo_id, schedule_id)
);


-- =========================================================
-- 5. 공지사항 (공지)
-- =========================================================

CREATE TABLE notice (
    id               BIGSERIAL PRIMARY KEY,
    -- 어느 팀의 공지인지
    team_id          BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    -- 공지 제목
    title            VARCHAR(200) NOT NULL,
    -- 공지 내용
    content          TEXT NOT NULL,
    -- 공지 작성자 (팀 멤버 기준으로 보관해도 되고, 단순히 유저만 남겨도 됨)
    author_id        BIGINT NOT NULL REFERENCES app_user(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 공지사항 수정/삭제는 이 테이블에 대한 UPDATE/DELETE 로직으로 처리


-- =========================================================
-- 6. 알림 (Notification 설정)
-- =========================================================

-- 유저별 / 팀별 알림 설정 테이블
-- (실제 발송 로그 테이블은 필요해지면 나중에 추가)
CREATE TABLE notification_setting (
    id                    BIGSERIAL PRIMARY KEY,
    -- 어떤 유저의 설정인지
    user_id               BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    -- 어떤 팀에 대한 알림 설정인지
    team_id               BIGINT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    -- 마감 알림 사용 여부
    enable_due_alarm      BOOLEAN NOT NULL DEFAULT TRUE,
    -- 마감 알림을 언제 보낼지 (마감 n분 전에)
    due_alarm_before_min  INTEGER NOT NULL DEFAULT 30,
    -- 주요 일정(핀 된 일정) 알림 사용 여부
    enable_pinned_alarm   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- 한 유저가 한 팀에 대한 알림 설정은 하나만 존재하도록
    CONSTRAINT uq_notification_setting_user_team UNIQUE (user_id, team_id)
);


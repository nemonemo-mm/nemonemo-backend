package com.example.demo.domain.enums;

public enum AlertType {
    // 스케줄 관련
    SCHEDULE_ASSIGNEE_ADDED,      // 스케줄 참석자로 추가됨
    SCHEDULE_POSITION_ADDED,      // 스케줄에 내 포지션이 포함됨

    // 투두 관련
    TODO_DUE_TODAY,               // 오늘까지인 투두가 남아있음

    // 팀/공지 관련
    NOTICE_UPDATED,               // 공지사항 수정/등록
    TEAM_MEMBER_JOINED,           // 새로운 팀원 참여
    TEAM_DISSOLVED                // 팀 해체 (현재는 보류용)
}



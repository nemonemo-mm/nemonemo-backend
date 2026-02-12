package com.example.demo.domain.enums;

public enum AlertType {
    // 스케줄 관련
    SCHEDULE_ASSIGNEE_ADDED,      // {userName}에게 새로운 스케줄이 등록되었습니다
    SCHEDULE_POSITION_ADDED,      // {positionName} 그룹의 새로운 스케줄이 등록되었습니다

    // 투두 관련
    TODO_DUE_TODAY,               // {teamName} 팀에서 {userName} 님의 오늘 할 일이 남아있어요!

    // 팀/공지 관련
    NOTICE_UPDATED,               // {teamName} 팀의 새로운 공지사항을 확인해주세요
    TEAM_MEMBER_JOINED,           // {teamName} 팀에 새로운 팀원 {newMemberName} 님이 참여했어요
    TEAM_DISSOLVED                // {teamName} 팀이 해체되었습니다 (보류)
}



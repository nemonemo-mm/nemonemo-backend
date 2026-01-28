package com.example.demo.repository;

import com.example.demo.domain.entity.Notice;
import com.example.demo.dto.notice.NoticeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            select 
                n.id as id,
                n.team.id as teamId,
                n.team.name as teamName,
                n.title as title,
                n.content as content,
                n.author.id as authorId,
                n.author.name as authorName,
                n.createdAt as createdAt,
                n.updatedAt as updatedAt
            from Notice n
            where n.team.id = :teamId
            order by n.createdAt desc
            """)
    List<NoticeResponse> findByTeamId(@Param("teamId") Long teamId);

    @Query("""
            select 
                n.id as id,
                n.team.id as teamId,
                n.team.name as teamName,
                n.title as title,
                n.content as content,
                n.author.id as authorId,
                n.author.name as authorName,
                n.createdAt as createdAt,
                n.updatedAt as updatedAt
            from Notice n
            where n.id = :noticeId
            """)
    Optional<NoticeResponse> findNoticeResponseById(@Param("noticeId") Long noticeId);

    boolean existsByIdAndAuthorId(Long noticeId, Long authorId);

    @Query("""
            select 
                n.id as id,
                n.team.id as teamId,
                n.team.name as teamName,
                n.title as title,
                n.content as content,
                n.author.id as authorId,
                n.author.name as authorName,
                n.createdAt as createdAt,
                n.updatedAt as updatedAt
            from Notice n
            where n.team.id = :teamId
            order by n.createdAt desc
            """)
    List<NoticeResponse> findLatestNoticesByTeamId(@Param("teamId") Long teamId);
}


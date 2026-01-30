package com.example.demo.repository;

import com.example.demo.domain.entity.Notice;
import com.example.demo.dto.notice.NoticeResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            select 
                n.id as id,
                n.team.id as teamId,
                n.team.name as teamName,
                n.content as content,
                n.author.id as authorId,
                n.author.name as authorName,
                n.createdAt as createdAt,
                n.updatedAt as updatedAt
            from Notice n
            where n.team.id = :teamId
            order by n.createdAt desc
            """)
    List<NoticeResponse> findLatestNoticeByTeamId(@Param("teamId") Long teamId, Pageable pageable);

    boolean existsByIdAndAuthorId(Long noticeId, Long authorId);
}


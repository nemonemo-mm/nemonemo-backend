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
            select new com.example.demo.dto.notice.NoticeResponse(
                n.id,
                n.team.id,
                n.team.name,
                n.content,
                n.author.id,
                n.author.name,
                n.createdAt,
                n.updatedAt
            )
            from Notice n
            join n.team
            join n.author
            where n.team.id = :teamId
            order by n.createdAt desc
            """)
    List<NoticeResponse> findLatestNoticeByTeamId(@Param("teamId") Long teamId, Pageable pageable);

    boolean existsByIdAndAuthorId(Long noticeId, Long authorId);
}


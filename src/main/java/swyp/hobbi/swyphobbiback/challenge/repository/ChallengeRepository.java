package swyp.hobbi.swyphobbiback.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.hobbi.swyphobbiback.challenge.domain.Challenge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Optional<Challenge> findByUserIdAndStartedAt(Long userId, LocalDateTime startedAt);

    @Query(value = """
        SELECT COUNT(l.post.postId) FROM Like l
        WHERE l.post.user.userId = :userId AND l.createdAt BETWEEN :start AND :end
        """)
    Integer countLikeThisWeek(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT COUNT(DISTINCT p.postId)
        FROM Post p
        JOIN UserHobbyTag uht ON p.user.userId = uht.user.userId
        JOIN PostHobbyTag pht ON p.postId = pht.post.postId
        WHERE p.user.userId = :userId
        AND
        uht.hobbyTag.hobbyTagId = pht.hobbyTag.hobbyTagId
        AND
        p.createdAt BETWEEN :start AND :end
        """)
    Integer countPostsWithSameTagsThisWeek(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT COUNT(DISTINCT p.postId)
        FROM Post p
        JOIN PostHobbyTag pht ON p.postId = pht.post.postId
        WHERE p.user.userId = :userId
        AND
        p.createdAt BETWEEN :start AND :end
        AND NOT EXISTS (
            SELECT 1
            FROM UserHobbyTag uht
            WHERE uht.user.userId = :userId
            AND
            uht.hobbyTag.hobbyTagId = pht.hobbyTag.hobbyTagId
        )
        """)
    Integer countPostsWithDiffTagsThisWeek(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

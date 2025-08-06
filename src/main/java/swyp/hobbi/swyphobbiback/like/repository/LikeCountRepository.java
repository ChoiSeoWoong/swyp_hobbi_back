package swyp.hobbi.swyphobbiback.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.hobbi.swyphobbiback.like.domain.LikeCount;
import swyp.hobbi.swyphobbiback.like.dto.LikeCountProjection;

import java.util.List;

@Repository
public interface LikeCountRepository extends JpaRepository<LikeCount, Long> {
    @Query(value = """
        SELECT plc.post_id AS postId, plc.like_count AS likeCount FROM post_like_count plc
        JOIN post p on plc.post_id = p.post_id
        WHERE plc.post_id IN (:postIds)
        """, nativeQuery = true)
    List<LikeCountProjection> findLikeCountByPostIds(@Param("postIds") List<Long> postIds);
}

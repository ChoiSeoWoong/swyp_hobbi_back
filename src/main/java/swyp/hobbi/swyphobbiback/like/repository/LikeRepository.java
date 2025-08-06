package swyp.hobbi.swyphobbiback.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.hobbi.swyphobbiback.like.domain.Like;
import swyp.hobbi.swyphobbiback.like.dto.LikeProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPost_PostIdAndUser_UserId(Long postId, Long userId);

    @Query(value = """
        SELECT pl.like_yn FROM post_like pl
        WHERE pl.post_id = :postId AND pl.user_id = :userId
        """,  nativeQuery = true)
    Optional<Boolean> findLikeYnByUserIdAndPostId(Long userId, Long postId);

    @Query(value = """
        SELECT pl.post_id AS postId, pl.like_yn AS likeYn FROM post_like pl
        JOIN post p on pl.post_id = p.post_id
        JOIN user u on pl.user_id = u.user_id
        WHERE pl.post_id IN (:postIds) and pl.user_id = :userId
        """, nativeQuery = true)
    List<LikeProjection> findLikeYnByPostIdsAndUserId(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);
}

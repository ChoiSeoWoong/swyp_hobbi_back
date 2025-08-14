package swyp.hobbi.swyphobbiback.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import swyp.hobbi.swyphobbiback.post.domain.Post;


import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>{
    @Query(value = """
            SELECT DISTINCT p.post_id
            FROM post p
            JOIN post_hobby_tag pht ON p.post_id = pht.post_id
            WHERE p.post_id < :lastId
              AND pht.hobby_tag_id IN (:tags)
            ORDER BY p.post_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findPostIdsWithTags(@Param("tags") List<Long> tags, @Param("lastId") Long lastId, @Param("limit") Integer limit);

    @Query(value = """
            SELECT DISTINCT p.post_id
            FROM post p
            JOIN post_hobby_tag pht ON p.post_id = pht.post_id
            WHERE pht.hobby_tag_id IN (:tags)
            ORDER BY p.post_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findPostIdsWithTags(@Param("tags") List<Long> tags, @Param("limit") Integer limit);

    @Query(value = """
            SELECT p.post_id
            FROM post p
            WHERE p.post_id < :lastId
            ORDER BY p.post_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findPostsByIds(@Param("lastId") Long lastId, final @Param("limit") Integer limit);

    @Query(value = """
            SELECT p.post_id
            FROM post p
            ORDER BY p.post_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findPostsByIds(final @Param("limit") Integer limit);

    @Query("""
            SELECT DISTINCT p
            FROM Post p
            left JOIN p.user u
            left JOIN p.postHobbyTags pht
            left JOIN p.postImages phts
            WHERE p.postId in (:postIds)
            ORDER BY p.postId DESC
            """)
    List<Post> findPostWithHobbyAndUser(@Param("postIds") List<Long> postIds);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.user.userId = :userId " +
            "ORDER BY p.postId DESC")
    List<Post> findTopByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.user.userId = :userId " +
            "AND p.postId < :lastPostId " +
            "ORDER BY p.postId DESC")
    List<Post> findNextByUserId(@Param("userId") Long userId, @Param("lastPostId") Long lastPostId, Pageable pageable);
}

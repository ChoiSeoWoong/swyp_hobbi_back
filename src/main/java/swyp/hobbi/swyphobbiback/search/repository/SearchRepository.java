package swyp.hobbi.swyphobbiback.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.hobbi.swyphobbiback.post.domain.Post;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Post, Long> {
    @Query(value = """
        SELECT DISTINCT p.post_id
        FROM post AS p
        JOIN post_hobby_tag AS pht ON p.post_id = pht.post_id
        JOIN user AS u ON p.user_id = u.user_id
        WHERE (:title IS NULL OR p.post_title LIKE :title)
          AND (:content IS NULL OR p.post_content LIKE :content)
          AND (:mbti IS NULL OR u.mbti LIKE :mbti)
          AND (:tagIds IS NULL OR pht.hobby_tag_id IN(:tagIds))
          AND p.post_id < :lastId
        ORDER BY p.post_id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Long> findAllPostIdsByTitleAndContent(
            @Param("title") String title,
            @Param("content") String content,
            @Param("tagIds") List<Long> tagIds,
            @Param("mbti") String mbti,
            @Param("lastId") Long lastId,
            @Param("limit") Integer pageSize
    );

    @Query(value = """
        SELECT DISTINCT p.post_id
        FROM post AS p
        JOIN post_hobby_tag AS pht ON p.post_id = pht.post_id
        JOIN user AS u ON p.user_id = u.user_id
        WHERE (:title IS NULL OR p.post_title LIKE :title)
          AND (:content IS NULL OR p.post_content LIKE :content)
          AND (:mbti IS NULL OR u.mbti LIKE :mbti)
          AND (:tagIds IS NULL OR pht.hobby_tag_id IN(:tagIds))
        ORDER BY p.post_id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Long> findAllPostIdsByTitleAndContent(
            @Param("title") String title,
            @Param("content") String content,
            @Param("tagIds") List<Long> tagIds,
            @Param("mbti") String mbti,
            @Param("limit") Integer pageSize
    );

    @Query(value = """
        SELECT DISTINCT p.post_id
        FROM post AS p
        JOIN post_hobby_tag AS pht ON p.post_id = pht.post_id
        JOIN user AS u ON p.user_id = u.user_id
        WHERE (:author IS NULL OR u.nickname LIKE :author)
          AND (:mbti IS NULL OR u.mbti LIKE :mbti)
          AND (:tagIds IS NULL OR pht.hobby_tag_id IN(:tagIds))
          AND p.post_id < :lastId
        ORDER BY p.post_id DESC
        LIMIT :limit
    """,  nativeQuery = true)
    List<Long> findAllPostIdsByAuthor(
            @Param("author") String author,
            @Param("tagIds") List<Long> tagIds,
            @Param("mbti") String mbti,
            @Param("lastId") Long lastId,
            @Param("limit") Integer pageSize
    );

    @Query(value = """
        SELECT DISTINCT p.post_id
        FROM post AS p
        JOIN post_hobby_tag AS pht ON p.post_id = pht.post_id
        JOIN user AS u ON p.user_id = u.user_id
        WHERE (:author IS NULL OR u.nickname LIKE :author)
          AND (:mbti IS NULL OR u.mbti LIKE :mbti)
          AND (:tagIds IS NULL OR pht.hobby_tag_id IN(:tagIds))
        ORDER BY p.post_id DESC
        LIMIT :limit
    """,  nativeQuery = true)
    List<Long> findAllPostIdsByAuthor(
            @Param("author") String author,
            @Param("tagIds") List<Long> tagIds,
            @Param("mbti") String mbti,
            @Param("limit") Integer pageSize
    );

    @Query("""
        SELECT DISTINCT p
        FROM Post p
        LEFT JOIN p.user u
        LEFT JOIN p.postHobbyTags pht
        LEFT JOIN p.postImages pi
        WHERE p.postId IN(:postIds)
        ORDER BY p.postId DESC
    """)
    List<Post> findAllPostsByPostIds(List<Long> postIds);

    @Query(value = """
        SELECT DISTINCT ht.hobby_tag_id
        FROM hobby_tag AS ht
        JOIN post_hobby_tag AS pht ON ht.hobby_tag_id = pht.hobby_tag_id
        WHERE ht.hobby_tag_name IN(:hobbyTags)
    """, nativeQuery = true)
    List<Long> findHobbyTagIdsByHobbyTagNames(@Param("hobbyTags") List<String> hobbyTags);
}

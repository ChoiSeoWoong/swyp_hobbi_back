package swyp.hobbi.swyphobbiback.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import swyp.hobbi.swyphobbiback.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyPostResponse {
    private Long postId;
    private String postTitle;
    private String postContents;
    private List<String> postHobbyTags;
    private String representativeImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long commentCount;
    private Long likeCount;

    public static MyPostResponse from(Post post, Long commentCount, Long likeCount) {
        return MyPostResponse.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContents(post.getPostContent())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount(commentCount == null ? 0 : commentCount)
                .likeCount(likeCount == null ? 0 : likeCount)
                .postHobbyTags(post.getPostHobbyTags().stream()
                        .map(pht -> pht.getHobbyTag().getHobbyTagName())
                        .toList())
                .representativeImageUrl(
                        post.getPostImages().isEmpty() ? null : post.getPostImages().get(0).getImageUrl()
                )
                .build();
    }
}

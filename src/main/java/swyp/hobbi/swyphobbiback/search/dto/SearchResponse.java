package swyp.hobbi.swyphobbiback.search.dto;

import lombok.Getter;
import swyp.hobbi.swyphobbiback.post.domain.Post;
import swyp.hobbi.swyphobbiback.post_image.domain.PostImage;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SearchResponse {
    private Long postId;
    private String nickname;
    private String userImageUrl;
    private Long userId;
    private String title;
    private String content;
    private List<String> postImageUrls;
    private List<String> postHobbyTags;
    private String searchWord;
    private Long commentCount;
    private Long likeCount;
    private Integer userLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SearchResponse of(Post post, String searchWord, Long commentCount, Long likeCount, Integer userLevel) {
        SearchResponse response = new SearchResponse();
        response.postId = post.getPostId();
        response.nickname = post.getUser().getNickname();
        response.userImageUrl = post.getUser().getUserImageUrl();
        response.userId = post.getUser().getUserId();
        response.title = post.getPostTitle();
        response.content = post.getPostContent();
        response.postImageUrls = post.getPostImages().stream()
                .map(PostImage::getImageUrl)
                .toList();
        response.postHobbyTags = post.getPostHobbyTags().stream()
                .map(postHobbyTag -> postHobbyTag.getHobbyTag().getHobbyTagName())
                .toList();
        response.searchWord = searchWord;
        response.commentCount = commentCount == null ? 0L : commentCount;
        response.likeCount = likeCount == null ? 0L : likeCount;
        response.userLevel = userLevel;
        response.createdAt = post.getCreatedAt();
        response.updatedAt = post.getUpdatedAt();

        return response;
    }
}

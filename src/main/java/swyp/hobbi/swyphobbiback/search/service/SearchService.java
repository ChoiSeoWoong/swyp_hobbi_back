package swyp.hobbi.swyphobbiback.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import swyp.hobbi.swyphobbiback.comment.dto.CommentCountProjection;
import swyp.hobbi.swyphobbiback.comment.repository.CommentRepository;
import swyp.hobbi.swyphobbiback.like.dto.LikeCountProjection;
import swyp.hobbi.swyphobbiback.like.repository.LikeCountRepository;
import swyp.hobbi.swyphobbiback.post.domain.Post;
import swyp.hobbi.swyphobbiback.search.dto.SearchRequest;
import swyp.hobbi.swyphobbiback.search.dto.SearchResponse;
import swyp.hobbi.swyphobbiback.search.repository.SearchRepository;
import swyp.hobbi.swyphobbiback.user.repository.UserRepository;
import swyp.hobbi.swyphobbiback.user_rank.dto.UserLevelProjection;
import swyp.hobbi.swyphobbiback.user_rank.repository.UserRankRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SearchRepository searchRepository;
    private final CommentRepository commentRepository;
    private final LikeCountRepository likeCountRepository;
    private final UserRankRepository userRankRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SearchResponse> getAllSearchForTitleAndContent(SearchRequest request, Long lastId, Integer pageSize) {
        String titleAndContent = request.getTitleAndContent();
        List<String> hobbyTags = request.getHobbyTags();
        String mbti = request.getMbti();
        String searchWord = request.getTitleAndContent();

        List<Long> hobbyTagIds = searchRepository.findHobbyTagIdsByHobbyTagNames(hobbyTags);
        if(hobbyTagIds.isEmpty()) {
            hobbyTagIds = null;
        }

        String titleAndContentWithWildCard = StringUtils.hasText(titleAndContent)?
                getLetterWithWildCard(titleAndContent) : null;
        String mbtiWithWildCard = StringUtils.hasText(mbti)?
                getLetterWithWildCard(mbti) : null;

        List<Long> postIds = fetchPostIdsByTitleAndContent(titleAndContentWithWildCard, titleAndContentWithWildCard, hobbyTagIds, mbtiWithWildCard, lastId, pageSize);
        List<Long> userIds = userRepository.findUserIdsByPostIds(postIds);
        List<Post> posts = searchRepository.findAllPostsByPostIds(postIds);

        Map<Long, Long> commentCountMap = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(CommentCountProjection::getPostId, CommentCountProjection::getCommentCount));
        Map<Long, Long> likeCountMap = likeCountRepository.findLikeCountByPostIds(postIds).stream()
                .collect(Collectors.toMap(LikeCountProjection::getPostId, LikeCountProjection::getLikeCount));
        Map<Long, Integer> userLevelMap = userRankRepository.findUserLevelByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserLevelProjection::getUserId, UserLevelProjection::getUserLevel));

        return posts.stream()
                .map(post -> {
                    Long commentCount = commentCountMap.getOrDefault(post.getPostId(), 0L);
                    Long likeCount = likeCountMap.getOrDefault(post.getPostId(), 0L);
                    Integer userLevel = userLevelMap.getOrDefault(post.getUser().getUserId(), 1);

                    return SearchResponse.of(post, searchWord, commentCount, likeCount, userLevel);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SearchResponse> getAllSearchForAuthor(SearchRequest request, Long lastId, Integer pageSize) {
        String author = request.getAuthor();
        List<String> hobbyTags = request.getHobbyTags();
        String mbti = request.getMbti();
        String searchWord = request.getAuthor();

        List<Long> hobbyTagIds = searchRepository.findHobbyTagIdsByHobbyTagNames(hobbyTags);
        if(hobbyTagIds.isEmpty()) {
            hobbyTagIds = null;
        }

        String authorWithWildCard = StringUtils.hasText(author)?
                getLetterWithWildCard(author) : null;
        String mbtiWithWildCard = StringUtils.hasText(mbti)?
                getLetterWithWildCard(mbti) : null;

        List<Long> postIds = fetchPostIdsByAuthor(authorWithWildCard, hobbyTagIds, mbtiWithWildCard, lastId, pageSize);
        List<Long> userIds = userRepository.findUserIdsByPostIds(postIds);
        List<Post> posts = searchRepository.findAllPostsByPostIds(postIds);

        Map<Long, Long> commentCountMap = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(CommentCountProjection::getPostId, CommentCountProjection::getCommentCount));
        Map<Long, Long> likeCountMap = likeCountRepository.findLikeCountByPostIds(postIds).stream()
                .collect(Collectors.toMap(LikeCountProjection::getPostId, LikeCountProjection::getLikeCount));
        Map<Long, Integer> userLevelMap = userRankRepository.findUserLevelByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserLevelProjection::getUserId, UserLevelProjection::getUserLevel));

        return posts.stream()
                .map(post -> {
                    Long commentCount = commentCountMap.getOrDefault(post.getPostId(), 0L);
                    Long likeCount = likeCountMap.getOrDefault(post.getPostId(), 0L);
                    Integer userLevel = userLevelMap.getOrDefault(post.getUser().getUserId(), 1);

                    return SearchResponse.of(post, searchWord, commentCount, likeCount, userLevel);
                })
                .toList();
    }

    private String getLetterWithWildCard(String letter) {
        StringBuilder letterWithWildCard = new StringBuilder();
        letterWithWildCard.append(letter);
        letterWithWildCard.append("%");
        return letterWithWildCard.toString();
    }

    private List<Long> fetchPostIdsByTitleAndContent(String title, String content, List<Long> hobbyTagIds, String mbti, Long lastId, Integer pageSize) {
        boolean isFirstPage = lastId == null || lastId == 0;
        if(isFirstPage) {
            return searchRepository.findAllPostIdsByTitleAndContent(title, content, hobbyTagIds, mbti, pageSize);
        } else {
            return searchRepository.findAllPostIdsByTitleAndContent(title, content, hobbyTagIds, mbti, lastId, pageSize);
        }
    }

    private List<Long> fetchPostIdsByAuthor(String author, List<Long> hobbyTagIds, String mbti, Long lastId, Integer pageSize) {
        boolean isFirstPage = lastId == null || lastId == 0;
        if(isFirstPage) {
            return searchRepository.findAllPostIdsByAuthor(author, hobbyTagIds, mbti, pageSize);
        } else {
            return searchRepository.findAllPostIdsByAuthor(author, hobbyTagIds, mbti, lastId, pageSize);
        }
    }
}


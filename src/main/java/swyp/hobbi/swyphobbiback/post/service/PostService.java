package swyp.hobbi.swyphobbiback.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swyp.hobbi.swyphobbiback.challenge.service.ChallengeService;
import swyp.hobbi.swyphobbiback.comment.dto.CommentCountProjection;
import swyp.hobbi.swyphobbiback.comment.repository.CommentRepository;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;
import swyp.hobbi.swyphobbiback.common.exception.FileUploadFailedException;
import swyp.hobbi.swyphobbiback.common.exception.PostNotFoundException;
import swyp.hobbi.swyphobbiback.common.security.CustomUserDetails;
import swyp.hobbi.swyphobbiback.hobbytag.domain.HobbyTag;
import swyp.hobbi.swyphobbiback.hobbytag.repository.HobbyTagRepository;
import swyp.hobbi.swyphobbiback.like.dto.LikeCountProjection;
import swyp.hobbi.swyphobbiback.like.dto.LikeProjection;
import swyp.hobbi.swyphobbiback.like.repository.LikeCountRepository;
import swyp.hobbi.swyphobbiback.like.repository.LikeRepository;
import swyp.hobbi.swyphobbiback.like.service.LikeService;
import swyp.hobbi.swyphobbiback.post.domain.Post;
import swyp.hobbi.swyphobbiback.post.dto.PostCreateRequest;
import swyp.hobbi.swyphobbiback.post.dto.PostResponse;
import swyp.hobbi.swyphobbiback.post.dto.PostUpdateRequest;
import swyp.hobbi.swyphobbiback.post.repository.PostRepository;
import swyp.hobbi.swyphobbiback.post_hobbytag.domain.PostHobbyTag;
import swyp.hobbi.swyphobbiback.post_image.domain.PostImage;
import swyp.hobbi.swyphobbiback.post_image.event.PostImageUploadEvent;
import swyp.hobbi.swyphobbiback.post_image.repository.PostImageRepository;
import swyp.hobbi.swyphobbiback.post_image.service.PostImageService;
import swyp.hobbi.swyphobbiback.user.repository.UserRepository;
import swyp.hobbi.swyphobbiback.user_hobbytag.domain.UserHobbyTag;
import swyp.hobbi.swyphobbiback.user_hobbytag.repository.UserHobbyTagRepository;
import swyp.hobbi.swyphobbiback.user_rank.dto.UserLevelProjection;
import swyp.hobbi.swyphobbiback.user_rank.repository.UserRankRepository;
import swyp.hobbi.swyphobbiback.user_rank.service.UserRankService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final PostImageService postImageService;
    private final LikeService likeService;
    private final HobbyTagRepository hobbyTagRepository;
    private final PostImageRepository postImageRepository;
    private final UserHobbyTagRepository userHobbyTagRepository;
    private final CommentRepository commentRepository;
    private final LikeCountRepository likeCountRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChallengeService challengeService;
    private final UserRankService userRankService;
    private final UserRankRepository userRankRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponse create(CustomUserDetails userDetails, PostCreateRequest request, List<MultipartFile> imageFiles) {
        Post post = Post.builder()
                .user(userDetails.getUser())
                .postTitle(request.getTitle())
                .postContent(request.getContent())
                .build();

        postRepository.save(post);

        List<String> uploadedImageUrls = new ArrayList<>();
        List<PostImage> postImages = post.getPostImages();

        uploadImages(imageFiles, postImages, uploadedImageUrls, post);

        List<PostHobbyTag> postHobbyTags = post.getPostHobbyTags();
        if(!request.getHobbyTagNames().isEmpty()) {
            List<HobbyTag> hobbyTags = hobbyTagRepository.findAllByHobbyTagNameIn(request.getHobbyTagNames());
            postHobbyTags = hobbyTags.stream()
                    .map(hobbyTag -> PostHobbyTag.builder()
                            .post(post)
                            .hobbyTag(hobbyTag)
                            .build()
                    )
                    .toList();
        }

        post.getPostHobbyTags().addAll(postHobbyTags);
        Integer userLevel = userRankService.getUserLevel(userDetails.getUser());
        challengeService.evaluateChallenges(userDetails.getUserId());

        return PostResponse.from(post, null, null, false, userLevel);
    }

    public PostResponse findPost(CustomUserDetails userDetails, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        Long commentCount = commentRepository.countByPostId(postId);
        Long likeCount = likeService.getCount(postId);
        Boolean liked = likeService.likedByUserAndPost(userDetails.getUserId(), postId);
        Integer userLevel = userRankService.getUserLevel(post.getUser());

        return PostResponse.from(post, commentCount, likeCount, liked, userLevel);
    }

    @Transactional
    public void update(CustomUserDetails userDetails, Long postId, PostUpdateRequest request, List<MultipartFile> newImageFiles) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if (!post.getUser().getUserId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException(ErrorCode.FORBIDDEN.getMessage());
        }

        post.update(request.getTitle(), request.getContent());

        List<PostImage> postImages = post.getPostImages();
        List<String> uploadedImageUrls = new ArrayList<>();

        if(request.getDeletedImageUrls() != null && !request.getDeletedImageUrls().isEmpty()) {
            List<PostImage> toDeleteImages = post.getPostImages().stream()
                    .filter(image -> request.getDeletedImageUrls().contains(image.getImageUrl()))
                    .toList();

            for (PostImage image : toDeleteImages) {
                String suffixImageUrl = postImageService.getSuffixImageUrl(image.getImageUrl());
                postImageService.deletePostImage(suffixImageUrl);
                postImages.remove(image);
                postImageRepository.delete(image);
            }
        }

        uploadImages(newImageFiles, postImages, uploadedImageUrls, post);

        post.getPostHobbyTags().clear();

        if(!request.getHobbyTagNames().isEmpty()) {
            List<HobbyTag> hobbyTags = hobbyTagRepository.findAllByHobbyTagNameIn(request.getHobbyTagNames());
            List<PostHobbyTag> postHobbyTags = hobbyTags.stream()
                    .map(hobbyTag -> PostHobbyTag.builder()
                            .post(post)
                            .hobbyTag(hobbyTag)
                            .build()
                    )
                    .toList();
            post.getPostHobbyTags().addAll(postHobbyTags);
        }
    }

    public void delete(CustomUserDetails userDetails, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if (!post.getUser().getUserId().equals(userDetails.getUserId())) {
            throw new AccessDeniedException(ErrorCode.FORBIDDEN.getMessage());
        }

        for(PostImage image : post.getPostImages()) {
            String suffixImageUrl = postImageService.getSuffixImageUrl(image.getImageUrl());
            postImageService.deletePostImage(suffixImageUrl);
        }

        post.getPostImages().clear();
        post.getPostHobbyTags().clear();

        postRepository.delete(post);
    }

    public List<PostResponse> findPostsInfiniteScroll(final CustomUserDetails userDetails, boolean tagExist, Long lastPostId, final Integer pageSize) {
        final Long userId = userDetails.getUserId();
        List<Long> postIds = fetchPostIds(tagExist, lastPostId, pageSize, userId);
        List<Long> userIds = userRepository.findUserIdsByPostIds(postIds);
        List<Post> posts = postRepository.findPostWithHobbyAndUser(postIds);

        Map<Long, Long> commentCountMap = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(CommentCountProjection::getPostId, CommentCountProjection::getCommentCount));
        Map<Long, Long> likeCountMap = likeCountRepository.findLikeCountByPostIds(postIds).stream()
                .collect(Collectors.toMap(LikeCountProjection::getPostId, LikeCountProjection::getLikeCount));
        Map<Long, Boolean> likeYnMap = likeRepository.findLikeYnByPostIdsAndUserId(postIds, userId).stream()
                .collect(Collectors.toMap(LikeProjection::getPostId, LikeProjection::getLikeYn));
        Map<Long, Integer> userLevelMap = userRankRepository.findUserLevelByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserLevelProjection::getUserId, UserLevelProjection::getUserLevel));

        return posts.stream()
                .map(post -> {
                    Long commentCount = commentCountMap.getOrDefault(post.getPostId(), 0L);
                    Long likeCount = likeCountMap.getOrDefault(post.getPostId(), 0L);
                    Boolean likeYn = likeYnMap.getOrDefault(post.getPostId(), false);
                    Integer userLevel = userLevelMap.getOrDefault(post.getUser().getUserId(), 1);

                    return PostResponse.from(post, commentCount, likeCount, likeYn, userLevel);
                })
                .toList();
    }

    private List<Long> getUserHobbyTagIds(final Long userId) {
        return userHobbyTagRepository.findAllByUser_UserId(userId)
                .stream()
                .map(UserHobbyTag::getHobbyTag)
                .map(HobbyTag::getHobbyTagId)
                .toList();
    }

    private List<Long> fetchPostIds(final boolean tagExist, final Long lastPostId, final Integer pageSize, final Long userId) {
        boolean isFirstPage = lastPostId == null || lastPostId == 0;
        if (!tagExist) {
            if (isFirstPage) {
                return postRepository.findPostsByIds(pageSize);
            }
            return postRepository.findPostsByIds(lastPostId, pageSize);
        }
        final List<Long> hobbyTagIds = getUserHobbyTagIds(userId);
        if (isFirstPage) {
            return postRepository.findPostIdsWithTags(hobbyTagIds, pageSize);
        }
        return postRepository.findPostIdsWithTags(hobbyTagIds, lastPostId, pageSize);
    }

    private void uploadImages(List<MultipartFile> imageFiles, List<PostImage> postImages, List<String> uploadedImageUrls, Post post) {
        try {
            if(imageFiles != null && !imageFiles.isEmpty()) {
                for(MultipartFile imageFile : imageFiles) {
                    String generatedUniqueFileName = postImageService.fileNameFormatter(imageFile);
                    String imageUrl = postImageService.generateS3Url(generatedUniqueFileName);
                    PostImage savedPostImage = postImageService.savePostImage(imageFile, post, imageUrl);
                    postImages.add(savedPostImage);

                    eventPublisher.publishEvent(PostImageUploadEvent.builder()
                            .postImageId(savedPostImage.getImageId())
                            .file(imageFile)
                            .fileName(generatedUniqueFileName)
                            .build()
                    );
                    uploadedImageUrls.add(imageUrl);
                }
            }
        } catch (Exception e) {
            for(String uploadedImageUrl : uploadedImageUrls) {
                postImageService.deletePostImage(uploadedImageUrl);
            }

            throw new FileUploadFailedException();
        }
    }
}

package swyp.hobbi.swyphobbiback.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.hobbi.swyphobbiback.common.security.CustomUserDetails;
import swyp.hobbi.swyphobbiback.post.dto.PostCreateRequest;
import swyp.hobbi.swyphobbiback.post.dto.PostResponse;
import swyp.hobbi.swyphobbiback.post.dto.PostUpdateRequest;
import swyp.hobbi.swyphobbiback.post.service.PostService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
@Slf4j
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") PostCreateRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) {
        PostResponse response = postService.create(userDetails, request, imageFiles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        return ResponseEntity.ok(postService.findPost(userDetails, postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestPart("request") PostUpdateRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> newImageFiles
    ) {
        postService.update(userDetails, postId, request, newImageFiles);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.delete(userDetails, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "tagExist") boolean tagExist,
            @RequestParam(value = "lastPostId", required = false) Long lastPostId,
            @RequestParam(value = "pageSize", defaultValue = "15") Integer pageSize
    ) {
        final List<PostResponse> postsInfiniteScroll = postService.findPostsInfiniteScroll(userDetails, tagExist, lastPostId, pageSize);
        return ResponseEntity.ok(postsInfiniteScroll);
    }
}

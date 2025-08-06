package swyp.hobbi.swyphobbiback.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;
import swyp.hobbi.swyphobbiback.common.exception.CustomException;
import swyp.hobbi.swyphobbiback.common.security.CustomUserDetails;
import swyp.hobbi.swyphobbiback.mypage.dto.*;
import swyp.hobbi.swyphobbiback.mypage.service.MyPageService;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class MyPageController {

    private final MyPageService myPageService;

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }

    @GetMapping("/my-page")
    public ResponseEntity<MyPageResponse> getMyPageInfo() {
        Long userId = getCurrentUserId();
        MyPageResponse response = myPageService.getMyPageInfo(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-page/my-modify-page") // 개인정보수정 페이지 조회
    public ResponseEntity<MyModifyPageResponse> getMyModifyPageInfo() {
        Long userId = getCurrentUserId();
        MyModifyPageResponse response = myPageService.getMyModifyPageInfo(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/my-page/update")
    public ResponseEntity<Void> updateMyPageInfo(@RequestBody MyPageUpdateRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        myPageService.updateMyPageInfo(userId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/my-page/update/nickname") // 닉네임 변경
    public ResponseEntity<Void> updateNickname(@RequestBody NicknameUpdateRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        myPageService.updateNickname(userDetails.getUserId(), request.getNickname());
        return ResponseEntity.ok().build();
    }

    @PostMapping("my-page/update/password/check") // 현재 비밀번호 확인
    public ResponseEntity<PasswordCheckResponse> PasswordCheck(@RequestBody PasswordCheckRequest request,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean check = myPageService.checkPassword(userDetails.getUserId(), request.getCurrentPassword());
        if (!check) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        return ResponseEntity.ok(new PasswordCheckResponse(check));
    }

    @PutMapping("/my-page/update/password") // 비밀번호 변경
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        myPageService.updatePassword(userDetails.getUserId(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/my-page/update/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadProfileImage(@RequestPart MultipartFile profileImage,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        String userImageUrl = myPageService.uploadAndSaveProfileImage(userDetails.getUserId(), profileImage);
        return ResponseEntity.ok(userImageUrl);
    }


    @PostMapping("/my-page/validation/nickname")
    public ResponseEntity<NicknameDuplicateResponse> validateNickname(@RequestBody NicknameUpdateRequest request) {

        Boolean exists = myPageService.isNicknameDuplicate(request.getNickname());
        String message = exists ? "이미 존재하는 닉네임입니다." : "사용 가능한 닉네임입니다.";

        NicknameDuplicateResponse response = new NicknameDuplicateResponse(exists, message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-page/myposts")
    public ResponseEntity<MyPostsScrollResponse> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long userId = userDetails.getUserId();
        MyPostsScrollResponse response = myPageService.getMyPosts(userId, lastPostId, pageSize);
        return ResponseEntity.ok(response);
    }
}

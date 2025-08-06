package swyp.hobbi.swyphobbiback.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.hobbi.swyphobbiback.email.dto.EmailCheckRequest;
import swyp.hobbi.swyphobbiback.email.dto.EmailCheckResponse;
import swyp.hobbi.swyphobbiback.user.dto.PasswordResetEmailRequest;
import swyp.hobbi.swyphobbiback.user.dto.PasswordResetRequest;
import swyp.hobbi.swyphobbiback.user.service.PasswordResetService;

@RestController
@RequestMapping("/api/v1/user/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/reset-link") // 인증 이메일 전송
    public ResponseEntity<String> sendResetLink(@RequestBody PasswordResetEmailRequest request) {
        passwordResetService.sendPasswordResetLink(request.getEmail());
        return ResponseEntity.ok("인증 메일 발송 완료!");
    }

    @PostMapping("/verify/check") //이메일 인증 여부 확인
    public ResponseEntity<EmailCheckResponse> checkEmailVerification(@RequestBody EmailCheckRequest request) {
        passwordResetService.verifyResetCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new EmailCheckResponse("이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/reset") // 비밀번호 변경
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}

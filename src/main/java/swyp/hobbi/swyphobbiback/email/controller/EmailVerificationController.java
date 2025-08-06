package swyp.hobbi.swyphobbiback.email.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.hobbi.swyphobbiback.email.dto.EmailCheckRequest;
import swyp.hobbi.swyphobbiback.email.service.EmailVerificationService;
import swyp.hobbi.swyphobbiback.email.dto.EmailRequest;
import swyp.hobbi.swyphobbiback.email.dto.EmailCheckResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send") //인증 이메일 발송
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailRequest request) {
        emailVerificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok("인증 메일 발송 완료!");
    }

    @PostMapping("/verification/check") //이메일 인증 여부 확인
    public ResponseEntity<EmailCheckResponse> checkEmailVerification(@RequestBody EmailCheckRequest request) {
        emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new EmailCheckResponse("이메일 인증이 완료되었습니다."));
    }
}

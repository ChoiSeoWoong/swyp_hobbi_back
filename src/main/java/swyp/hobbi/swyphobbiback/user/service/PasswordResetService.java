package swyp.hobbi.swyphobbiback.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;
import swyp.hobbi.swyphobbiback.common.exception.CustomException;
import swyp.hobbi.swyphobbiback.email.service.EmailContentBuilder;
import swyp.hobbi.swyphobbiback.email.service.EmailSendService;
import swyp.hobbi.swyphobbiback.email.service.EmailVerificationService;
import swyp.hobbi.swyphobbiback.user.domain.PasswordResetCode;
import swyp.hobbi.swyphobbiback.user.domain.User;
import swyp.hobbi.swyphobbiback.user.repository.PasswordResetCodeRepository;
import swyp.hobbi.swyphobbiback.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String RATE_LIMIT_PREFIX = "email:limit:";
    private static final String RATE_LIMIT_FLAG = "1";
    private static final long RATE_LIMIT_TTL_SECONDS = 30;
    private static final long CODE_EXPIRATION_MINUTES = 3;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final EmailSendService emailSendService;
    private final EmailVerificationService emailVerificationService;
    private final RedisTemplate<String, String> redisTemplate;

    public void sendPasswordResetLink(String email) {

        if (!userRepository.existsByEmailAndIsDeletedFalse(email)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String key = rateLimitKey(email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }
        redisTemplate.opsForValue().set(key, RATE_LIMIT_FLAG, RATE_LIMIT_TTL_SECONDS, TimeUnit.SECONDS); // rate limit 기록 (30초 유지)

        String code = emailVerificationService.generateAlphaNumericCode(); // 인증코드 생성

        PasswordResetCode resetCode = passwordResetCodeRepository.findByEmail(email)
                .orElse(PasswordResetCode.builder().email(email).build());

        resetCode.setCode(code);
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        resetCode.setVerified(false);
        passwordResetCodeRepository.save(resetCode);

        // 인증 이메일 전송
        emailSendService.sendEmail(email, EmailContentBuilder.getVerificationTitle(), EmailContentBuilder.buildVerificationContent(code));
    }

    public void verifyResetCode(String email, String resetCode) {
        PasswordResetCode code = passwordResetCodeRepository.findByEmailAndCode(email, resetCode)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL_CODE));

        code.setVerified(true);
        passwordResetCodeRepository.save(code);

        redisTemplate.delete(rateLimitKey(email));
    }

    public void resetPassword(String email, String resetCode, String newPassword) {

        passwordResetCodeRepository.findByEmailAndCode(email, resetCode)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()) && Boolean.TRUE.equals(t.getVerified()))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_EMAIL_CODE));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword)); // 비밀번호 암호화 후 변경
        userRepository.save(user);

        passwordResetCodeRepository.deleteByEmail(email);
    }

    private String rateLimitKey(String email) {
        return RATE_LIMIT_PREFIX + email;
    }
}

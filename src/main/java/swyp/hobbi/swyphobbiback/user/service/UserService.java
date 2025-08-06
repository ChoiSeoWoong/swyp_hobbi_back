package swyp.hobbi.swyphobbiback.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swyp.hobbi.swyphobbiback.common.exception.UserNotFoundException;
import swyp.hobbi.swyphobbiback.hobbytag.domain.HobbyTag;
import swyp.hobbi.swyphobbiback.hobbytag.repository.HobbyTagRepository;
import swyp.hobbi.swyphobbiback.token.dto.TokenPair;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;
import swyp.hobbi.swyphobbiback.common.exception.CustomException;
import swyp.hobbi.swyphobbiback.email.domain.EmailVerification;
import swyp.hobbi.swyphobbiback.email.repository.EmailVerificationRepository;
import swyp.hobbi.swyphobbiback.token.repository.RefreshTokenRepository;
import swyp.hobbi.swyphobbiback.user.domain.DeletedUser;
import swyp.hobbi.swyphobbiback.user.domain.User;
import swyp.hobbi.swyphobbiback.user.dto.*;
import swyp.hobbi.swyphobbiback.user.repository.DeletedUserRepository;
import swyp.hobbi.swyphobbiback.user_hobbytag.domain.UserHobbyTag;
import swyp.hobbi.swyphobbiback.user.repository.UserRepository;
import swyp.hobbi.swyphobbiback.token.service.TokenService;
import swyp.hobbi.swyphobbiback.user_rank.domain.UserRank;
import swyp.hobbi.swyphobbiback.user_rank.repository.UserRankRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HobbyTagRepository hobbyTagRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRankRepository userRankRepository;

    private static final String DELETED_NICKNAME_PREFIX = "탈퇴한사용자_";

    public UserResponse signUp(UserCreateRequest request) {

        if (!emailVerificationRepository.findByEmail(request.getEmail()).map(EmailVerification::getVerified).orElse(false)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword()); //비밀번호 암호화

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .birthYear(request.getBirthYear())
                .birthMonth(request.getBirthMonth())
                .birthDay(request.getBirthDay())
                .gender(request.getGender())
                .mbti(request.getMbti())
                .isDeleted(false)
                .role("User")
                .isTagExist(!request.getHobbyTags().isEmpty())
                .isBlocked(false)
                .userImageUrl(request.getUserImageUrl() != null ? request.getUserImageUrl() : "default.png") //기본값 설정 필요
                .build();

        List<HobbyTag> hobbyTags = hobbyTagRepository.findAllByHobbyTagNameIn(request.getHobbyTags());
        // UserHobbyTag 생성
        for (HobbyTag hobbyTag : hobbyTags) {
            user.getUserHobbyTags().add(new UserHobbyTag(user, hobbyTag));
        }

        UserRank userRank = UserRank.builder()
                .user(user)
                .level(1)
                .rank(UserRank.RankType.RED_HOBBI)
                .exp(0)
                .build();

        userRepository.save(user); // 유저 정보 저장
        userRankRepository.save(userRank); // 유저 랭크 저장

        TokenPair tokens = tokenService.generateAndSaveTokens(user.getEmail()); // 자동로그인 위한 토큰 생성 및 저장

        return UserResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .userId(user.getUserId())
                .build();

    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED); //이미 탈퇴한 사용자
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        TokenPair tokens = tokenService.generateAndSaveTokens(user.getEmail()); // 토큰 생성 및 저장

        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .userId(user.getUserId())
                .build();
    }

    @Transactional
    public void delete(Long userId, UserDeleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Refresh Token 삭제
        refreshTokenRepository.deleteByEmail(user.getEmail());

        // 닉네임 비식별 처리
        user.setNickname(generateAnonymousNickname());

        // 탈퇴 처리
        user.setIsDeleted(true);
        userRepository.save(user);

        DeletedUser deletedUser = DeletedUser.builder()
                .userId(user.getUserId())
                .reason(request.getReason())
                .deletedAt(LocalDateTime.now())
                .build();
        deletedUserRepository.save(deletedUser); // 탈퇴 기록 저장
    }

    @Transactional
    public void logout(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    private String generateAnonymousNickname() {
        return DELETED_NICKNAME_PREFIX + UUID.randomUUID().toString().substring(0, 6);
    }


}

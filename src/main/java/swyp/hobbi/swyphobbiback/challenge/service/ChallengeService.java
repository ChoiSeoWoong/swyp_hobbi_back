package swyp.hobbi.swyphobbiback.challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.hobbi.swyphobbiback.challenge.domain.Challenge;
import swyp.hobbi.swyphobbiback.challenge.domain.ChallengeDefaults;
import swyp.hobbi.swyphobbiback.challenge.domain.ChallengeType;
import swyp.hobbi.swyphobbiback.challenge.dto.ChallengeResponse;
import swyp.hobbi.swyphobbiback.challenge.repository.ChallengeRepository;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;
import swyp.hobbi.swyphobbiback.common.exception.CustomException;
import swyp.hobbi.swyphobbiback.user_rank.service.UserRankService;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final UserRankService userRankService;

    public ChallengeResponse getChallenge(Long userId) {
        LocalDateTime weekStart = getStartOfWeek(LocalDateTime.now());

        Challenge challenge = challengeRepository.findByUserIdAndStartedAt(userId, weekStart)
                .orElseGet(() -> createChallenge(userId, weekStart));

        return ChallengeResponse.of(challenge);
    }

    @Transactional
    public void startSpecificChallenge(Long userId, String challengeType) {
        LocalDateTime weekStart = getStartOfWeek(LocalDateTime.now());
        Challenge challenge = challengeRepository.findByUserIdAndStartedAt(userId, weekStart)
                .orElseGet(() -> createChallenge(userId, weekStart));
        ChallengeType type = ChallengeType.valueOf(challengeType);

        switch (type) {
            case SHOWOFF -> challenge.setHobbyShowOffStarted(true);
            case ROUTINER -> challenge.setHobbyRoutinerStarted(true);
            case RICH -> challenge.setHobbyRichStarted(true);
            default -> throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        challengeRepository.save(challenge);
    }

    @Transactional
    public void evaluateChallenges(Long userId) {
        LocalDateTime weekStart = getStartOfWeek(LocalDateTime.now());
        LocalDateTime weekEnd = getEndOfWeek(weekStart);
        Challenge challenge = challengeRepository.findByUserIdAndStartedAt(userId, weekStart)
                .orElse(null);

        if(challenge == null) {
            return;
        }

        // 취미 자랑 챌린지 - 좋아요 누적 50개
        if(challenge.getHobbyShowOffStarted() && !challenge.getHobbyShowOffAchieved()) {
            Integer likeCount = challengeRepository.countLikeThisWeek(userId, weekStart, weekEnd);
            challenge.setHobbyShowOffPoint(likeCount);

            if(likeCount >= ChallengeDefaults.LIKE_STACK_COUNT) {
                challenge.setHobbyShowOffAchieved(true);
                userRankService.addExp(userId, 10);
            }
        }

        // 루티너 챌린지 - 같은 취미 게시글 3개
        if(challenge.getHobbyRoutinerStarted() && !challenge.getHobbyRoutinerAchieved()) {
            Integer count = challengeRepository.countPostsWithSameTagsThisWeek(userId, weekStart, weekEnd);
            challenge.setHobbyRoutinerPoint(count);

            if (count >= ChallengeDefaults.POST_SAME_TAGS_COUNT) {
                challenge.setHobbyRoutinerAchieved(true);
                userRankService.addExp(userId, 10);
            }
        }

        // 취미 부자 챌린지 - 다른 취미 게시글 3개
        if(challenge.getHobbyRichStarted() && !challenge.getHobbyRichAchieved()) {
            Integer count = challengeRepository.countPostsWithDiffTagsThisWeek(userId, weekStart, weekEnd);
            challenge.setHobbyRichPoint(count);

            if (count >= ChallengeDefaults.POST_DIFF_TAGS_COUNT) {
                challenge.setHobbyRichAchieved(true);
                userRankService.addExp(userId, 10);
            }
        }

        challengeRepository.save(challenge);
    }

    private LocalDateTime getStartOfWeek(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay();
    }

    private LocalDateTime getEndOfWeek(LocalDateTime weekStart) {
        return weekStart.plusDays(ChallengeDefaults.SIX_DAYS).with(LocalTime.MAX);
    }

    private Challenge createChallenge(Long userId, LocalDateTime weekStart) {
        Challenge challenge = Challenge.builder()
                .userId(userId)
                .startedAt(weekStart)
                .hobbyShowOffPoint(ChallengeDefaults.ZERO)
                .hobbyRoutinerPoint(ChallengeDefaults.ZERO)
                .hobbyRichPoint(ChallengeDefaults.ZERO)
                .build();

        return challengeRepository.save(challenge);
    }
}

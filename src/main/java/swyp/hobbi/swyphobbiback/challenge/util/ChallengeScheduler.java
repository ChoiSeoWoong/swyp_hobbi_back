package swyp.hobbi.swyphobbiback.challenge.util;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import swyp.hobbi.swyphobbiback.challenge.domain.Challenge;
import swyp.hobbi.swyphobbiback.challenge.domain.ChallengeDefaults;
import swyp.hobbi.swyphobbiback.challenge.repository.ChallengeRepository;
import swyp.hobbi.swyphobbiback.user.domain.User;
import swyp.hobbi.swyphobbiback.user.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChallengeScheduler {
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * MON")
    public void resetChallenges() {
        LocalDateTime weekStart = getStartOfWeek(LocalDateTime.now());

        List<User> users = userRepository.findAll();

        for(User user : users){
            Challenge challenge = Challenge.builder()
                    .userId(user.getUserId())
                    .startedAt(weekStart)
                    .hobbyShowOffPoint(ChallengeDefaults.ZERO)
                    .hobbyRoutinerPoint(ChallengeDefaults.ZERO)
                    .hobbyRichPoint(ChallengeDefaults.ZERO)
                    .build();
            challengeRepository.save(challenge);
        }
    }

    private LocalDateTime getStartOfWeek(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay();
    }
}

package swyp.hobbi.swyphobbiback.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import swyp.hobbi.swyphobbiback.challenge.domain.Challenge;

@Getter
@AllArgsConstructor
public class ChallengeResponse {
    private ChallengeStatusResponse hobbyShowOff;
    private ChallengeStatusResponse hobbyRoutiner;
    private ChallengeStatusResponse hobbyRich;

    @Data
    @AllArgsConstructor
    public static class ChallengeStatusResponse {
        private boolean started;
        private boolean achieved;
        private int point;
    }

    public static ChallengeResponse of(Challenge challenge) {
        return new ChallengeResponse(
                new ChallengeStatusResponse(challenge.getHobbyShowOffStarted(), challenge.getHobbyShowOffAchieved(), challenge.getHobbyShowOffPoint()),
                new ChallengeStatusResponse(challenge.getHobbyRoutinerStarted(), challenge.getHobbyRoutinerAchieved(), challenge.getHobbyRoutinerPoint()),
                new ChallengeStatusResponse(challenge.getHobbyRichStarted(), challenge.getHobbyRichAchieved(), challenge.getHobbyRichPoint())
        );
    }
}

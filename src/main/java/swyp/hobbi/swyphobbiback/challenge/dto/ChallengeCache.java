package swyp.hobbi.swyphobbiback.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.hobbi.swyphobbiback.challenge.domain.Challenge;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeCache implements Serializable {
    private int hobbyShowOffPoint;
    private int hobbyRoutinerPoint;
    private int hobbyRichPoint;
    private boolean hobbyShowOffStarted;
    private boolean hobbyRoutinerStarted;
    private boolean hobbyRichStarted;
    private boolean hobbyShowOffAchieved;
    private boolean hobbyRoutinerAchieved;
    private boolean hobbyRichAchieved;

    public static ChallengeCache from(Challenge challenge) {
        ChallengeCache cache = new ChallengeCache();
        cache.hobbyShowOffPoint = challenge.getHobbyShowOffPoint();
        cache.hobbyRoutinerPoint = challenge.getHobbyRoutinerPoint();
        cache.hobbyRichPoint = challenge.getHobbyRichPoint();
        cache.hobbyShowOffStarted = challenge.getHobbyShowOffStarted();
        cache.hobbyRoutinerStarted = challenge.getHobbyRoutinerStarted();
        cache.hobbyRichStarted = challenge.getHobbyRichStarted();
        cache.hobbyShowOffAchieved = challenge.getHobbyShowOffAchieved();
        cache.hobbyRoutinerAchieved = challenge.getHobbyRoutinerAchieved();
        cache.hobbyRichAchieved = challenge.getHobbyRichAchieved();

        return cache;
    }
}

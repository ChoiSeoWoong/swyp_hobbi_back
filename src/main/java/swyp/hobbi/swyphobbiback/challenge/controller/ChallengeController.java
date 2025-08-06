package swyp.hobbi.swyphobbiback.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import swyp.hobbi.swyphobbiback.challenge.dto.ChallengeResponse;
import swyp.hobbi.swyphobbiback.challenge.service.ChallengeService;
import swyp.hobbi.swyphobbiback.common.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenge")
public class ChallengeController {
    private final ChallengeService challengeService;

    @PostMapping("/start")
    public ResponseEntity<Void> startChallenge(@RequestParam("challengeType") String challengeType, @AuthenticationPrincipal CustomUserDetails userDetails) {
        challengeService.startSpecificChallenge(userDetails.getUserId(), challengeType);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<ChallengeResponse> getAllChallenges(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ChallengeResponse response = challengeService.getChallenge(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }
}

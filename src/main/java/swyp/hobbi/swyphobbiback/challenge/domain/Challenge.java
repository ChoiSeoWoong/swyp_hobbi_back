package swyp.hobbi.swyphobbiback.challenge.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long challengeId;

    private Long userId;

    // HobbyShowOff, HobbyRoutiner, HobbyRich
    private Integer hobbyShowOffPoint;
    private Integer hobbyRoutinerPoint;
    private Integer hobbyRichPoint;

    @Builder.Default
    private Boolean hobbyShowOffStarted = false;
    @Builder.Default
    private Boolean hobbyRoutinerStarted = false;
    @Builder.Default
    private Boolean hobbyRichStarted = false;

    @Builder.Default
    private Boolean hobbyShowOffAchieved = false;
    @Builder.Default
    private Boolean hobbyRoutinerAchieved = false;
    @Builder.Default
    private Boolean hobbyRichAchieved = false;

    private LocalDateTime startedAt;
}

package swyp.hobbi.swyphobbiback.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.hobbi.swyphobbiback.email.domain.EmailVerification;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
}

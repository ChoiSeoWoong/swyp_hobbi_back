package swyp.hobbi.swyphobbiback.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.hobbi.swyphobbiback.user.domain.PasswordResetCode;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByEmail(String email);
    Optional<PasswordResetCode> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
}

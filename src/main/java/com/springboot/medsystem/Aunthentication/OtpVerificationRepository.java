package com.springboot.medsystem.Aunthentication;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmail(String email);

    Optional<OtpVerification> findByEmailAndOtp(String email, String otp);
}
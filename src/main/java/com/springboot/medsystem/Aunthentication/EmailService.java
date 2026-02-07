package com.springboot.medsystem.Aunthentication;

public interface EmailService {
    void sendOtpEmail(String to, String otp);

    void sendResetPasswordEmail(String to, String resetLink);
}
package com.springboot.medsystem.Configuration;

import com.springboot.medsystem.Patient.PatientProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${BREVO_API}")
    private String brevoApiKey;

    @Value("${BREVO_MAIL}")
    private String fromEmail;

    @Value("${BREVO_SENDER_NAME:MedEase}")
    private String fromName;


    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> payload = new HashMap<>();

            payload.put("sender", Map.of(
                    "email", fromEmail,
                    "name", fromName
            ));

            payload.put("to", List.of(
                    Map.of("email", to)
            ));

            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    BREVO_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent to {}", to);
            } else {
                log.error("Brevo error: {}", response.getBody());
            }

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public void sendVerificationEmail(Object user, String token) {

        String email = null;
        String name = null;
        if (user instanceof com.springboot.medsystem.DTO.Register) {
            email = ((com.springboot.medsystem.DTO.Register) user).getEmail();
            name = ((com.springboot.medsystem.DTO.Register) user).getFullName();
        } else if (user instanceof com.springboot.medsystem.Pharmacy.PharmacyProfile) {
            email = ((com.springboot.medsystem.Pharmacy.PharmacyProfile) user).getEmail();
            name = ((com.springboot.medsystem.Pharmacy.PharmacyProfile) user).getFullName();
        } else if (user instanceof com.springboot.medsystem.Doctor.DoctorProfile) {
            email = ((com.springboot.medsystem.Doctor.DoctorProfile) user).getEmail();
            name = ((com.springboot.medsystem.Doctor.DoctorProfile) user).getFullName();
        } else if (user PatientProfile) {
            email = ((com.springboot.medsystem.Patient.PatientProfile) user).getEmail();
            name = ((com.springboot.medsystem.Patient.PatientProfile) user).getFullName();
        }
        if (email == null || name == null) {
            log.error("Cannot send verification email: missing email or name");
            return;
        }

        String html = """
                <html>
                <body style=\"font-family: Arial, sans-serif;\">
                    <div style=\"max-width:600px;margin:auto;padding:20px;border:1px solid #ddd;\">
                        <h2 style=\"color:#4CAF50;\">Email Verification</h2>
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Please verify your email by clicking the button below:</p>
                        <div style=\"text-align:center;margin:20px;\">
                            <a href=\"%s\"
                               style=\"background:#4CAF50;color:#fff;padding:12px 25px;
                                      text-decoration:none;border-radius:5px;\">
                               Verify Email
                            </a>
                        </div>
                        <p>If the button doesn’t work, copy this link:</p>
                        <p>%s</p>
                        <small>This link expires in 24 hours.</small>
                    </div>
                </body>
                </html>
                """.formatted(name, link, link);
        sendEmail(email, "Verify Your Email", html);
    }
}
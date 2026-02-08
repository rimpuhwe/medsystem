package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String Message;
    private String referenceNumber;
    private String token;
    private String otp;

    @CreationTimestamp
    private LocalDate Timestamp;
}

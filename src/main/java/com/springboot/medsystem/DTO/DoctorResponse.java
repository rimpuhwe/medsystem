package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorResponse {
    private String Message;
    private String email;
    private String otp;

    @CreationTimestamp
    private LocalDate Timestamp;
    @Enumerated(EnumType.STRING)
    private Role role;
}

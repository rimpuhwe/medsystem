package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Role;
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
public class PharmacyResponse {
    private String message;
    private String email;
    private String otp;
    @CreationTimestamp
    private LocalDate timestamp;
    private Role role;
}

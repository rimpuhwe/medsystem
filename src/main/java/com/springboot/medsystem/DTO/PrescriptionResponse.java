package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionResponse {
    private Long id;
    private String patientName;
    private String medicineName;
    private Integer quantity;
    private String dosage;
    private String notes;
    private LocalDateTime prescribedAt;
    private Boolean dispensed;
    private String pharmacistEmail;
}

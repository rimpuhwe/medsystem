package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Insurance;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DispenseRecordResponse {
    private Long ordinanceId;
    private String patientReferenceNumber;
    @Enumerated(EnumType.STRING)
    private Insurance insuranceUsed;

    private Double coverageRate;

    private LocalDateTime dispensedAt;
    private Double totalAmount;
    private Double insuranceCoveredAmount;
    private Double amountPaid;
    private String medicineName;
    private Integer quantity;
}


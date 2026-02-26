package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DoctorConsultationHistoryResponse {
    private Long consultationId;
    private String patientReferenceNumber;
    private String patientName;
    private LocalDateTime consultationDate;
    private String diagnosisSummary;
    private int numberOfMedicines;
}


package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.PrescriptionStatus;
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
    private String patientReferenceNumber;
    private LocalDateTime prescribedAt;
    private PrescriptionStatus status;
    private java.util.List<PrescriptionItemDto> items;
}

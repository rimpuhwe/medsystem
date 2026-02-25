package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionRequest {
    private String patientName;
    private String medicineName;
    private Integer quantity;
    private String dosage;
    private String notes;
}

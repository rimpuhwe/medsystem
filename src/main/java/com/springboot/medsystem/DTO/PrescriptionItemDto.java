package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.PrescriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItemDto {
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String note;
    private PrescriptionStatus status;
}


package com.springboot.medsystem.prescription;

import com.springboot.medsystem.Enums.PrescriptionStatus;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItem {

    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;
    private String note;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;
}


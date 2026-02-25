package com.springboot.medsystem.prescription;

import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;
    private String medicineName;
    private Integer quantity;
    private String dosage;
    private String notes;
    private LocalDateTime prescribedAt;
    private Boolean dispensed = false;

    @ManyToOne
    @JoinColumn
    private PharmacyProfile pharmacist;
}

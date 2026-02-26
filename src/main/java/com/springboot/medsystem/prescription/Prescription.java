package com.springboot.medsystem.prescription;

import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Enums.PrescriptionStatus;
import com.springboot.medsystem.Patient.PatientProfile;
import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime prescribedAt;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status;

    @ManyToOne
    @JoinColumn
    private PatientProfile patient;

    @ManyToOne
    @JoinColumn
    private DoctorProfile doctor;

    @ManyToOne
    @JoinColumn
    private PharmacyProfile pharmacy;

    @ElementCollection
    private List<PrescriptionItem> items;
}

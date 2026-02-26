package com.springboot.medsystem.Doctor;

import com.springboot.medsystem.Enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "DoctorPatientRecords")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPatientRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String doctorEmail;

    private String patientReferenceNumber;
    private String patientName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String patientEmail;
    private String patientPhone;

    /**
     * Total number of consultations/prescriptions this doctor has issued to this patient.
     */
    private Integer numberOfPrescriptions;

    private LocalDateTime lastVisit;
}


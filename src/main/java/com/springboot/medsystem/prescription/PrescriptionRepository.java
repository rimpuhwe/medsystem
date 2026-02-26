package com.springboot.medsystem.prescription;

import com.springboot.medsystem.Enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientReferenceNumber(String patientReferenceNumber);
    List<Prescription> findByPatientReferenceNumberAndStatus(String patientReferenceNumber, PrescriptionStatus status);
    List<Prescription> findByStatus(PrescriptionStatus status);
}

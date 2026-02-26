package com.springboot.medsystem.Pharmacy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispenseRecordRepository extends JpaRepository<DispenseRecord, Long> {
    List<DispenseRecord> findByPharmacistEmail(String email);
    List<DispenseRecord> findByPharmacistEmailAndPatientReferenceNumber(String email, String patientReferenceNumber);
}


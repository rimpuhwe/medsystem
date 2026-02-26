package com.springboot.medsystem.Doctor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorPatientRecordRepository extends JpaRepository<DoctorPatientRecord, Long> {

    Optional<DoctorPatientRecord> findByDoctorEmailAndPatientReferenceNumber(String doctorEmail, String patientReferenceNumber);

    List<DoctorPatientRecord> findByDoctorEmail(String doctorEmail);
}


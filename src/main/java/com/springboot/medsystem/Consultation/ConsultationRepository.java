package com.springboot.medsystem.Consultation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByPatientReferenceNumber(String patientReferenceNumber);
}

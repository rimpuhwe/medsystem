package com.springboot.medsystem.prescription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPharmacistEmail(String email);
    List<Prescription> findByPharmacistEmailAndDispensedFalse(String email);
}

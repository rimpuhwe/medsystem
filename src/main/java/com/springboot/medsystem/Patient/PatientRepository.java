package com.springboot.medsystem.Patient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientProfile, Long> {
}
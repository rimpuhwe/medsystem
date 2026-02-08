package com.springboot.medsystem.Patient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByEmailOrPhone(String email, String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<PatientProfile> findByEmail(String email);
}
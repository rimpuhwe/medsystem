package com.springboot.medsystem.Pharmacy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyRepository extends JpaRepository<PharmacyProfile, Long> {
    Optional<PharmacyProfile> findByEmailOrPhone(String email, String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
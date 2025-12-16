package com.springboot.medsystem.Pharmacy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<PharmacyProfile, Long> {
}
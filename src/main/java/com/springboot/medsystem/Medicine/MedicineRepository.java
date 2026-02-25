package com.springboot.medsystem.Medicine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByPharmacyEmail(String email);
    List<Medicine> findByPharmacyEmailAndQuantityLessThan(String email, Integer lowStock);
    Optional<Medicine> findByNameAndPharmacyEmail(String name, String email);
}

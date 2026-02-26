package com.springboot.medsystem.Medicine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByPharmacyEmail(String email);
    List<Medicine> findByPharmacyEmailAndQuantityLessThan(String email, Integer lowStock);
    Optional<Medicine> findByNameAndPharmacyEmail(String name, String email);

    Optional<Medicine> findByIdAndPharmacyEmail(Long id, String email);
    List<Medicine> findByPharmacyEmailAndCategoryIgnoreCase(String email, String category);
    List<Medicine> findByPharmacyEmailAndNameContainingIgnoreCase(String email, String name);
    List<Medicine> findByPharmacyEmailAndExpiryDateBetween(String email, LocalDate start, LocalDate end);
}

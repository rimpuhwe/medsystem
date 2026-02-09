package com.springboot.medsystem.Doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<DoctorProfile, Long> {

	Optional<DoctorProfile> findByFullName(String name);

}
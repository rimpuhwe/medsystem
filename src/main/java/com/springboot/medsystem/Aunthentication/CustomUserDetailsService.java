package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;

    public CustomUserDetailsService(PatientRepository patientRepository,
            PharmacyRepository pharmacyRepository) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by identifier: {}", identifier);
        // Try patient
        var patientOpt = patientRepository.findByEmailOrPhone(identifier, identifier);
        if (patientOpt.isPresent()) {
            var patient = patientOpt.get();
            logger.debug("Patient found: email={}, phone={}, role={}, passwordHash={}", patient.getEmail(),
                    patient.getPhone(), patient.getRole(), patient.getPassword());
            return org.springframework.security.core.userdetails.User.builder()
                    .username(patient.getEmail() != null ? patient.getEmail() : patient.getPhone())
                    .password(patient.getPassword()) // stored hashed password
                    .roles(patient.getRole().name())
                    .build();
        }

        // Try pharmacist
        var pharmacyOpt = pharmacyRepository.findByEmailOrPhone(identifier, identifier);
        if (pharmacyOpt.isPresent()) {
            var pharmacy = pharmacyOpt.get();
            logger.debug("Pharmacist found: email={}, phone={}, role={}, passwordHash={}", pharmacy.getEmail(),
                    pharmacy.getPhone(), pharmacy.getRole(), pharmacy.getPassword());
            return org.springframework.security.core.userdetails.User.builder()
                    .username(pharmacy.getEmail() != null ? pharmacy.getEmail() : pharmacy.getPhone())
                    .password(pharmacy.getPassword()) // stored hashed password
                    .roles(pharmacy.getRole().name())
                    .build();
        }

        logger.warn("User not found with email or phone: {}", identifier);
        throw new UsernameNotFoundException("User not found with email or phone: " + identifier);
    }
}

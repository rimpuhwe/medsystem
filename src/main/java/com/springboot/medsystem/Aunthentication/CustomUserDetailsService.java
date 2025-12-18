package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;

    public CustomUserDetailsService(PatientRepository patientRepository,
                                    PharmacyRepository pharmacyRepository) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Try patient
        var patientOpt = patientRepository.findByEmailOrPhone(identifier, identifier);
        if (patientOpt.isPresent()) {
            var patient = patientOpt.get();
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
            return org.springframework.security.core.userdetails.User.builder()
                    .username(pharmacy.getEmail() != null ? pharmacy.getEmail() : pharmacy.getPhone())
                    .password(pharmacy.getPassword()) // stored hashed password
                    .roles(pharmacy.getRole().name())
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email or phone: " + identifier);
    }
}


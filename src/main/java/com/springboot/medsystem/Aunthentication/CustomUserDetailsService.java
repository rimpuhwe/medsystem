package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.Doctor.DoctorRepository;
import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import com.springboot.medsystem.Admin.AdminRepository;
import com.springboot.medsystem.Aunthentication.AdminUserDetails;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(PatientRepository patientRepository, PharmacyRepository pharmacyRepository, AdminRepository adminRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;

    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        // Try patient
        var patientOpt = patientRepository.findByEmailOrPhone(identifier, identifier);
        System.out.println("[DEBUG] Checking PatientRepository for: " + identifier);
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

        // Try doctor

        var doctorOpt = doctorRepository.findByEmail(identifier);
        if (doctorOpt.isPresent()) {
            var doctor = doctorOpt.get();
            return org.springframework.security.core.userdetails.User.builder()
                .username(doctor.getEmail()!= null ? doctor.getEmail() : doctor.getPhoneNumber())
                .password(doctor.getPassword()) // stored hashed password
                .roles(doctor.getRole().name())
                .build();
        } else {
            System.out.println("[DEBUG] Doctor not found with email: " + identifier);
        }

        // Try admin

        var adminOpt = adminRepository.findByEmail(identifier);
        if (adminOpt.isPresent()) {
            var admin = adminOpt.get();
            return new AdminUserDetails(admin);
        }

        throw new UsernameNotFoundException("User not found with email or phone: " + identifier);
    }
}

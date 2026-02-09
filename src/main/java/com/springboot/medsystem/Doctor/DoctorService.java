package com.springboot.medsystem.Doctor;

import com.springboot.medsystem.DTO.DoctorDto;
import com.springboot.medsystem.DTO.DoctorResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicRepository;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicRepository clinicRepository;

    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder, ClinicRepository clinicRepository) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.clinicRepository = clinicRepository;
    }

    public List<DoctorProfile> getAllDoctors() {

        return doctorRepository.findAll();
    }

    public DoctorProfile getDoctorByName(String name) {

        return doctorRepository.findByFullName(name).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }


    public DoctorResponse addDoctor(DoctorDto doctor) {
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setFullName(doctor.getFullName());
        doctorProfile.setEmail(doctor.getEmail());
        doctorProfile.setService(doctor.getService());
        doctorProfile.setPhoneNumber(doctor.getPhoneNumber());
        doctorProfile.setRole(com.springboot.medsystem.Enums.Role.DOCTOR);
        // Find clinic by name
        Clinic clinic = clinicRepository.findByClinicName(String.valueOf(doctor.getClinicName()))
            .orElseThrow(() -> new RuntimeException("Clinic not found: " + doctor.getClinicName()));
        doctorProfile.setClinic(clinic);
        doctorProfile.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctorRepository.save(doctorProfile);

        return new DoctorResponse();
    }

    public DoctorProfile getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

}

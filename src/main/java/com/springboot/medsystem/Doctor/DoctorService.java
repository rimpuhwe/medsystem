package com.springboot.medsystem.Doctor;


import com.springboot.medsystem.Aunthentication.OtpVerification;
import com.springboot.medsystem.Configuration.EmailService;
import com.springboot.medsystem.DTO.DoctorDto;
import com.springboot.medsystem.DTO.DoctorResponse;
import com.springboot.medsystem.Enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.springboot.medsystem.Aunthentication.OtpVerificationRepository;
import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicRepository clinicRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;

    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder, ClinicRepository clinicRepository, OtpVerificationRepository otpVerificationRepository, EmailService emailService) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.clinicRepository = clinicRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.emailService = emailService;
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
        doctorProfile.setPhone(doctor.getPhone());
        doctorProfile.setRole(Role.DOCTOR);

        Clinic clinic = clinicRepository.findByClinicName(String.valueOf(doctor.getClinicName()))
            .orElseThrow(() -> new RuntimeException("Clinic not found: " + doctor.getClinicName()));
        doctorProfile.setClinic(clinic);
        doctorProfile.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctorRepository.save(doctorProfile);

        String otp = generateOtp();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(doctor.getEmail());
        otpVerification.setOtp(otp);
        otpVerification.setVerified(false);
        otpVerification.setExpiry(LocalDateTime.now().plusMinutes(10));
        otpVerificationRepository.save(otpVerification);

        // Send OTP email
        emailService.sendVerificationEmail(doctorProfile, otp);

        return com.springboot.medsystem.DTO.DoctorResponse.builder()
                .Message("Doctor added successfully. The doctor must  use the OTP sent in their email to verify  their account.")
                .email(doctor.getEmail())
                .otp(otp)
                .role(Role.DOCTOR)
                .Timestamp(LocalDate.now())
                .build();
    }

    private String generateOtp() {
        java.util.Random random = new java.util.Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public DoctorProfile getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

}

package com.springboot.medsystem.Doctor;


import com.springboot.medsystem.DTO.DoctorDto;
import com.springboot.medsystem.DTO.DoctorResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.springboot.medsystem.Aunthentication.OtpVerificationRepository;
import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicRepository;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicRepository clinicRepository;
    private final OtpVerificationRepository otpVerificationRepository;

    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder, ClinicRepository clinicRepository, OtpVerificationRepository otpVerificationRepository) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.clinicRepository = clinicRepository;
        this.otpVerificationRepository = otpVerificationRepository;
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
        doctorProfile.setRole(com.springboot.medsystem.Enums.Role.DOCTOR);
        // Find clinic by name
        Clinic clinic = clinicRepository.findByClinicName(String.valueOf(doctor.getClinicName()))
            .orElseThrow(() -> new RuntimeException("Clinic not found: " + doctor.getClinicName()));
        doctorProfile.setClinic(clinic);
        doctorProfile.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctorRepository.save(doctorProfile);

        // Generate OTP
        String otp = generateOtp();
        com.springboot.medsystem.Aunthentication.OtpVerification otpVerification = new com.springboot.medsystem.Aunthentication.OtpVerification();
        otpVerification.setEmail(doctor.getEmail());
        otpVerification.setOtp(otp);
        otpVerification.setVerified(false);
        otpVerification.setExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        otpVerificationRepository.save(otpVerification);

        // Return DoctorResponse with OTP
        return com.springboot.medsystem.DTO.DoctorResponse.builder()
                .Message("Doctor added successfully. Use the OTP below to verify your email.")
                .email(doctor.getEmail())
                .otp(otp)
                .role(com.springboot.medsystem.Enums.Role.DOCTOR)
                .Timestamp(java.time.LocalDate.now())
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

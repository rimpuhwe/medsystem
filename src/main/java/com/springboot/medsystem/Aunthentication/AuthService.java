package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.Admin.Admin;
import com.springboot.medsystem.Admin.AdminRepository;
import com.springboot.medsystem.Configuration.EmailService;
import com.springboot.medsystem.DTO.*;
import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Doctor.DoctorRepository;
import com.springboot.medsystem.Enums.Role;
import com.springboot.medsystem.Patient.PatientProfile;
import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpVerificationRepository otpVerificationRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    public AuthService(
            PatientRepository patientRepository,
            PharmacyRepository pharmacyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            OtpVerificationRepository otpVerificationRepository,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            EmailService emailService) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.otpVerificationRepository = otpVerificationRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
    }

    public RegisterResponse registerPatient(PatientRegisterRequest request) {
        validateUniqueness(request);
        String referenceNumber = generateUniquePatientReferenceNumber();
        PatientProfile patient = new PatientProfile();
        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setRole(Role.PATIENT);
        patient.setInsurance(request.getInsurance());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setReferenceNumber(referenceNumber);
        patientRepository.save(patient);

        // Generate OTP and save
        String otp = generateOtp();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(patient.getEmail());
        otpVerification.setOtp(otp);
        otpVerification.setVerified(false);
        otpVerification.setExpiry(LocalDateTime.now().plusMinutes(10));
        otpVerificationRepository.save(otpVerification);

        // Send OTP email
        emailService.sendVerificationEmail(patient, otp);

        return new RegisterResponse(
                "Patient registration successful. Use the OTP below to verify your email.",
                referenceNumber,
                null,
                otp,
                LocalDate.now());
    }


    public PharmacyResponse registerPharmacist(PharmacyRegisterRequest request) {
        validateUniqueness(request);

        PharmacyProfile pharmacy = new PharmacyProfile();
        pharmacy.setFullName(request.getFullName());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setPhone(request.getPhone());
        pharmacy.setGender(request.getGender());
        pharmacy.setPassword(passwordEncoder.encode(request.getPassword()));
        pharmacy.setRole(Role.PHARMACIST);
        pharmacy.setPharmacyName(request.getPharmacyName());
        pharmacy.setLicenseNumber(request.getLicenseNumber());

        pharmacyRepository.save(pharmacy);

        String otp = generateOtp();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(pharmacy.getEmail());
        otpVerification.setOtp(otp);
        otpVerification.setVerified(false);
        otpVerification.setExpiry(LocalDateTime.now().plusMinutes(10));
        otpVerificationRepository.save(otpVerification);

        emailService.sendVerificationEmail(pharmacy, otp);

        return PharmacyResponse.builder()
            .message("Pharmacist registration successful. Use the OTP sent to your email  to validate  your account .")
            .email(pharmacy.getEmail())
            .otp(otp)
            .role(Role.PHARMACIST)
            .build();
    }
    public LoginResponse login(LoginRequest request) {

        String email = request.getIdentifier();

        if (email != null && email.contains("@")) {
            otpVerificationRepository.findByEmail(email).ifPresent(otp -> {
                if (!otp.isVerified()) {
                    throw new IllegalStateException("Email not verified. Please verify with OTP sent to your email.");
                }
            });
        }
        try {
            // Check if admin
            String identifier = request.getIdentifier();
            if (identifier != null && identifier.contains("@")) {
                var adminOpt = adminRepository.findByEmail(identifier);
                if (adminOpt.isPresent()) {
                    Admin admin = adminOpt.get();
                    if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                        throw new IllegalStateException("Invalid email or password");
                    }
                    AdminUserDetails adminUserDetails = new AdminUserDetails(admin);
                    String token = jwtService.generateToken(adminUserDetails);
                    return new LoginResponse("Login successfully", "ADMIN", token);
                }
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            assert userDetails != null;
            String token = jwtService.generateToken(userDetails);
            String role = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Role not found"));
            String roleForResponse = role.startsWith("ROLE_") ? role.substring(5) : role;
            return new LoginResponse("Login successfully", roleForResponse, token);
        } catch (BadCredentialsException e) {
            throw new IllegalStateException("Invalid email/phone or password");
        }
    }

    public void verifyOtp(OtpRequest request) {
        OtpVerification otp = otpVerificationRepository.findByEmailAndOtp(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new IllegalStateException("Invalid OTP or email."));
        if (otp.isVerified()) {
            throw new IllegalStateException("Email already verified.");
        }
        if (otp.getExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("OTP expired. Please request a new one.");
        }
        otp.setVerified(true);
        otpVerificationRepository.save(otp);
    }

    public void resendOtp(String email) {

        boolean found = false;
        if (patientRepository.existsByEmail(email)) found = true;
        if (pharmacyRepository.existsByEmail(email)) found = true;
        if (doctorRepository.findByEmail(email).isPresent()) found = true;
        if (!found) {
            throw new IllegalStateException("No registration found for this email.");
        }
        OtpVerification otp = otpVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No OTP found for this email."));
        String newOtp = generateOtp();
        otp.setOtp(newOtp);
        otp.setExpiry(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        otpVerificationRepository.save(otp);
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalStateException("Passwords do not match.");
        }
        boolean found = false;
        if (patientRepository.existsByEmail(request.getEmail())) {
            PatientProfile patient = patientRepository.findByEmail(request.getEmail()).orElseThrow();
            patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
            patientRepository.save(patient);
            found = true;
        }
        if (pharmacyRepository.existsByEmail(request.getEmail())) {
            PharmacyProfile pharmacy = pharmacyRepository.findByEmail(request.getEmail()).orElseThrow();
            pharmacy.setPassword(passwordEncoder.encode(request.getNewPassword()));
            pharmacyRepository.save(pharmacy);
            found = true;
        }
        if (doctorRepository.findByEmail(request.getEmail()).isPresent()) {
            DoctorProfile doctor = doctorRepository.findByEmail(request.getEmail()).orElseThrow();
            doctor.setPassword(passwordEncoder.encode(request.getNewPassword()));
            doctorRepository.save(doctor);
            found = true;
        }
        if (!found) {
            throw new IllegalStateException("Email not found.");
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void validateUniqueness(RegisterRequest request) {
        boolean emailExists = request.getEmail() != null &&
                (patientRepository.existsByEmail(request.getEmail()) ||
                        pharmacyRepository.existsByEmail(request.getEmail()));

        boolean phoneExists = request.getPhone() != null &&
                (patientRepository.existsByPhone(request.getPhone()) ||
                        pharmacyRepository.existsByPhone(request.getPhone()));

        if (emailExists || phoneExists) {
            throw new IllegalStateException("Email or phone already exists");
        }
    }

    private String generateUniquePatientReferenceNumber() {
        return "PAT-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}

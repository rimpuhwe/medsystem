package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.DTO.*;
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
import java.util.Objects;
import java.util.UUID;

import com.springboot.medsystem.DTO.OtpRequest;
import com.springboot.medsystem.DTO.ResetPasswordRequest;
import java.time.LocalDateTime;
import java.util.Random;

@Service

public class AuthService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpVerificationRepository otpVerificationRepository;

    public AuthService(
            PatientRepository patientRepository,
            PharmacyRepository pharmacyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            OtpVerificationRepository otpVerificationRepository) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.otpVerificationRepository = otpVerificationRepository;
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

        // For development: include OTP in response, do not send email
        return new RegisterResponse(
                "Patient registration successful. Use the OTP below to verify your email.",
                referenceNumber,
                null,
                otp,
                LocalDate.now());
    }

    public RegisterResponse registerPharmacist(PharmacyRegisterRequest request) {

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

        String username = pharmacy.getEmail() != null ? pharmacy.getEmail() : pharmacy.getPhone();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(username)
                .password(pharmacy.getPassword())
                .roles(pharmacy.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        pharmacyRepository.save(pharmacy);

        return new RegisterResponse(
                "Pharmacist registration successful",
                null, // Pharmacies do NOT get reference numbers
                token,
                null,
                LocalDate.now());
    }

    public LoginResponse login(LoginRequest request) {
        // Check OTP verification for patient/pharmacy
        String email = request.getIdentifier();
        // Only check for email, not phone
        if (email != null && email.contains("@")) {
            otpVerificationRepository.findByEmail(email).ifPresent(otp -> {
                if (!otp.isVerified()) {
                    throw new IllegalStateException("Email not verified. Please verify with OTP sent to your email.");
                }
            });
        }
        try {
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
        OtpVerification otp = otpVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No registration found for this email."));
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

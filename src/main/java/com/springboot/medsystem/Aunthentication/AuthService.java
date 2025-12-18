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

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            PatientRepository patientRepository,
            PharmacyRepository pharmacyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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

        String username = patient.getEmail() != null ? patient.getEmail() : patient.getPhone();
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(username)
                .password(patient.getPassword())
                .roles(patient.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        patientRepository.save(patient);

        return new RegisterResponse(
                "Patient registration successful",
                referenceNumber,
                token,
                LocalDate.now()
        );
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
                null,   // Pharmacies do NOT get reference numbers
                token,
                LocalDate.now()
        );
    }


    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );

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

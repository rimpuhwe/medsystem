package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.DTO.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.medsystem.DTO.OtpRequest;
import com.springboot.medsystem.DTO.ResetPasswordRequest;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for login and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {

        this.authService = authService;
    }

    @Operation(summary = "Register a new patient", description = "Registers a new patient, generates a reference number, and sends an OTP to the provided email for verification.")
    @PostMapping("/register/patient")
    public ResponseEntity<RegisterResponse> registerPatient(@Valid @RequestBody PatientRegisterRequest request) {
        RegisterResponse response = authService.registerPatient(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register a new pharmacy", description = "Registers a new pharmacy/pharmacist in the system.")
    @PostMapping("/register/pharmacy")
    public ResponseEntity<RegisterResponse> registerPharmacist(@Valid @RequestBody PharmacyRegisterRequest request) {
        RegisterResponse response = authService.registerPharmacist(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login", description = "Logs in a user (patient or pharmacist) after email verification. Returns a JWT token for authenticated access.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to the user's email during registration. Marks the email as verified.")
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok("OTP verified successfully. You can now login.");
    }

    @Operation(summary = "Reset password", description = "Resets the user's password after verifying the email exists. Requires new password and confirmation.")
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully.");
    }

    @Operation(summary = "Resend OTP", description = "Resends a new OTP to the user's email for verification if the previous OTP expired or was lost.")
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody OtpRequest request) {
        authService.resendOtp(request.getEmail());
        return ResponseEntity.ok("OTP resent to your email.");
    }
}

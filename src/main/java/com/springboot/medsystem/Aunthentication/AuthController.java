package com.springboot.medsystem.Aunthentication;

import com.springboot.medsystem.DTO.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for login and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<RegisterResponse> registerPatient(@Valid @RequestBody PatientRegisterRequest request) {
        RegisterResponse response = authService.registerPatient(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/pharmacy")
    public ResponseEntity<RegisterResponse> registerPharmacist(@Valid@RequestBody PharmacyRegisterRequest request) {
        RegisterResponse response = authService.registerPharmacist(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

package com.springboot.medsystem.Patient;

import com.springboot.medsystem.DTO.RegisterResponse;
import com.springboot.medsystem.Enums.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/patient/profile")
@Tag(name = "Patient Profile", description = "Endpoints for viewing and editing patient profile. Requires role: PATIENT and JWT authentication.")

public class PatientProfileController {
    private final PatientService patientService;

    public PatientProfileController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "Get patient profile", description = "Returns the authenticated patient's profile.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<PatientProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<PatientProfile> patientOpt = patientService.getProfileByEmail(email);
        return patientOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update patient profile", description = "Updates the authenticated patient's profile (except name). Requires JWT and PATIENT role.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping
    public ResponseEntity<PatientProfile> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PatientProfile updateRequest) {
        String email = userDetails.getUsername();
        PatientProfile updated = patientService.updateProfile(email, updateRequest);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}

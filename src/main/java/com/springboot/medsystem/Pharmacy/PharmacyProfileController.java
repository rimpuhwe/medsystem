package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.DTO.PharmacyProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/pharmacy/profile")
@Tag(name = "Pharmacy Profile", description = "Endpoints for viewing and editing pharmacy profile. Requires role: PHARMACIST and JWT authentication.")
public class PharmacyProfileController {
    private final PharmacyService pharmacyService;

    public PharmacyProfileController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @Operation(summary = "Get pharmacy profile", description = "Returns the authenticated pharmacist's profile.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<PharmacyProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<PharmacyProfile> pharmacyOpt = pharmacyService.getProfileByEmail(email);
        return pharmacyOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update pharmacy profile", description = "Updates the authenticated pharmacist's profile (except name). Requires JWT and PHARMACIST role.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping
    public ResponseEntity<PharmacyProfile> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody PharmacyProfileUpdateRequest updateRequest) {
        String email = userDetails.getUsername();
        PharmacyProfile updated = pharmacyService.updateProfile(email, updateRequest);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}

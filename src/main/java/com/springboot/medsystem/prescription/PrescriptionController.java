package com.springboot.medsystem.prescription;

import com.springboot.medsystem.DTO.PrescriptionRequest;
import com.springboot.medsystem.DTO.PrescriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/prescriptions")
@Tag(name = "Prescriptions", description = "Endpoints for dispensing and viewing prescriptions. Requires PHARMACIST role.")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @Operation(summary = "Add prescription", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PrescriptionResponse> addPrescription(@AuthenticationPrincipal UserDetails userDetails,
                                                                @RequestBody PrescriptionRequest request) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(prescriptionService.addPrescription(email, request));
    }

    @GetMapping
    @Operation(summary = "Get all prescriptions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PrescriptionResponse>> getMyPrescriptions(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(prescriptionService.getMyPrescriptions(userDetails.getUsername()));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending prescriptions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PrescriptionResponse>> getPending(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(prescriptionService.getPendingPrescriptions(userDetails.getUsername()));
    }
}

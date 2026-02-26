package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.DTO.DispenseRecordResponse;
import com.springboot.medsystem.DTO.InventoryAlertsResponse;
import com.springboot.medsystem.DTO.MedecineDto;
import com.springboot.medsystem.DTO.MedicineResponse;
import com.springboot.medsystem.DTO.PharmacyProfileUpdateRequest;
import com.springboot.medsystem.DTO.PrescriptionResponse;
import com.springboot.medsystem.Patient.PatientProfile;
import com.springboot.medsystem.Patient.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacist")
@Tag(name = "Pharmacy Profile", description = "Endpoints for viewing and editing pharmacy profile. Requires role: PHARMACIST and JWT authentication.")
public class PharmacyProfileController {
    private final PharmacyService pharmacyService;
    private final PatientService patientService;

    public PharmacyProfileController(PharmacyService pharmacyService, PatientService patientService) {
        this.pharmacyService = pharmacyService;
        this.patientService = patientService;
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

    // -------- Inventory routes --------

    @PostMapping("/inventory")
    @Operation(summary = "Add medicine to inventory", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MedicineResponse> addMedicine(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody MedecineDto dto) {
        return ResponseEntity.ok(pharmacyService.addMedicine(userDetails.getUsername(), dto));
    }

    @PutMapping("/inventory/{medicineId}")
    @Operation(summary = "Update inventory medicine", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MedicineResponse> updateMedicine(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable Long medicineId,
                                                           @RequestBody MedecineDto dto) {
        return ResponseEntity.ok(pharmacyService.updateMedicine(userDetails.getUsername(), medicineId, dto));
    }

    @DeleteMapping("/inventory/{medicineId}")
    @Operation(summary = "Delete inventory medicine", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteMedicine(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable Long medicineId) {
        pharmacyService.deleteMedicine(userDetails.getUsername(), medicineId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inventory/{medicineId}")
    @Operation(summary = "Get inventory medicine by id", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MedicineResponse> getMedicine(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable Long medicineId) {
        return ResponseEntity.ok(pharmacyService.getMedicine(userDetails.getUsername(), medicineId));
    }

    @GetMapping("/inventory")
    @Operation(summary = "List inventory (optional filters: name, category)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<MedicineResponse>> listInventory(@AuthenticationPrincipal UserDetails userDetails,
                                                                @RequestParam(required = false) String name,
                                                                @RequestParam(required = false) String category) {
        return ResponseEntity.ok(pharmacyService.listInventory(userDetails.getUsername(), name, category));
    }

    @GetMapping("/inventory/alerts")
    @Operation(summary = "Get inventory alerts (low stock + expiring soon)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<InventoryAlertsResponse> inventoryAlerts(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @RequestParam(defaultValue = "10") int lowStockThreshold,
                                                                   @RequestParam(defaultValue = "15") int expiryDays) {
        return ResponseEntity.ok(pharmacyService.getAlerts(userDetails.getUsername(), lowStockThreshold, expiryDays));
    }

    // -------- Patient lookup routes --------

    @GetMapping("/patients/{referenceNumber}")
    @Operation(summary = "Get patient profile by reference number", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PatientProfile> getPatientByReference(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(patientService.getPatientByReferenceNumber(referenceNumber));
    }

    // -------- Prescription + records routes --------

    @GetMapping("/prescriptions")
    @Operation(summary = "List prescriptions (optional referenceNumber filter)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<PrescriptionResponse>> listPrescriptions(@AuthenticationPrincipal UserDetails userDetails,
                                                                        @RequestParam(required = false) String referenceNumber,
                                                                        @RequestParam(defaultValue = "true") boolean pendingOnly) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(pharmacyService.getPharmacistPrescriptions(email, referenceNumber, pendingOnly));
    }

    @PostMapping("/prescriptions/{prescriptionId}/dispense")
    @Operation(summary = "Dispense a prescription (moves to records)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DispenseRecordResponse> dispense(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable Long prescriptionId) {
        return ResponseEntity.ok(pharmacyService.dispensePrescription(userDetails.getUsername(), prescriptionId));
    }

    @GetMapping("/records")
    @Operation(summary = "List dispensing records (optional referenceNumber filter)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<DispenseRecordResponse>> listRecords(@AuthenticationPrincipal UserDetails userDetails,
                                                                    @RequestParam(required = false) String referenceNumber) {
        return ResponseEntity.ok(pharmacyService.getDispenseRecords(userDetails.getUsername(), referenceNumber));
    }
}

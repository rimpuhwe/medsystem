package com.springboot.medsystem.Medicine;

import com.springboot.medsystem.DTO.MedicineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/medicines")
@Tag(name = "Medicines", description = "Endpoints for inventory management. Requires PHARMACIST role.")
public class MedicineController {
    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    @Operation(summary = "Get full inventory", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<MedicineResponse>> getInventory(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(medicineService.getInventory(userDetails.getUsername()));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<MedicineResponse>> getLowStock(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(medicineService.getLowStock(userDetails.getUsername(), threshold));
    }
}


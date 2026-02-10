package com.springboot.medsystem.Clinics;


import com.springboot.medsystem.DTO.ClinicDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clinic")
@Tag(name = "Clinic", description = "Endpoints for adding and view the clinic requirement. Requires role: PATIENT and ADMIN")

public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }


    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Get all clinics", description = "Returns a list of all clinics. Accessible by ADMIN and PATIENT.", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Clinic> getAllClinics() {
        return clinicService.getAllClinics();
    }



    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Search clinic by name", description = "Search for a clinic by name. Accessible by ADMIN and PATIENT.", security = @SecurityRequirement(name = "bearerAuth"))
    public Clinic searchClinicsByName(@RequestParam String name) {
        return clinicService.getClinicByName(name);
    }



    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new clinic", description = "Adds a new clinic to the system. Accessible by ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public Clinic addClinic(@RequestBody ClinicDto clinic) {
        return clinicService.addClinic(clinic);
    }
}

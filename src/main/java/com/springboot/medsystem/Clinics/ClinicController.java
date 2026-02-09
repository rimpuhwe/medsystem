package com.springboot.medsystem.Clinics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {
    @Autowired
    private ClinicService clinicService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<Clinic> getAllClinics() {
        return clinicService.getAllClinics();
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Clinic searchClinicsByName(@RequestParam String name) {
        return clinicService.getClinicByName(name);
    }

    @GetMapping(params = "name")
    @PreAuthorize("hasRole('PATIENT')")
    public Clinic getClinicByName(@RequestParam String name) {
        return clinicService.getClinicByName(name);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Clinic addClinic(@RequestBody Clinic clinic) {
        return clinicService.addClinic(clinic);
    }
}

package com.springboot.medsystem.Doctor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // ADMIN and PATIENT can view all doctors
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public List<DoctorProfile> getAllDoctors() {
        return doctorService.getAllDoctors();
    }


    // ADMIN and PATIENT can search doctors by name
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public DoctorProfile searchDoctorsByName(@RequestParam String name) {
        return doctorService.getDoctorByName(name);
    }

    // DOCTOR can view their own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfile getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return doctorService.getAllDoctors().stream()
                .filter(doc -> email.equals(doc.getEmail()))
                .findFirst().orElse(null);
    }

    // ADMIN can add doctors
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorProfile addDoctor(@RequestBody DoctorProfile doctorProfile) {
        return doctorService.addDoctor(doctorProfile);
    }
}

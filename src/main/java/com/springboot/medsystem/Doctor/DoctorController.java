package com.springboot.medsystem.Doctor;

import com.springboot.medsystem.DTO.DoctorDto;
import com.springboot.medsystem.DTO.DoctorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctor Profile", description = "Endpoints for the doctor's profile and all tasks a Doctor can perform. Requires role: PATIENT , Aunthenticated DOCTOR and ADMIN")

public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("all-doctors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Get all doctors", description = "Returns a list of all doctors. Accessible by ADMIN and PATIENT.", security = @SecurityRequirement(name = "bearerAuth"))
    public List<DoctorProfile> getAllDoctors() {
        return doctorService.getAllDoctors();
    }



    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Search doctor by name", description = "Search for a doctor by name. Accessible by ADMIN and PATIENT.", security = @SecurityRequirement(name = "bearerAuth"))
    public DoctorProfile searchDoctorsByName(@RequestParam String name) {
        return doctorService.getDoctorByName(name);
    }



    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get own doctor profile", description = "Returns the authenticated doctor's profile. Accessible by DOCTOR.", security = @SecurityRequirement(name = "bearerAuth"))
    public DoctorProfile getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        DoctorProfile doctor = doctorService.getDoctorByEmail(email);
        if (!doctor.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to another doctor's profile");
        }
        return doctor;
    }

   
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new doctor", description = "Adds a new doctor to the system. Accessible by ADMIN. Returns OTP for verification.", security = @SecurityRequirement(name = "bearerAuth"))
    public DoctorResponse addDoctor(@RequestBody DoctorDto doctor) {
        return doctorService.addDoctor(doctor);
    }
}

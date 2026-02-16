package com.springboot.medsystem.Patient;

import com.springboot.medsystem.DTO.PatientProfileUpdateRequest;
import com.springboot.medsystem.DTO.PatientQueueJoinRequest;
import com.springboot.medsystem.DTO.QueuePosition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient")
@Tag(name = "Patient Profile", description = "Endpoints for viewing, editing, joining clinic queue, get the medical report and  patient profile  . Requires role: PATIENT and JWT authentication.")

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

    @Operation(summary = "Update patient profile", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/update-profile")
    public ResponseEntity<PatientProfile> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody PatientProfileUpdateRequest updateRequest) {
        String email = userDetails.getUsername();
        PatientProfile updated = patientService.updateProfile(email, updateRequest);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }


    @Operation(summary = "Join clinic/service queue", description = "Join the queue for a specific clinic and service. Provide clinic name, service (capitalized), and optional doctor name. Returns your queue position, updated in real time.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/queue/join")
    public ResponseEntity<QueuePosition> joinQueue(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PatientQueueJoinRequest joinRequest) {
        return ResponseEntity.ok(patientService.joinQueue(userDetails, joinRequest));
    }


    /**
     * WebSocket endpoint for real-time queue position updates. Clients subscribe to /topic/queue/positions.
     */

    @MessageMapping("/queue/position")
    @SendTo("/topic/queue/positions")
    public List<QueuePosition> getPositionsWS(PatientQueueJoinRequest joinRequest) {
        return patientService.getServiceQueue(joinRequest.getClinicName(), joinRequest.getService());
    }



    @Operation(summary = "Get queue summary for clinic/service", description = "Returns the total count of patients in the queue for a specific clinic and service, the service, the clinic, and the list of doctors for that clinic/service.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/queue/")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getQueueSummary(@RequestParam String clinicName, @RequestParam String service , @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(patientService.getQueueSummary(clinicName, service, userDetails));
    }


}

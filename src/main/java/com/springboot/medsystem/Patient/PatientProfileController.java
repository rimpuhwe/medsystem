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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.springboot.medsystem.Consultation.ConsultationRepository;
import com.springboot.medsystem.Consultation.Consultation;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
@Tag(name = "Patient Profile", description = "Endpoints for viewing, editing, joining clinic queue, get the medical report and  patient profile  . Requires role: PATIENT and JWT authentication.")

public class PatientProfileController {
    private final PatientService patientService;
    private final ConsultationRepository consultationRepository;

    public PatientProfileController(PatientService patientService, ConsultationRepository consultationRepository) {
        this.patientService = patientService;
        this.consultationRepository = consultationRepository;
    }

    @Operation(summary = "Get medical history and conditions", description = "Returns the patient's chronic diseases, allergies, and medical history.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/medical-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMedicalHistory(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<PatientProfile> patientOpt = patientService.getProfileByEmail(email);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PatientProfile patient = patientOpt.get();
        String refNumber = patient.getReferenceNumber();
        List<Consultation> consultations = consultationRepository.findByPatientReferenceNumber(refNumber);
        // Collect all unique chronic diseases and allergies from consultations
        List<String> chronicDiseases = consultations.stream()
                .flatMap(c -> c.getChronicDiseases() != null ? c.getChronicDiseases().stream() : java.util.stream.Stream.empty())
                .distinct().collect(Collectors.toList());
        List<String> allergies = consultations.stream()
                .flatMap(c -> c.getAllergies() != null ? c.getAllergies().stream() : java.util.stream.Stream.empty())
                .distinct().collect(Collectors.toList());
        // Format medical history
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Object> medicalHistory = consultations.stream().map(c -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", c.getConsultationDate() != null ? c.getConsultationDate().format(formatter) : null);
            entry.put("Doctor", c.getDoctorName());
            entry.put("diagnosed", c.getDiagnosis());
            entry.put("prescribed", c.getMedicines());
            // Clinic name is not stored in Consultation, so left blank or can be enhanced if needed
            entry.put("clinic", "");
            return entry;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("chronicDiseases", chronicDiseases);
        result.put("allergies", allergies);
        result.put("medical-history", medicalHistory);
        return ResponseEntity.ok(result);
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

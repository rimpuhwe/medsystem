package com.springboot.medsystem.Consultation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Metadata to link consultation to patient and doctor
    private String patientReferenceNumber;
    private String doctorName;

    private String diagnosis;

    // Free-form summary of prescribed medicines for this consultation
    @ElementCollection
    private List<String> prescriptionItem;

    // Legacy field used by existing history logic; kept for backward compatibility
    @ElementCollection
    private List<String> medicines;
    @ElementCollection
    private List<String> chronicDiseases;
    @ElementCollection
    private List<String> allergies;
    private LocalDateTime consultationDate;

}

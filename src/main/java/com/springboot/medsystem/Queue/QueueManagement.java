package com.springboot.medsystem.Queue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot.medsystem.Enums.QueueStatus;
import com.springboot.medsystem.Clinics.Clinic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class QueueManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String patientReferenceNumber;
    private int position;
    @ManyToOne
    @JsonIgnore
    private Clinic clinic;

    private String service;
    private String doctorName;

    private LocalDate queueDate;

    @Enumerated(EnumType.STRING)
    private QueueStatus status;

}

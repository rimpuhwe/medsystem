package com.springboot.medsystem.Queue;

import com.springboot.medsystem.Clinics.Clinic;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Clinic clinic;

    private String service;
    private String doctorName;

}

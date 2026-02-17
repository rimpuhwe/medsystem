package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Enums.QueueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class QueuePosition {

    private String patientReferenceNumber;
    private int position;
    @ManyToOne
    private Clinic clinic;

    private String service;
    private String doctorName;
    @Enumerated(EnumType.STRING)
    private QueueStatus status;

}

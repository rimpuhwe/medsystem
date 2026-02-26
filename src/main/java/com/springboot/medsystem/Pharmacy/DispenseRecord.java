package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.Enums.Insurance;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DispenseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientReferenceNumber;

    @Enumerated(EnumType.STRING)
    private Insurance insuranceUsed;

    /**
     * 0.85 = 85% coverage by insurance.
     */
    private Double coverageRate;

    private Long ordinanceId;

    private LocalDateTime dispensedAt;

    private Double totalAmount;
    private Double insuranceCoveredAmount;
    private Double amountPaid;

    private String medicineName ;
    private Integer quantity;

    @ManyToOne
    @JoinColumn
    private PharmacyProfile pharmacist;
}


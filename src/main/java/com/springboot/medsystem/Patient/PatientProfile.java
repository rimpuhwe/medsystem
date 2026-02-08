package com.springboot.medsystem.Patient;

import com.springboot.medsystem.Enums.Insurance;
import com.springboot.medsystem.User.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Patients")
public class PatientProfile extends Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Insurance insurance;

    private String insuranceNumber;
    private Date dateOfBirth;

    private String referenceNumber;
}

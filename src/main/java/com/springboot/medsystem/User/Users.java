package com.springboot.medsystem.User;

import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Patient.PatientProfile;
import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    private List<DoctorProfile> doctors;

    @Transient
    private List<PatientProfile> patients;

    @Transient
    private List<PharmacyProfile> pharmacists;
}

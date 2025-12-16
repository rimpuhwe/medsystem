package com.springboot.medsystem.User;

import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Patient.PatientProfile;
import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import jakarta.persistence.*;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Entity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.util.List;


@Entity
@TypeDef(name = "json", typeClass = JsonType.class)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private List<DoctorProfile> doctors;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private List<PatientProfile> patients;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private List<PharmacyProfile> pharmacist;



}

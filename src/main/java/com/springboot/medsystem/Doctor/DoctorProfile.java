package com.springboot.medsystem.Doctor;

import jakarta.persistence.*;

@Entity
@Table(name = "Doctors")
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;


}

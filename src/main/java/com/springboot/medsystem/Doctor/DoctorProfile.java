package com.springboot.medsystem.Doctor;

import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Enums.Role;
import com.springboot.medsystem.Enums.Service;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Doctors")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String fullName;
    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "clinic_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Clinic clinic;


    @Enumerated(EnumType.STRING)
    private Service service;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String password;

}

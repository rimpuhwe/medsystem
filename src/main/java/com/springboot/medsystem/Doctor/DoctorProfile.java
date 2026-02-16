package com.springboot.medsystem.Doctor;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonBackReference
    private Clinic clinic;

    @Enumerated(EnumType.STRING)
    private Service service;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Role role;

    @JsonIgnore
    private String password;

}

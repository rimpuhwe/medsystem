package com.springboot.medsystem.Clinics;

import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Enums.Service;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String clinicName;

    @ElementCollection(targetClass = Service.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "clinic_services", joinColumns = @JoinColumn(name = "clinic_id"))
    @Column(name = "service")
    private Set<Service> services;

    @OneToMany(mappedBy = "clinic")
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<DoctorProfile> doctors;
}

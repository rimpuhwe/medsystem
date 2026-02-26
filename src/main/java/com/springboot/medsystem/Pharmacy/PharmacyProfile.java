package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.Medicine.Medicine;
import com.springboot.medsystem.User.Profile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Pharmacists")
public class PharmacyProfile extends Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String pharmacyName;
    private String licenseNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "pharmacy", fetch = FetchType.LAZY)
    private List<Medicine> medicines;

}

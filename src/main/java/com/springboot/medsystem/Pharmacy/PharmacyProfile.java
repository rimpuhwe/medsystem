package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.User.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}

package com.springboot.medsystem.Medicine;

import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Medicine {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String batch;
    private Integer quantity;
    private Double price;

    private String category;

    private LocalDate expiryDate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn
    private PharmacyProfile pharmacy;



}

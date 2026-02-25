package com.springboot.medsystem.Medicine;

import com.springboot.medsystem.Pharmacy.PharmacyProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Medicine {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;
    private Integer quantity;
    private Double price;

    private String category;

    @ManyToOne
    @JoinColumn
    private PharmacyProfile pharmacy;



}

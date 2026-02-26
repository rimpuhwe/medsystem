package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedecineDto {
    private String name;
    private String batch;
    private Integer quantity;
    private Double price;

    private String category;

    private LocalDate expiryDate;
}

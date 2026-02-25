package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private String name;
    private String brand;
    private Integer quantity;
    private Double price;
    private String category;
}

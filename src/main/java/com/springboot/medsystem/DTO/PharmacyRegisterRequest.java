package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PharmacyRegisterRequest extends RegisterRequest{

    private String pharmacyName;
    private String licenseNumber;
}

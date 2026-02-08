package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Gender;
import lombok.Data;

@Data
public class PharmacyProfileUpdateRequest {
    private String email;
    private String phone;
    private Gender gender;
    private String pharmacyName;
    private String licenseNumber;
}
package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Gender;
import com.springboot.medsystem.Enums.Insurance;
import lombok.Data;
import java.util.Date;

@Data
public class PatientProfileUpdateRequest {
    private String email;
    private String phone;
    private Gender gender;
    private Insurance insurance;
    private String insuranceNumber;
    private Date dateOfBirth;
}
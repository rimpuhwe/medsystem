package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Gender;
import com.springboot.medsystem.Enums.Insurance;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientRegisterRequest extends RegisterRequest{

    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Insurance insurance;

    private String insuranceNumber;
}

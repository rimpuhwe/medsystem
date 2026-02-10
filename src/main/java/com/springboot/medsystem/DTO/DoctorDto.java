package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Enums.Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDto {

    private String fullName;
    private String email;
    private String phone;



    private String clinicName;


    private Service service;

    private String password;
}

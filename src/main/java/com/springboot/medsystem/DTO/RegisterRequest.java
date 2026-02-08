package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Gender;
import com.springboot.medsystem.Enums.Insurance;
import com.springboot.medsystem.Enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String fullName;
    private String email;
    private String phone;
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}

package com.springboot.medsystem.User;

import com.springboot.medsystem.Enums.Gender;
import com.springboot.medsystem.Enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    protected String fullName;
    @Column(nullable = false, unique = true)
    protected String email;
    @Column(unique = true)
    protected String phone;
    private String password;

    @Enumerated(EnumType.STRING)
    protected Role role;

    @Enumerated(EnumType.STRING)
    protected Gender gender;

}

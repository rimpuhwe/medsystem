package com.springboot.medsystem.Admin;

import com.springboot.medsystem.Enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String fullName;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

}

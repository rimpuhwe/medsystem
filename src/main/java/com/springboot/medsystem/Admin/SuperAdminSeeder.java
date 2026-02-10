package com.springboot.medsystem.Admin;

import com.springboot.medsystem.Enums.Role;
import com.springboot.medsystem.User.Users;
import com.springboot.medsystem.Aunthentication.JwtService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminSeeder implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository repository;
    private final JwtService jwtService;
    public SuperAdminSeeder(PasswordEncoder passwordEncoder, AdminRepository repository, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.jwtService = jwtService;
    }


    @Override
    public void run(String... args) {
        String email = "admin@medsystem.com";
        if (repository.findByEmail(email).isEmpty()) {
            Admin admin = new Admin();
            admin.setFullName("Super Admin");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            repository.save(admin);
            // Generate JWT token for seeded admin
            AdminUserDetails adminUserDetails = new AdminUserDetails(admin);
            String token = jwtService.generateToken(adminUserDetails);
            System.out.println("Super admin seeded. JWT token: " + token);
        } else {
            System.out.println("Super admin already exists.");
        }
    }
}

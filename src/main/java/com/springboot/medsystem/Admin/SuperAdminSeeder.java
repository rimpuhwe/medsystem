package com.springboot.medsystem.Admin;

import com.springboot.medsystem.Enums.Role;
import com.springboot.medsystem.User.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminSeeder implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository repository;
    public SuperAdminSeeder(PasswordEncoder passwordEncoder , AdminRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
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
            System.out.println("Super admin seeded.");
        } else {
            System.out.println("Super admin already exists.");
        }
    }
}

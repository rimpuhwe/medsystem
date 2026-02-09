package com.springboot.medsystem.Doctor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<DoctorProfile> getAllDoctors() {

        return doctorRepository.findAll();
    }

    public DoctorProfile getDoctorByName(String name) {

        return doctorRepository.findByFullName(name).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public DoctorProfile addDoctor(DoctorProfile doctorProfile) {

        return doctorRepository.save(doctorProfile);
    }

}

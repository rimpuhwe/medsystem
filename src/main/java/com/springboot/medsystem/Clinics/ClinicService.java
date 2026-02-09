package com.springboot.medsystem.Clinics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;
    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    public List<Clinic> getAllClinics() {

        return clinicRepository.findAll();
    }

    public Clinic addClinic(Clinic clinic) {

        return clinicRepository.save(clinic);
    }

    public Clinic getClinicByName(String name) {
        return clinicRepository.findByClinicName(name).orElseThrow(() -> new RuntimeException("clinic not found "));
    }
}

package com.springboot.medsystem.Patient;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Optional<PatientProfile> getProfileByEmail(String email) {

        return patientRepository.findByEmail(email);
    }

    public PatientProfile updateProfile(String email, PatientProfile updateRequest) {
        Optional<PatientProfile> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isEmpty()) {
            return null;
        }
        PatientProfile patient = patientOpt.get();
        // patient.setFullName(updateRequest.getFullName()); // Do not allow name change
        patient.setEmail(updateRequest.getEmail());
        patient.setPhone(updateRequest.getPhone());
        patient.setGender(updateRequest.getGender());
        patient.setInsurance(updateRequest.getInsurance());
        patient.setInsuranceNumber(updateRequest.getInsuranceNumber());
        patient.setDateOfBirth(updateRequest.getDateOfBirth());
        return patientRepository.save(patient);
    }
}

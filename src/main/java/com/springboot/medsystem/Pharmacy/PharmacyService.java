package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.DTO.PharmacyProfileUpdateRequest;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    public Optional<PharmacyProfile> getProfileByEmail(String email) {
        return pharmacyRepository.findByEmail(email);
    }

    public PharmacyProfile updateProfile(String email, PharmacyProfileUpdateRequest updateRequest) {
        Optional<PharmacyProfile> pharmacyOpt = pharmacyRepository.findByEmail(email);
        if (pharmacyOpt.isEmpty()) {
            return null;
        }
        PharmacyProfile pharmacy = pharmacyOpt.get();
        // pharmacy.setFullName(updateRequest.getFullName()); // Do not allow name change
        pharmacy.setEmail(updateRequest.getEmail());
        pharmacy.setPhone(updateRequest.getPhone());
        pharmacy.setGender(updateRequest.getGender());
        pharmacy.setPharmacyName(updateRequest.getPharmacyName());
        pharmacy.setLicenseNumber(updateRequest.getLicenseNumber());
        return pharmacyRepository.save(pharmacy);
    }
}

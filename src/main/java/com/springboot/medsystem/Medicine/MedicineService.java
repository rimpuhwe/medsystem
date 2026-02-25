package com.springboot.medsystem.Medicine;

import com.springboot.medsystem.DTO.MedicineResponse;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineService {
    private final MedicineRepository medicineRepository;
    private final PharmacyRepository pharmacyRepository;

    public MedicineService(MedicineRepository medicineRepository, PharmacyRepository pharmacyRepository) {
        this.medicineRepository = medicineRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    public List<MedicineResponse> getInventory(String pharmacistEmail) {
        return medicineRepository.findByPharmacyEmail(pharmacistEmail)
                .stream()
                .map(m -> new MedicineResponse(
                        m.getId(),
                        m.getName(),
                        m.getBrand(),
                        m.getQuantity(),
                        m.getPrice(),
                        m.getCategory()
                ))
                .collect(Collectors.toList());
    }

    public List<MedicineResponse> getLowStock(String pharmacistEmail, Integer threshold) {
        return medicineRepository.findByPharmacyEmailAndQuantityLessThan(pharmacistEmail, threshold)
                .stream()
                .map(m -> new MedicineResponse(
                        m.getId(),
                        m.getName(),
                        m.getBrand(),
                        m.getQuantity(),
                        m.getPrice(),
                        m.getCategory()
                ))
                .collect(Collectors.toList());
    }
}

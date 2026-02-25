package com.springboot.medsystem.prescription;

import com.springboot.medsystem.DTO.PrescriptionRequest;
import com.springboot.medsystem.DTO.PrescriptionResponse;
import com.springboot.medsystem.Medicine.MedicineRepository;
import com.springboot.medsystem.Pharmacy.PharmacyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {


    private final PrescriptionRepository prescriptionRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PharmacyRepository pharmacyRepository,
                               MedicineRepository medicineRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
    }

    public PrescriptionResponse addPrescription(String pharmacistEmail, PrescriptionRequest request) {
        var pharmacist = pharmacyRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        // Check medicine stock
        var medicineOpt = medicineRepository.findByNameAndPharmacyEmail(request.getMedicineName(), pharmacistEmail);
        if (medicineOpt.isEmpty() || medicineOpt.get().getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock or medicine not found");
        }

        var medicine = medicineOpt.get();
        medicine.setQuantity(medicine.getQuantity() - request.getQuantity());
        medicineRepository.save(medicine);

        Prescription prescription = new Prescription();
        prescription.setPatientName(request.getPatientName());
        prescription.setMedicineName(request.getMedicineName());
        prescription.setQuantity(request.getQuantity());
        prescription.setDosage(request.getDosage());
        prescription.setNotes(request.getNotes());
        prescription.setPrescribedAt(LocalDateTime.now());
        prescription.setPharmacist(pharmacist);
        prescription.setDispensed(true);
        prescriptionRepository.save(prescription);

        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPatientName(),
                prescription.getMedicineName(),
                prescription.getQuantity(),
                prescription.getDosage(),
                prescription.getNotes(),
                prescription.getPrescribedAt(),
                prescription.getDispensed(),
                pharmacistEmail
        );
    }

    public List<PrescriptionResponse> getMyPrescriptions(String pharmacistEmail) {
        return prescriptionRepository.findByPharmacistEmail(pharmacistEmail)
                .stream()
                .map(p -> new PrescriptionResponse(
                        p.getId(),
                        p.getPatientName(),
                        p.getMedicineName(),
                        p.getQuantity(),
                        p.getDosage(),
                        p.getNotes(),
                        p.getPrescribedAt(),
                        p.getDispensed(),
                        pharmacistEmail
                ))
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponse> getPendingPrescriptions(String pharmacistEmail) {
        return prescriptionRepository.findByPharmacistEmailAndDispensedFalse(pharmacistEmail)
                .stream()
                .map(p -> new PrescriptionResponse(
                        p.getId(),
                        p.getPatientName(),
                        p.getMedicineName(),
                        p.getQuantity(),
                        p.getDosage(),
                        p.getNotes(),
                        p.getPrescribedAt(),
                        p.getDispensed(),
                        pharmacistEmail
                ))
                .collect(Collectors.toList());
    }
}

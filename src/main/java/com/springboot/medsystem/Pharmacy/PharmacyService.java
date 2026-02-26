package com.springboot.medsystem.Pharmacy;

import com.springboot.medsystem.DTO.DispenseRecordResponse;
import com.springboot.medsystem.DTO.InventoryAlertsResponse;
import com.springboot.medsystem.DTO.MedecineDto;
import com.springboot.medsystem.DTO.MedicineResponse;
import com.springboot.medsystem.DTO.PrescriptionResponse;
import com.springboot.medsystem.Enums.Insurance;
import com.springboot.medsystem.DTO.PharmacyProfileUpdateRequest;
import com.springboot.medsystem.Medicine.Medicine;
import com.springboot.medsystem.Medicine.MedicineRepository;
import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.Pharmacy.DispenseRecord;
import com.springboot.medsystem.Pharmacy.DispenseRecordRepository;
import com.springboot.medsystem.prescription.Prescription;
import com.springboot.medsystem.prescription.PrescriptionItem;
import com.springboot.medsystem.prescription.PrescriptionRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DispenseRecordRepository dispenseRecordRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository,
                           MedicineRepository medicineRepository,
                           PatientRepository patientRepository,
                           PrescriptionRepository prescriptionRepository,
                           DispenseRecordRepository dispenseRecordRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository= medicineRepository;
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.dispenseRecordRepository = dispenseRecordRepository;
    }

    /** Business logic: load the authenticated pharmacist profile by email. */
    public Optional<PharmacyProfile> getProfileByEmail(String email) {
        return pharmacyRepository.findByEmail(email);
    }

    /** Business logic: update core pharmacist profile fields (contact, gender, pharmacy info). */
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

    /** Business logic: add a new stock item belonging to the authenticated pharmacist. */
    public Medicine addNewStock(MedecineDto medecineDto , UserDetails userDetails){
        String email = userDetails.getUsername();
        Medicine medicine = new Medicine();
        var pharmacyProfile = pharmacyRepository.findByEmail(email);
        if (pharmacyProfile.isPresent()) {
            medicine.setName(medecineDto.getName());
            medicine.setBatch(medecineDto.getBatch());
            medicine.setQuantity(medecineDto.getQuantity());
            medicine.setPrice(medecineDto.getPrice());
            medicine.setCategory(medecineDto.getCategory());
            medicine.setExpiryDate(medecineDto.getExpiryDate());
            medicine.setPharmacy(pharmacyProfile.get());
        }
     return medicineRepository.save(medicine);
    }

    /** Business logic: create a medicine row in inventory for the pharmacist. */
    public MedicineResponse addMedicine(String pharmacistEmail, MedecineDto dto) {
        PharmacyProfile pharmacist = pharmacyRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        Medicine medicine = new Medicine();
        applyDto(medicine, dto);
        medicine.setPharmacy(pharmacist);

        return toResponse(medicineRepository.save(medicine));
    }

    /** Business logic: update an existing medicine row owned by this pharmacist. */
    public MedicineResponse updateMedicine(String pharmacistEmail, Long medicineId, MedecineDto dto) {
        Medicine medicine = medicineRepository.findByIdAndPharmacyEmail(medicineId, pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        applyDto(medicine, dto);
        return toResponse(medicineRepository.save(medicine));
    }

    /** Business logic: remove a medicine from the pharmacist inventory. */
    public void deleteMedicine(String pharmacistEmail, Long medicineId) {
        Medicine medicine = medicineRepository.findByIdAndPharmacyEmail(medicineId, pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        medicineRepository.delete(medicine);
    }

    /** Business logic: fetch a single medicine from the pharmacist inventory. */
    public MedicineResponse getMedicine(String pharmacistEmail, Long medicineId) {
        return medicineRepository.findByIdAndPharmacyEmail(medicineId, pharmacistEmail)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
    }

    /** Business logic: list inventory with optional filters (by name or category) for a pharmacist. */
    public List<MedicineResponse> listInventory(String pharmacistEmail, String name, String category) {
        List<Medicine> meds;
        if (name != null && !name.isBlank()) {
            meds = medicineRepository.findByPharmacyEmailAndNameContainingIgnoreCase(pharmacistEmail, name.trim());
        } else if (category != null && !category.isBlank()) {
            meds = medicineRepository.findByPharmacyEmailAndCategoryIgnoreCase(pharmacistEmail, category.trim());
        } else {
            meds = medicineRepository.findByPharmacyEmail(pharmacistEmail);
        }

        return meds.stream()
                .sorted(Comparator.comparing(Medicine::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    /** Business logic: compute low-stock and expiring-soon alerts for pharmacist inventory. */
    public InventoryAlertsResponse getAlerts(String pharmacistEmail, int lowStockThreshold, int expiryDays) {
        List<MedicineResponse> lowStock = medicineRepository
                .findByPharmacyEmailAndQuantityLessThan(pharmacistEmail, lowStockThreshold + 1)
                .stream()
                .sorted(Comparator.comparing(Medicine::getQuantity))
                .map(this::toResponse)
                .toList();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(expiryDays);
        List<MedicineResponse> expiringSoon = medicineRepository
                .findByPharmacyEmailAndExpiryDateBetween(pharmacistEmail, start, end)
                .stream()
                .sorted(Comparator.comparing(Medicine::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toResponse)
                .toList();

        return new InventoryAlertsResponse(lowStock, expiringSoon);
    }

    /** Business logic: list prescriptions for this pharmacist, optionally filtered by patient reference and status. */
    public List<PrescriptionResponse> getPharmacistPrescriptions(String pharmacistEmail, String referenceNumber, boolean pendingOnly) {
        List<Prescription> prescriptions;
        if (referenceNumber != null && !referenceNumber.isBlank()) {
            prescriptions = pendingOnly
                    ? prescriptionRepository.findByPatientReferenceNumberAndStatus(referenceNumber.trim(), com.springboot.medsystem.Enums.PrescriptionStatus.ACTIVE)
                    : prescriptionRepository.findByPatientReferenceNumber(referenceNumber.trim());
        } else {
            prescriptions = pendingOnly
                    ? prescriptionRepository.findByStatus(com.springboot.medsystem.Enums.PrescriptionStatus.ACTIVE)
                    : prescriptionRepository.findAll();
        }

        return prescriptions.stream()
                .map(this::toPrescriptionResponse)
                .toList();
    }

    /** Business logic: dispense a prescription and persist a financial/clinical record for the pharmacist. */
    public DispenseRecordResponse dispensePrescription(String pharmacistEmail, Long prescriptionId) {
        var pharmacist = pharmacyRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (prescription.getStatus() == com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED) {
            throw new RuntimeException("Prescription already dispensed");
        }

        String ref = prescription.getPatient() != null ? prescription.getPatient().getReferenceNumber() : null;
        if (ref == null || ref.isBlank()) {
            throw new RuntimeException("Patient reference number is required to dispense");
        }

        var patient = patientRepository.findByReferenceNumber(ref);
        if (patient == null) {
            throw new RuntimeException("Patient not found");
        }

        // For pricing, assume all items share same unit price by medicine name and sum across items.
        double totalAmount = 0.0;
        for (PrescriptionItem item : prescription.getItems()) {
            var medicine = medicineRepository.findByNameAndPharmacyEmail(item.getMedicineName(), pharmacistEmail)
                    .orElseThrow(() -> new RuntimeException("Medicine not found in inventory: " + item.getMedicineName()));

            if (medicine.getQuantity() == null || medicine.getQuantity() < 1) {
                throw new RuntimeException("Insufficient stock for medicine: " + item.getMedicineName());
            }

            // decrement by 1 unit per item (can be enhanced to quantity field later)
            medicine.setQuantity(medicine.getQuantity() - 1);
            medicineRepository.save(medicine);

            if (medicine.getPrice() != null) {
                totalAmount += medicine.getPrice();
            }
        }

        // Mark prescription and items as dispensed, associate with pharmacy
        prescription.setStatus(com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED);
        for (PrescriptionItem item : prescription.getItems()) {
            item.setStatus(com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED);
        }
        prescription.setPharmacy(pharmacist);
        prescriptionRepository.save(prescription);

        Insurance insurance = patient.getInsurance();
        double coverageRate = coverageRateFor(insurance);
        double insuranceCoveredAmount = totalAmount * coverageRate;
        double amountPaid = totalAmount - insuranceCoveredAmount;

        DispenseRecord record = new DispenseRecord();
        record.setPatientReferenceNumber(ref);
        record.setInsuranceUsed(insurance);
        record.setCoverageRate(coverageRate);
        record.setOrdinanceId(prescription.getId());
        record.setDispensedAt(LocalDateTime.now());
        record.setTotalAmount(totalAmount);
        record.setInsuranceCoveredAmount(insuranceCoveredAmount);
        record.setAmountPaid(amountPaid);
        record.setMedicineName(prescription.getItems() != null && !prescription.getItems().isEmpty() ? prescription.getItems().getFirst().getMedicineName() : null);
        record.setQuantity(prescription.getItems() != null ? prescription.getItems().size() : 0);
        record.setPharmacist(pharmacist);
        DispenseRecord saved = dispenseRecordRepository.save(record);

        return new DispenseRecordResponse(
                saved.getOrdinanceId(),
                saved.getPatientReferenceNumber(),
                saved.getInsuranceUsed(),
                saved.getCoverageRate(),
                saved.getDispensedAt(),
                saved.getTotalAmount(),
                saved.getInsuranceCoveredAmount(),
                saved.getAmountPaid(),
                saved.getMedicineName(),
                saved.getQuantity()
        );
    }

    /**
     * Business logic: dispense a single prescription item for a patient and update stock accordingly.
     * Marks the item as DISPENSED and, if all items are dispensed, marks the whole prescription as DISPENSED.
     */
    public DispenseRecordResponse dispensePrescriptionItem(String pharmacistEmail, Long prescriptionId, int itemIndex) {
        var pharmacist = pharmacyRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        if (prescription.getItems() == null || prescription.getItems().isEmpty()) {
            throw new RuntimeException("Prescription has no items to dispense");
        }

        if (itemIndex < 0 || itemIndex >= prescription.getItems().size()) {
            throw new RuntimeException("Invalid prescription item index");
        }

        PrescriptionItem item = prescription.getItems().get(itemIndex);
        if (item.getStatus() == com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED) {
            throw new RuntimeException("This medicine has already been dispensed");
        }

        String ref = prescription.getPatient() != null ? prescription.getPatient().getReferenceNumber() : null;
        if (ref == null || ref.isBlank()) {
            throw new RuntimeException("Patient reference number is required to dispense");
        }

        var patient = patientRepository.findByReferenceNumber(ref);
        if (patient == null) {
            throw new RuntimeException("Patient not found");
        }

        var medicine = medicineRepository.findByNameAndPharmacyEmail(item.getMedicineName(), pharmacistEmail)
                .orElseThrow(() -> new RuntimeException("Medicine not found in inventory: " + item.getMedicineName()));

        if (medicine.getQuantity() == null || medicine.getQuantity() < 1) {
            throw new RuntimeException("Insufficient stock for medicine: " + item.getMedicineName());
        }

        // Decrement stock for this specific medicine
        medicine.setQuantity(medicine.getQuantity() - 1);
        medicineRepository.save(medicine);

        double unitPrice = medicine.getPrice() != null ? medicine.getPrice() : 0.0;
        double totalAmount = unitPrice;

        // Mark this item as dispensed
        item.setStatus(com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED);

        // If all items are now dispensed, mark whole prescription as DISPPENSED
        boolean allDispensed = prescription.getItems().stream()
                .allMatch(i -> i.getStatus() == com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED);
        if (allDispensed) {
            prescription.setStatus(com.springboot.medsystem.Enums.PrescriptionStatus.DISPENSED);
        }
        prescription.setPharmacy(pharmacist);
        prescriptionRepository.save(prescription);

        Insurance insurance = patient.getInsurance();
        double coverageRate = coverageRateFor(insurance);
        double insuranceCoveredAmount = totalAmount * coverageRate;
        double amountPaid = totalAmount - insuranceCoveredAmount;

        DispenseRecord record = new DispenseRecord();
        record.setPatientReferenceNumber(ref);
        record.setInsuranceUsed(insurance);
        record.setCoverageRate(coverageRate);
        record.setOrdinanceId(prescription.getId());
        record.setDispensedAt(LocalDateTime.now());
        record.setTotalAmount(totalAmount);
        record.setInsuranceCoveredAmount(insuranceCoveredAmount);
        record.setAmountPaid(amountPaid);
        record.setMedicineName(item.getMedicineName());
        record.setQuantity(1);
        record.setPharmacist(pharmacist);
        DispenseRecord saved = dispenseRecordRepository.save(record);

        return new DispenseRecordResponse(
                saved.getOrdinanceId(),
                saved.getPatientReferenceNumber(),
                saved.getInsuranceUsed(),
                saved.getCoverageRate(),
                saved.getDispensedAt(),
                saved.getTotalAmount(),
                saved.getInsuranceCoveredAmount(),
                saved.getAmountPaid(),
                saved.getMedicineName(),
                saved.getQuantity()
        );
    }

    /** Business logic: retrieve dispensing history for a pharmacist, optionally filtered by patient reference. */
    public List<DispenseRecordResponse> getDispenseRecords(String pharmacistEmail, String referenceNumber) {
        var records = (referenceNumber != null && !referenceNumber.isBlank())
                ? dispenseRecordRepository.findByPharmacistEmailAndPatientReferenceNumber(pharmacistEmail, referenceNumber.trim())
                : dispenseRecordRepository.findByPharmacistEmail(pharmacistEmail);

        return records.stream()
                .map(r -> new DispenseRecordResponse(
                        r.getOrdinanceId(),
                        r.getPatientReferenceNumber(),
                        r.getInsuranceUsed(),
                        r.getCoverageRate(),
                        r.getDispensedAt(),
                        r.getTotalAmount(),
                        r.getInsuranceCoveredAmount(),
                        r.getAmountPaid(),
                        r.getMedicineName(),
                        r.getQuantity()
                ))
                .toList();
    }

    private void applyDto(Medicine medicine, MedecineDto dto) {
        medicine.setName(dto.getName());
        medicine.setBatch(dto.getBatch());
        medicine.setQuantity(dto.getQuantity());
        medicine.setPrice(dto.getPrice());
        medicine.setCategory(dto.getCategory());
        medicine.setExpiryDate(dto.getExpiryDate());
    }

    private MedicineResponse toResponse(Medicine m) {
        String msg = String.format("%s (%s) - qty: %d, price: %.2f, category: %s, expires: %s",
                m.getName(),
                m.getBatch(),
                m.getQuantity(),
                m.getPrice(),
                m.getCategory(),
                m.getExpiryDate());
        return new MedicineResponse(msg);
    }

    private PrescriptionResponse toPrescriptionResponse(Prescription p) {
        java.util.List<com.springboot.medsystem.DTO.PrescriptionItemDto> items = p.getItems() == null ? java.util.List.of() :
                p.getItems().stream()
                        .map(item -> {
                            com.springboot.medsystem.DTO.PrescriptionItemDto dto = new com.springboot.medsystem.DTO.PrescriptionItemDto();
                            dto.setMedicineName(item.getMedicineName());
                            dto.setDosage(item.getDosage());
                            dto.setFrequency(item.getFrequency());
                            dto.setDuration(item.getDuration());
                            dto.setNote(item.getNote());
                            dto.setStatus(item.getStatus());
                            return dto;
                        })
                        .toList();

        String ref = p.getPatient() != null ? p.getPatient().getReferenceNumber() : null;
        String name = p.getPatient() != null ? p.getPatient().getFullName() : null;

        return com.springboot.medsystem.DTO.PrescriptionResponse.builder()
                .id(p.getId())
                .patientReferenceNumber(ref)
                .prescribedAt(p.getPrescribedAt())
                .status(p.getStatus())
                .items(items)
                .build();
    }

    private double coverageRateFor(Insurance insurance) {
        if (insurance == null) return 0.0;
        return switch (insurance) {
            case RAMA -> 0.85;
            case MMI -> 0.65;
            case UAP -> 0.70;
            case MITUEL -> 0.60;
            case RADIANT -> 0.75;
        };
    }
}

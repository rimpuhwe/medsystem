package com.springboot.medsystem.Doctor;


import com.springboot.medsystem.Aunthentication.OtpVerification;
import com.springboot.medsystem.Configuration.EmailService;
import com.springboot.medsystem.Consultation.Consultation;
import com.springboot.medsystem.Consultation.ConsultationRepository;
import com.springboot.medsystem.DTO.DoctorDto;
import com.springboot.medsystem.DTO.DoctorResponse;
import com.springboot.medsystem.Enums.QueueStatus;
import com.springboot.medsystem.Enums.Role;
import com.springboot.medsystem.Queue.QueueManagement;
import com.springboot.medsystem.Queue.QueueManagementRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.springboot.medsystem.Aunthentication.OtpVerificationRepository;
import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.springboot.medsystem.DTO.ConsultationRequest;
import com.springboot.medsystem.DTO.PrescriptionItemDto;
import com.springboot.medsystem.Patient.PatientRepository;
import com.springboot.medsystem.prescription.Prescription;
import com.springboot.medsystem.prescription.PrescriptionItem;
import com.springboot.medsystem.prescription.PrescriptionRepository;
import com.springboot.medsystem.Enums.PrescriptionStatus;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicRepository clinicRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final QueueManagementRepository queueManagementRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final DoctorPatientRecordRepository doctorPatientRecordRepository;
    private final PrescriptionRepository prescriptionRepository;

    public DoctorService(DoctorRepository doctorRepository, PasswordEncoder passwordEncoder,
                         ClinicRepository clinicRepository, OtpVerificationRepository otpVerificationRepository,
                         EmailService emailService , QueueManagementRepository queueManagementRepository,
                         ConsultationRepository consultationRepository, PatientRepository patientRepository,
                         DoctorPatientRecordRepository doctorPatientRecordRepository,
                         PrescriptionRepository prescriptionRepository) {
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.clinicRepository = clinicRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.emailService = emailService;
        this.queueManagementRepository = queueManagementRepository;
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.doctorPatientRecordRepository = doctorPatientRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
    }
    /**
     * Add a consultation (diagnosis and medicines) for a patient by reference number.
     * Also creates a structured prescription, updates queue status to SERVED, and maintains per-doctor patient record.
     */
    public void addConsultation(String patientReferenceNumber, ConsultationRequest request, UserDetails userDetails) {
        if (userDetails == null) throw new RuntimeException("Doctor not authenticated");
        String email = userDetails.getUsername();
        DoctorProfile doctor = doctorRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
        var patientOpt = patientRepository.findAll().stream().filter(p -> patientReferenceNumber.equals(p.getReferenceNumber())).findFirst();
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient reference number does not exist");
        }
        var patient = patientOpt.get();
        // Update chronic diseases and allergies on patient profile if provided
        if (request.getChronicDiseases() != null && !request.getChronicDiseases().isEmpty()) {
            Set<String> mergedChronics = new HashSet<>();
            if (patient.getChronicDiseases() != null) mergedChronics.addAll(patient.getChronicDiseases());
            mergedChronics.addAll(request.getChronicDiseases());
            patient.setChronicDiseases(new ArrayList<>(mergedChronics));
        }
        if (request.getAllergies() != null && !request.getAllergies().isEmpty()) {
            Set<String> mergedAllergies = new HashSet<>();
            if (patient.getAllergies() != null) mergedAllergies.addAll(patient.getAllergies());
            mergedAllergies.addAll(request.getAllergies());
            patient.setAllergies(new java.util.ArrayList<>(mergedAllergies));
        }
        patientRepository.save(patient);
        Consultation consultation = new Consultation();
        consultation.setPatientReferenceNumber(patientReferenceNumber);
        consultation.setDoctorName(doctor.getFullName());
        consultation.setDiagnosis(request.getDiagnosis());
        consultation.setChronicDiseases(request.getChronicDiseases() != null ? request.getChronicDiseases() : java.util.Collections.emptyList());
        consultation.setAllergies(request.getAllergies() != null ? request.getAllergies() : java.util.Collections.emptyList());
        consultation.setConsultationDate(java.time.LocalDateTime.now());
        // store a simple text summary of prescribed medicines
        java.util.List<String> summary = new java.util.ArrayList<>();
        if (request.getPrescriptionItems() != null) {
            for (PrescriptionItemDto dto : request.getPrescriptionItems()) {
                summary.add(dto.getMedicineName());
            }
        } else if (request.getMedicines() != null) {
            summary.addAll(request.getMedicines());
        }
        consultation.setPrescriptionItem(summary);
        consultation.setMedicines(summary);
        consultationRepository.save(consultation);

        // Build prescription items (prefer structured items, fallback to simple medicine names)
        java.util.List<PrescriptionItem> items = new java.util.ArrayList<>();
        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            for (PrescriptionItemDto dto : request.getPrescriptionItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(dto.getMedicineName());
                item.setDosage(dto.getDosage());
                item.setFrequency(dto.getFrequency());
                item.setDuration(dto.getDuration());
                item.setNote(dto.getNote());
                item.setStatus(PrescriptionStatus.ACTIVE);
                items.add(item);
            }
        } else if (request.getMedicines() != null) {
            for (String name : request.getMedicines()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(name);
                item.setStatus(PrescriptionStatus.ACTIVE);
                items.add(item);
            }
        }

        // Persist prescription linked to patient and doctor
        Prescription prescription = new Prescription();
        prescription.setPrescribedAt(java.time.LocalDateTime.now());
        prescription.setStatus(PrescriptionStatus.ACTIVE);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setItems(items);
        prescriptionRepository.save(prescription);

        // Mark queue entry as SERVED for this doctor/patient today
        String clinicName = doctor.getClinic().getClinicName();
        String service = doctor.getService().toString();
        LocalDate today = LocalDate.now();
        List<QueueManagement> queue = queueManagementRepository.findByClinic_ClinicNameAndServiceAndQueueDate(clinicName, service, today);
        queue.stream()
                .filter(q -> patientReferenceNumber.equals(q.getPatientReferenceNumber()))
                .findFirst()
                .ifPresent(q -> {
                    q.setStatus(QueueStatus.SERVED);
                    queueManagementRepository.save(q);
                });

        // Update doctor-patient record (number of prescriptions + last visit + basic demographics)
        DoctorPatientRecord record = doctorPatientRecordRepository
                .findByDoctorEmailAndPatientReferenceNumber(doctor.getEmail(), patientReferenceNumber)
                .orElseGet(() -> {
                    DoctorPatientRecord r = new DoctorPatientRecord();
                    r.setDoctorEmail(doctor.getEmail());
                    r.setPatientReferenceNumber(patientReferenceNumber);
                    r.setPatientName(patient.getFullName());
                    r.setGender(patient.getGender());
                    r.setPatientEmail(patient.getEmail());
                    r.setPatientPhone(patient.getPhone());
                    r.setNumberOfPrescriptions(0);
                    return r;
                });
        int currentCount = record.getNumberOfPrescriptions() != null ? record.getNumberOfPrescriptions() : 0;
        record.setNumberOfPrescriptions(currentCount + 1);
        record.setLastVisit(LocalDateTime.now());
        doctorPatientRecordRepository.save(record);
    }

    public List<DoctorProfile> getAllDoctors() {

        return doctorRepository.findAll();
    }

    public DoctorProfile getDoctorByName(String name) {

        return doctorRepository.findByFullName(name).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }




    public DoctorResponse addDoctor(DoctorDto doctor) {
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setFullName(doctor.getFullName());
        doctorProfile.setEmail(doctor.getEmail());
        doctorProfile.setService(doctor.getService());
        doctorProfile.setPhone(doctor.getPhone());
        doctorProfile.setRole(Role.DOCTOR);

        Clinic clinic = clinicRepository.findByClinicName(String.valueOf(doctor.getClinicName()))
            .orElseThrow(() -> new RuntimeException("Clinic not found: " + doctor.getClinicName()));
        doctorProfile.setClinic(clinic);
        doctorProfile.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctorRepository.save(doctorProfile);

        String otp = generateOtp();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(doctor.getEmail());
        otpVerification.setOtp(otp);
        otpVerification.setVerified(false);
        otpVerification.setExpiry(LocalDateTime.now().plusMinutes(10));
        otpVerificationRepository.save(otpVerification);

        // Send OTP email
        emailService.sendVerificationEmail(doctorProfile, otp);

        return com.springboot.medsystem.DTO.DoctorResponse.builder()
                .Message("Doctor added successfully. The doctor must  use the OTP sent in their email to verify  their account.")
                .email(doctor.getEmail())
                .otp(otp)
                .role(Role.DOCTOR)
                .Timestamp(LocalDate.now())
                .build();
    }

    private String generateOtp() {
        java.util.Random random = new java.util.Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public DoctorProfile getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public void callNextPatient(UserDetails userDetails) {
        if (userDetails == null) throw new RuntimeException("Doctor not authenticated");
        String email = userDetails.getUsername();
        DoctorProfile doctor = doctorRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
        String clinicName = doctor.getClinic().getClinicName();
        String service = doctor.getService().toString();
        LocalDate today = LocalDate.now();
        List<QueueManagement> queue = queueManagementRepository.findByClinic_ClinicNameAndServiceAndQueueDate(clinicName, service, today);
        QueueManagement nextPatient = queue.stream()
                .filter(q -> q.getStatus() == QueueStatus.WAITING)
                .min(java.util.Comparator.comparingInt(QueueManagement::getPosition))
                .orElse(null);
        if (nextPatient == null) {
            throw new RuntimeException("No patient waiting in queue for your service");
        }
        nextPatient.setStatus(QueueStatus.IN_PROGRESS);
        queueManagementRepository.save(nextPatient);
        for (QueueManagement q : queue) {
            if (q.getPosition() > nextPatient.getPosition()) {
                q.setPosition(q.getPosition() - 1);
                queueManagementRepository.save(q);
            }
        }

    }

    /**
     * Get consultation history for this doctor with optional patient and period filters.
     */
    public java.util.List<com.springboot.medsystem.DTO.DoctorConsultationHistoryResponse> getConsultationHistory(
            UserDetails userDetails,
            String referenceNumber,
            String patientName,
            String period) {
        if (userDetails == null) throw new RuntimeException("Doctor not authenticated");
        String email = userDetails.getUsername();
        DoctorProfile doctor = doctorRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = null;
        if (period != null) {
            String p = period.toLowerCase();
            if (p.equals("today")) {
                start = now.toLocalDate().atStartOfDay();
            } else if (p.equals("this_week")) {
                start = now.minusDays(7);
            } else if (p.equals("last_month")) {
                start = now.minusDays(30);
            }
        }

        java.util.List<Consultation> consultations;
        if (start != null) {
            consultations = consultationRepository.findByDoctorNameAndConsultationDateBetween(doctor.getFullName(), start, now);
        } else {
            consultations = consultationRepository.findByDoctorName(doctor.getFullName());
        }

        if (referenceNumber != null && !referenceNumber.isBlank()) {
            consultations = consultations.stream()
                    .filter(c -> referenceNumber.equals(c.getPatientReferenceNumber()))
                    .toList();
        }

        if (patientName != null && !patientName.isBlank()) {
            String search = patientName.toLowerCase();
            var allPatients = patientRepository.findAll();
            var refByName = allPatients.stream()
                    .filter(p -> p.getFullName() != null && p.getFullName().toLowerCase().contains(search))
                    .map(com.springboot.medsystem.Patient.PatientProfile::getReferenceNumber)
                    .collect(java.util.stream.Collectors.toSet());
            consultations = consultations.stream()
                    .filter(c -> refByName.contains(c.getPatientReferenceNumber()))
                    .toList();
        }

        var allPatients = patientRepository.findAll();
        return consultations.stream()
                .map(c -> {
                    String ref = c.getPatientReferenceNumber();
                    String name = allPatients.stream()
                            .filter(p -> ref != null && ref.equals(p.getReferenceNumber()))
                            .map(com.springboot.medsystem.Patient.PatientProfile::getFullName)
                            .findFirst()
                            .orElse(null);
                    int medsCount = c.getMedicines() != null ? c.getMedicines().size() : 0;
                    return new com.springboot.medsystem.DTO.DoctorConsultationHistoryResponse(
                            c.getId(),
                            ref,
                            name,
                            c.getConsultationDate(),
                            c.getDiagnosis(),
                            medsCount
                    );
                })
                .toList();
    }

    /**
     * Get per-doctor served patient records (name, contacts, number of prescriptions, last visit).
     */
    public java.util.List<DoctorPatientRecord> getServedPatients(UserDetails userDetails) {
        if (userDetails == null) throw new RuntimeException("Doctor not authenticated");
        String email = userDetails.getUsername();
        return doctorPatientRecordRepository.findByDoctorEmail(email);
    }

}

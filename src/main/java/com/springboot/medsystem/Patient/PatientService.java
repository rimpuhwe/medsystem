package com.springboot.medsystem.Patient;

import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicService;
import com.springboot.medsystem.Consultation.Consultation;
import com.springboot.medsystem.Consultation.ConsultationRepository;
import com.springboot.medsystem.DTO.PatientProfileUpdateRequest;
import com.springboot.medsystem.DTO.PatientQueueJoinRequest;
import com.springboot.medsystem.DTO.QueuePosition;
import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Doctor.DoctorRepository;
import com.springboot.medsystem.Enums.QueueStatus;
import com.springboot.medsystem.Queue.QueueManagement;
import com.springboot.medsystem.Queue.QueueManagementRepository;
import com.springboot.medsystem.Queue.QueueManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final QueueManagementService queueManagementService;
    private final QueueManagementRepository queueManagementRepository;
    private final ClinicService clinicService;
    private final DoctorRepository doctorRepository;
    private final ConsultationRepository consultationRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository, ClinicService clinicService, QueueManagementService queueManagementService, QueueManagementRepository queueManagementRepository, com.springboot.medsystem.Doctor.DoctorRepository doctorRepository, ConsultationRepository consultationRepository) {
        this.patientRepository = patientRepository;
        this.queueManagementService = queueManagementService;
        this.queueManagementRepository = queueManagementRepository;
        this.clinicService = clinicService;
        this.doctorRepository = doctorRepository;
        this.consultationRepository = consultationRepository;
    }
    public Map<String, Object> getMedicalHistoryAndConditions(String email, LocalDate startDate, LocalDate endDate) {
        Optional<PatientProfile> patientOpt = getProfileByEmail(email);
        if (patientOpt.isEmpty()) {
            return null;
        }
        PatientProfile patient = patientOpt.get();
        String refNumber = patient.getReferenceNumber();
        List<Consultation> consultations = consultationRepository.findByPatientReferenceNumber(refNumber);
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            consultations = consultations.stream()
                .filter(c -> c.getConsultationDate() != null &&
                    !c.getConsultationDate().toLocalDate().isBefore(startDate) &&
                    !c.getConsultationDate().toLocalDate().isAfter(endDate))
                .toList();
        }
        // Collect all unique chronic diseases and allergies from consultations
        List<String> chronicDiseases = consultations.stream()
                .flatMap(c -> c.getChronicDiseases() != null ? c.getChronicDiseases().stream() : java.util.stream.Stream.empty())
                .distinct().collect(Collectors.toList());
        List<String> allergies = consultations.stream()
                .flatMap(c -> c.getAllergies() != null ? c.getAllergies().stream() : java.util.stream.Stream.empty())
                .distinct().collect(Collectors.toList());
        // Format medical history
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Object> medicalHistory = consultations.stream().map(c -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", c.getConsultationDate() != null ? c.getConsultationDate().format(formatter) : null);
            entry.put("Doctor", c.getDoctorName());
            entry.put("diagnosed", c.getDiagnosis());
            entry.put("prescribed", c.getMedicines());
            // Clinic name is not stored in Consultation, so left blank or can be enhanced if needed
            entry.put("clinic", "");
            return entry;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("chronicDiseases", chronicDiseases);
        result.put("allergies", allergies);
        result.put("medical-history", medicalHistory);
        return result;
    }

    public Optional<PatientProfile> getProfileByEmail(String email) {
        return patientRepository.findByEmail(email);
    }


    /**
     * Join a patient to a queue for a specific clinic and service. Adds to QueueManagement and returns their position.
     * Checks for existing entry for today, resets queue at end of day.
     */
    public QueuePosition joinQueue(UserDetails userDetails, PatientQueueJoinRequest joinRequest) {
        String email = userDetails.getUsername();
        Optional<PatientProfile> patientOpt = getProfileByEmail(email);
        if (patientOpt.isEmpty()) throw new RuntimeException("Patient not found");
        PatientProfile patient = patientOpt.get();
        String refNumber = patient.getReferenceNumber();
        String clinicName = joinRequest.getClinicName();
        String service = joinRequest.getService().toUpperCase();
        String doctorName = joinRequest.getDoctorName();
        LocalDate today = LocalDate.now();

        Clinic clinic = clinicService.getClinicByName(clinicName);

        List<QueueManagement> allForService = queueManagementRepository.findByClinic_ClinicNameAndService(clinicName, service);
        boolean needsReset = allForService.stream().anyMatch(q -> q.getQueueDate() != null && !q.getQueueDate().isEqual(today));
        if (needsReset) {

            allForService.stream().filter(q -> q.getQueueDate() != null && !q.getQueueDate().isEqual(today)).forEach(queueManagementRepository::delete);
        }

        boolean exists = allForService.stream().anyMatch(q -> q.getPatientReferenceNumber().equals(refNumber) && today.equals(q.getQueueDate()));
        if (exists) throw new IllegalStateException("Patient already in queue for this service today");


        String assignedDoctorName = doctorName;
        if (assignedDoctorName == null || assignedDoctorName.isEmpty()) {

            List<com.springboot.medsystem.Doctor.DoctorProfile> doctors = doctorRepository.findAll();
            List<com.springboot.medsystem.Doctor.DoctorProfile> filteredDoctors = new ArrayList<>();
            for (com.springboot.medsystem.Doctor.DoctorProfile doc : doctors) {
                if (doc.getClinic().getClinicName().equals(clinicName) && doc.getService().toString().equalsIgnoreCase(service)) {
                    filteredDoctors.add(doc);
                }
            }
            if (filteredDoctors.isEmpty()) {
                throw new RuntimeException("No doctor available for this clinic and service");
            } else if (filteredDoctors.size() == 1) {
                assignedDoctorName = filteredDoctors.getFirst().getFullName();
            } else {
                // Assign to doctor with the least patients in queue for today
                int minQueue = Integer.MAX_VALUE;
                com.springboot.medsystem.Doctor.DoctorProfile selectedDoctor = null;
                for (com.springboot.medsystem.Doctor.DoctorProfile doc : filteredDoctors) {
                    long count = allForService.stream().filter(q -> today.equals(q.getQueueDate()) && doc.getFullName().equals(q.getDoctorName())).count();
                    if (count < minQueue) {
                        minQueue = (int) count;
                        selectedDoctor = doc;
                    }
                }
                assert selectedDoctor != null;
                assignedDoctorName = selectedDoctor.getFullName();
            }
        }

        // Find current max position for this clinic/service for today
        int maxPosition = allForService.stream().filter(q -> today.equals(q.getQueueDate())).mapToInt(QueueManagement::getPosition).max().orElse(0);
        int newPosition = maxPosition + 1;

        QueueStatus status = QueueStatus.WAITING;


        QueueManagement qm = new QueueManagement();
        qm.setPatientReferenceNumber(refNumber);
        qm.setPosition(newPosition);
        qm.setClinic(clinic);
        qm.setService(service);
        qm.setDoctorName(assignedDoctorName);
        qm.setQueueDate(today);
        qm.setStatus(status);
        queueManagementRepository.save(qm);

        return new QueuePosition(refNumber, newPosition, clinic, service, assignedDoctorName , status);
    }


    public List<QueuePosition> getServiceQueue(String clinicName, String service) {
        return queueManagementService.getServiceQueue(clinicName, service);
    }




    public List<QueuePosition> getAllPatientsInQueueForDoctor(String clinicName, UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        String email = userDetails.getUsername();
        Optional<DoctorProfile> doctorOpt = doctorRepository.findByEmail(email);
        if (doctorOpt.isEmpty()) {
            throw new RuntimeException("Doctor not found");
        }
        DoctorProfile doctor = doctorOpt.get();
        LocalDate today = LocalDate.now();
        // Only show patients in the same clinic and service as the doctor
        List<QueueManagement> allForService = queueManagementRepository.findByClinic_ClinicNameAndService(clinicName, doctor.getService().toString());
        List<QueuePosition> result = new ArrayList<>();
        for (QueueManagement qm : allForService) {
            if (today.equals(qm.getQueueDate())) {
                result.add(new QueuePosition(qm.getPatientReferenceNumber(), qm.getPosition(), qm.getClinic(), qm.getService(), qm.getDoctorName() , qm.getStatus()));
            }
        }
        return result;
    }


    public PatientProfile updateProfile(String email, PatientProfileUpdateRequest updateRequest) {
        Optional<PatientProfile> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isEmpty()) {
            return null;
        }
        PatientProfile patient = patientOpt.get();
        patient.setEmail(updateRequest.getEmail());
        patient.setPhone(updateRequest.getPhone());
        patient.setGender(updateRequest.getGender());
        patient.setInsurance(updateRequest.getInsurance());
        patient.setInsuranceNumber(updateRequest.getInsuranceNumber());
        patient.setDateOfBirth(updateRequest.getDateOfBirth());
        return patientRepository.save(patient);
    }

    public Map<String, Object> getQueueSummary(String clinicName, String service, UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        if (email == null || patientRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("Patient not authenticated");
        }

        LocalDate today = LocalDate.now();

        List<QueueManagement> queue = queueManagementRepository.findByClinic_ClinicNameAndServiceAndQueueDate(clinicName, service, today);
        int totalCount = queue.size();

        List<DoctorProfile> allDoctors = doctorRepository.findAll();
        List<String> doctorNames = new ArrayList<>();
        for (DoctorProfile doc : allDoctors) {
            if (doc.getClinic().getClinicName().equals(clinicName) && doc.getService().toString().equalsIgnoreCase(service)) {
                doctorNames.add(doc.getFullName());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("Message", totalCount + " have joined this queue");
        result.put("service", service);
        result.put("clinic", clinicName);
        result.put("doctor", doctorNames);
        return result;
    }
}

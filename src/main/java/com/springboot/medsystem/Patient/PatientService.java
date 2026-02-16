package com.springboot.medsystem.Patient;

import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicService;
import com.springboot.medsystem.DTO.PatientProfileUpdateRequest;
import com.springboot.medsystem.DTO.PatientQueueJoinRequest;
import com.springboot.medsystem.DTO.QueuePosition;
import com.springboot.medsystem.Queue.QueueManagement;
import com.springboot.medsystem.Queue.QueueManagementRepository;
import com.springboot.medsystem.Queue.QueueManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final QueueManagementService queueManagementService;
    private final QueueManagementRepository queueManagementRepository;
    private final ClinicService clinicService;
    private final com.springboot.medsystem.Doctor.DoctorRepository doctorRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository, ClinicService clinicService, QueueManagementService queueManagementService, QueueManagementRepository queueManagementRepository, com.springboot.medsystem.Doctor.DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.queueManagementService = queueManagementService;
        this.queueManagementRepository = queueManagementRepository;
        this.clinicService = clinicService;
        this.doctorRepository = doctorRepository;
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
        String service = joinRequest.getService();
        String doctorName = joinRequest.getDoctorName();
        LocalDate today = LocalDate.now();

        Clinic clinic = clinicService.getClinicByName(clinicName);
        // Reset queue for this clinic/service if not today
        List<QueueManagement> allForService = queueManagementRepository.findByClinic_ClinicNameAndService(clinicName, service);
        boolean needsReset = allForService.stream().anyMatch(q -> q.getQueueDate() != null && !q.getQueueDate().isEqual(today));
        if (needsReset) {
            // Remove all old queue entries for this clinic/service
            allForService.stream().filter(q -> q.getQueueDate() != null && !q.getQueueDate().isEqual(today)).forEach(queueManagementRepository::delete);
        }
        // Check if patient already in queue for today
        boolean exists = allForService.stream().anyMatch(q -> q.getPatientReferenceNumber().equals(refNumber) && today.equals(q.getQueueDate()));
        if (exists) throw new IllegalStateException("Patient already in queue for this service today");

        // Doctor assignment logic
        String assignedDoctorName = doctorName;
        if (assignedDoctorName == null || assignedDoctorName.isEmpty()) {
            // Find all doctors for this clinic and service
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
                assignedDoctorName = filteredDoctors.get(0).getFullName();
            } else {
                // Assign to doctor with least patients in queue for today
                int minQueue = Integer.MAX_VALUE;
                com.springboot.medsystem.Doctor.DoctorProfile selectedDoctor = null;
                for (com.springboot.medsystem.Doctor.DoctorProfile doc : filteredDoctors) {
                    long count = allForService.stream().filter(q -> today.equals(q.getQueueDate()) && doc.getFullName().equals(q.getDoctorName())).count();
                    if (count < minQueue) {
                        minQueue = (int) count;
                        selectedDoctor = doc;
                    }
                }
                assignedDoctorName = selectedDoctor.getFullName();
            }
        }

        // Find current max position for this clinic/service for today
        int maxPosition = allForService.stream().filter(q -> today.equals(q.getQueueDate())).mapToInt(QueueManagement::getPosition).max().orElse(0);
        int newPosition = maxPosition + 1;

        // Save new queue entry
        QueueManagement qm = new QueueManagement();
        qm.setPatientReferenceNumber(refNumber);
        qm.setPosition(newPosition);
        qm.setClinic(clinic);
        qm.setService(service);
        qm.setDoctorName(assignedDoctorName);
        qm.setQueueDate(today);
        queueManagementRepository.save(qm);

        return new QueuePosition(refNumber, newPosition, clinic, service, assignedDoctorName);
    }

    /**
     * Get all queue positions for a clinic and service.
     */

    public Map<String, List<QueuePosition>> getClinicQueueStructure(String clinicName) {
        return queueManagementService.getClinicQueueStructure(clinicName);
    }

    public List<QueuePosition> getServiceQueue(String clinicName, String service) {
        return queueManagementService.getServiceQueue(clinicName, service);
    }


    // WebSocket updates are handled elsewhere; no in-memory queue logic needed.


    /**
     * For doctor: get all patients in queue for a clinic, service, and doctor.
     */
    public List<QueuePosition> getAllPatientsInQueueForDoctor(String clinicName, String service, String doctorName) {
        LocalDate today = LocalDate.now();
        List<QueueManagement> allForService = queueManagementRepository.findByClinic_ClinicNameAndService(clinicName, service);
        List<QueuePosition> result = new ArrayList<>();
        for (QueueManagement qm : allForService) {
            if (today.equals(qm.getQueueDate()) && doctorName.equals(qm.getDoctorName())) {
                result.add(new QueuePosition(qm.getPatientReferenceNumber(), qm.getPosition(), qm.getClinic(), qm.getService(), qm.getDoctorName()));
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
}

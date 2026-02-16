package com.springboot.medsystem.Patient;

import com.springboot.medsystem.Clinics.Clinic;
import com.springboot.medsystem.Clinics.ClinicService;
import com.springboot.medsystem.DTO.PatientProfileUpdateRequest;
import com.springboot.medsystem.DTO.PatientQueueJoinRequest;
import com.springboot.medsystem.DTO.QueuePosition;
import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Doctor.DoctorRepository;
import com.springboot.medsystem.Queue.QueueManagement;
import com.springboot.medsystem.Queue.QueueManagementRepository;
import com.springboot.medsystem.Queue.QueueManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final QueueManagementService queueManagementService;
    private final QueueManagementRepository queueManagementRepository;
    private final ClinicService clinicService;
    private final DoctorRepository doctorRepository;

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


    public List<QueuePosition> getServiceQueue(String clinicName, String service) {
        return queueManagementService.getServiceQueue(clinicName, service);
    }




    public List<QueuePosition> getAllPatientsInQueueForDoctor(String clinicName, UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<DoctorProfile> DoctorOpt = doctorRepository.findByEmail(email);
        LocalDate today = LocalDate.now();
        List<QueueManagement> allForService = queueManagementRepository.findByClinic_ClinicName(clinicName);
        List<QueuePosition> result = new ArrayList<>();
        for (QueueManagement qm : allForService) {
            if (today.equals(qm.getQueueDate()) && DoctorOpt.get().getFullName().equals(qm.getDoctorName()) && qm.getService().equals(DoctorOpt.get().getService())) {
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

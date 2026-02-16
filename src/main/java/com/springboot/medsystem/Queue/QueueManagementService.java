package com.springboot.medsystem.Queue;

import com.springboot.medsystem.DTO.QueuePosition;
import com.springboot.medsystem.Clinics.Clinic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class QueueManagementService {
    private final QueueManagementRepository queueManagementRepository;

    @Autowired
    public QueueManagementService(QueueManagementRepository queueManagementRepository) {
        this.queueManagementRepository = queueManagementRepository;
    }

    public Map<String, List<QueuePosition>> getClinicQueueStructure(String clinicName) {
        LocalDate today = LocalDate.now();
        List<QueueManagement> allQueues = queueManagementRepository.findByClinic_ClinicName(clinicName);
        Map<String, List<QueuePosition>> serviceMap = new HashMap<>();
        for (QueueManagement qm : allQueues) {
            if (today.equals(qm.getQueueDate())) {
                serviceMap.computeIfAbsent(qm.getService(), k -> new ArrayList<>())
                    .add(new QueuePosition(qm.getPatientReferenceNumber(), qm.getPosition(), qm.getClinic(), qm.getService(), qm.getDoctorName()));
            }
        }
        // Sort each service queue by position
        for (List<QueuePosition> queue : serviceMap.values()) {
            queue.sort(Comparator.comparingInt(QueuePosition::getPosition));
        }
        return serviceMap;
    }

    public List<QueuePosition> getServiceQueue(String clinicName, String service) {
        LocalDate today = LocalDate.now();
        List<QueueManagement> queue = queueManagementRepository.findByClinic_ClinicNameAndServiceAndQueueDate(clinicName, service, today);
        return queue.stream()
            .map(qm -> new QueuePosition(qm.getPatientReferenceNumber(), qm.getPosition(), qm.getClinic(), qm.getService(), qm.getDoctorName()))
            .collect(Collectors.toList());
    }
}

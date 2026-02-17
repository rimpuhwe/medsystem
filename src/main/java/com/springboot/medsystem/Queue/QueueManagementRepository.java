package com.springboot.medsystem.Queue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface QueueManagementRepository extends JpaRepository<QueueManagement, Long> {
    List<QueueManagement> findByClinic_ClinicName(String clinicName);
    List<QueueManagement> findByClinic_ClinicNameAndService(String clinicName, String service);
    List<QueueManagement> findByClinic_ClinicNameAndServiceAndQueueDate(String clinicName, String service, LocalDate queueDate);
    List<QueueManagement> findByClinic_ClinicNameAndServiceOrderByPosition(String clinicName, String service);
}

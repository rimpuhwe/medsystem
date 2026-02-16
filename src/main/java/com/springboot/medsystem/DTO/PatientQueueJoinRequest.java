package com.springboot.medsystem.DTO;

import lombok.Data;

@Data
public class PatientQueueJoinRequest {
    private String clinicName;
    private String service;
    private String doctorName;
}

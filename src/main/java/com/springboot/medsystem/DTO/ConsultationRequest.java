package com.springboot.medsystem.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ConsultationRequest {
    private String diagnosis;
    private List<String> medicines;
    private List<String> chronicDiseases;
    private List<String> allergies;
}

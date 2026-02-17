package com.springboot.medsystem.DTO;

import com.springboot.medsystem.Enums.Service;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClinicDto {

    private String clinicName;

    @ElementCollection(targetClass = Service.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "clinic_services", joinColumns = @JoinColumn(name = "clinic_id"))
    @Column(name = "service")
    private Set<Service> services;

}

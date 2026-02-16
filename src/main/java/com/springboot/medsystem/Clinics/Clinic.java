package com.springboot.medsystem.Clinics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springboot.medsystem.Doctor.DoctorProfile;
import com.springboot.medsystem.Enums.Service;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import com.springboot.medsystem.Queue.QueueManagement;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String clinicName;

    @ElementCollection(targetClass = Service.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "clinic_services", joinColumns = @JoinColumn(name = "clinic_id"))
    @Column(name = "service")
    private Set<Service> services;

    @OneToMany(mappedBy = "clinic")
    @JsonManagedReference
    private List<DoctorProfile> doctors;

    @OneToMany(mappedBy = "clinic")
    @JsonIgnore
    private List<QueueManagement> queueManagement;

    @com.fasterxml.jackson.annotation.JsonProperty("queueManagement")
    public List<QueueManagement> getTodayQueueManagement() {
        LocalDate today = LocalDate.now();
        if (queueManagement == null) return null;
        return queueManagement.stream()
            .filter(q -> q.getQueueDate() != null && q.getQueueDate().isEqual(today))
            .collect(Collectors.toList());
    }
}

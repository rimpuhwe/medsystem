package com.springboot.medsystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class InventoryAlertsResponse {
    private List<MedicineResponse> lowStock;
    private List<MedicineResponse> expiringSoon;
}


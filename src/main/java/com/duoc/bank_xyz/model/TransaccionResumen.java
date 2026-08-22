package com.duoc.bank_xyz.model;

import lombok.Data;

@Data
public class TransaccionResumen {

    private String fechaReporte;
    private int totalProcesadas;
    private double montoTotal;
    private int totalAnomalias;
    
}

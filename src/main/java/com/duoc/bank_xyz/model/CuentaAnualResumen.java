package com.duoc.bank_xyz.model;

import lombok.Data;

@Data
public class CuentaAnualResumen {

    private int cuentaId;
    private int totalMovimientos;
    private double montoTotal;
    private String fechaReporte;
    
}
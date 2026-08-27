package com.duoc.bank_xyz.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interes {

    private int cuentaId;
    private String nombre;
    private Double saldo;
    private Integer edad;
    private String tipo;
    private Double saldoFinal;
    private Double interes;
    
}
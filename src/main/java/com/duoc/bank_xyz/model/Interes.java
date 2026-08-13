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
    private double saldo;
    private int edad;
    private String tipo;
    private double saldoFinal;
    private double interes;
    
}
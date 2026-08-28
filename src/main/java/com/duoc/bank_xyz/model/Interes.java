package com.duoc.bank_xyz.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(of = {"cuentaId", "nombre", "saldo", "tipo"})
public class Interes {

    private int cuentaId;
    private String nombre;
    private Double saldo;
    private Integer edad;
    private String tipo;
    private Double saldoFinal;
    private Double interes;
    
}
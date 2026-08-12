package com.duoc.bank_xyz.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    private int id;
    private String fecha;
    private double monto;
    private String tipo;
    
}
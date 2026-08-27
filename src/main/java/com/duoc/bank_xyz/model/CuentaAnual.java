package com.duoc.bank_xyz.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaAnual {

    private int cuentaId;
    private String fecha;
    private String transaccion;
    private Double monto;
    private String descripcion;
    
}
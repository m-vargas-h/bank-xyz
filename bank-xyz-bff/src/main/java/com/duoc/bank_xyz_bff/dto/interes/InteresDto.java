package com.duoc.bank_xyz_bff.dto.interes;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InteresDto {
    private Long id;
    private Long cuentaId;
    private String nombre;
    private BigDecimal saldo;
    private String tipo;
    private BigDecimal saldoFinal;
    private BigDecimal interes;
}
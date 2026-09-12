package com.duoc.bank_xyz_bff.dto.interes;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InteresDto {
    private Long cuentaId;
    private BigDecimal saldo;
    private String tipo;
    private BigDecimal tasaInteres;
    private BigDecimal interesCalculado;
}
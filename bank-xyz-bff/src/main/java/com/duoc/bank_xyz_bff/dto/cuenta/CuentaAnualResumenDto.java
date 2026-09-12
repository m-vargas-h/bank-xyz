package com.duoc.bank_xyz_bff.dto.cuenta;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaAnualResumenDto {
    private Long cuentaId;
    private BigDecimal totalMonto;
    private Long totalTransacciones;
}
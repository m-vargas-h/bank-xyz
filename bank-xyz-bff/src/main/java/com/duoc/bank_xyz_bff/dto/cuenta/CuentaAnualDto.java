package com.duoc.bank_xyz_bff.dto.cuenta;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaAnualDto {
    private Long cuentaId;
    private BigDecimal monto;
    private String transaccion;
    private String fecha;
    private String tipo;
}
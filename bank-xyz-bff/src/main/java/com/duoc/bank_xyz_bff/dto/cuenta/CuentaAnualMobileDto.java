package com.duoc.bank_xyz_bff.dto.cuenta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CuentaAnualMobileDto {
    private Long cuentaId;
    private BigDecimal monto;
    private String transaccion;
}
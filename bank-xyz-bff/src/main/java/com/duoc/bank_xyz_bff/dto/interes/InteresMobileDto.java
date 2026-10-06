package com.duoc.bank_xyz_bff.dto.interes;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class InteresMobileDto {
    private Long cuentaId;
    private BigDecimal saldo;
    private String tipo;
}
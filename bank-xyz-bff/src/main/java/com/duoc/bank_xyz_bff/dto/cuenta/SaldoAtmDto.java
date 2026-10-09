package com.duoc.bank_xyz_bff.dto.cuenta;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaldoAtmDto {
    private Integer cuentaId;
    private String titular;
    private Long saldo;
    private String estado;
}
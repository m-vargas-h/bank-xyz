package com.duoc.bank_xyz_bff.dto.transaccion;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransaccionMobileDto {
    private BigDecimal monto;
    private String tipo;
    private String estado;
}
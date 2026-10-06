package com.duoc.bank_xyz_bff.dto.transaccion;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransaccionResumenDto {
    private Long totalProcesadas;
    private BigDecimal montoTotal;
    private Long totalAnomalias;
}
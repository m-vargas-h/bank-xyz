package com.duoc.bank_xyz_bff.dto.resumen;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ResumenMobileDto {
    private BigDecimal montoTotal;
    private Long totalAnomalias;
}
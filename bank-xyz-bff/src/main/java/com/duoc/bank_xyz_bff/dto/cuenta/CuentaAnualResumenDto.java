package com.duoc.bank_xyz_bff.dto.cuenta;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaAnualResumenDto {
    private Long id;
    private Long cuentaId;
    private Integer totalMovimientos;
    private BigDecimal montoTotal;
    private String fechaReporte;
}
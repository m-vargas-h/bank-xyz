package com.duoc.bank_xyz_bff.dto.transaccion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransaccionDto {
    private Long id;
    private Integer transaccionId;
    private BigDecimal monto;
    private String tipo;
    private String estado;
    private String fecha;
}
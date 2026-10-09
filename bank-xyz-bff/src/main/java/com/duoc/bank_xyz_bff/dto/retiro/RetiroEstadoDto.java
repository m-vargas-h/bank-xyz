package com.duoc.bank_xyz_bff.dto.retiro;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetiroEstadoDto {
    private Integer id;
    private Integer cuentaId;
    private Integer monto;
    private String estado;
    private String motivo;
}
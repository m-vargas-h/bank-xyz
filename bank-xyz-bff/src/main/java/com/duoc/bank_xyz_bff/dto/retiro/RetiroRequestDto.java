package com.duoc.bank_xyz_bff.dto.retiro;

import lombok.Data;

@Data
public class RetiroRequestDto {
    private Integer cuentaId;
    private Integer monto;
}
package com.duoc.bank_xyz_bff.dto.retiro;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetiroResponseDto {
    private Integer id;
    private Integer monto;
    private String estado;
}
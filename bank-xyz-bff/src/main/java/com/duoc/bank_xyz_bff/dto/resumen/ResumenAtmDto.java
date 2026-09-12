package com.duoc.bank_xyz_bff.dto.resumen;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumenAtmDto {
    private Long totalProcesadas;
    private Long totalAnomalias;
}
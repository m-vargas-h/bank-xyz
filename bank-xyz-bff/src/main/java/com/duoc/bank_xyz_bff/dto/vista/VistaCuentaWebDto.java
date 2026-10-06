package com.duoc.bank_xyz_bff.dto.vista;

import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VistaCuentaWebDto {
    private List<CuentaAnualDto> movimientos;
    private List<InteresDto> intereses;
}
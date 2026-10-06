package com.duoc.bank_xyz_bff.dto.vista;

import com.duoc.bank_xyz_bff.dto.interes.InteresMobileDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VistaCuentaAtmDto {
    private List<InteresMobileDto> intereses;
}
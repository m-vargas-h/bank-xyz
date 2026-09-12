package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.dto.ApiResponse;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualMobileDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresMobileDto;
import com.duoc.bank_xyz_bff.dto.resumen.ResumenAtmDto;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/atm")
public class AtmBffController {

    private final BffDataService dataService;

    public AtmBffController(BffDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/saldo/{cuentaId}")
    public ResponseEntity<ApiResponse<List<InteresMobileDto>>> getSaldo(@PathVariable int cuentaId) {
        var data = dataService.getInteresByCuenta(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Cuenta no encontrada: " + cuentaId);
        List<InteresMobileDto> reducida = data.stream()
                .map(i -> new InteresMobileDto(i.getCuentaId(), i.getSaldo(), i.getTipo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("atm", reducida));
    }

    @GetMapping("/transacciones/{cuentaId}")
    public ResponseEntity<ApiResponse<List<CuentaAnualMobileDto>>> getTransacciones(@PathVariable int cuentaId) {
        var data = dataService.getCuentaAnualById(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin transacciones para cuenta: " + cuentaId);
        List<CuentaAnualMobileDto> reducida = data.stream()
                .map(c -> new CuentaAnualMobileDto(c.getCuentaId(), c.getMonto(), c.getTransaccion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("atm", reducida));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<ResumenAtmDto>> getResumen() {
        var full = dataService.getResumenTransacciones();
        return ResponseEntity.ok(new ApiResponse<>("atm",
                new ResumenAtmDto(full.getTotalProcesadas(), full.getTotalAnomalias())));
    }
}
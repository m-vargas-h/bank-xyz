package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.dto.ApiResponse;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualMobileDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresMobileDto;
import com.duoc.bank_xyz_bff.dto.resumen.ResumenMobileDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionMobileDto;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mobile")
public class MobileBffController {

    private final BffDataService dataService;

    public MobileBffController(BffDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/transacciones")
    public ResponseEntity<ApiResponse<List<TransaccionMobileDto>>> getTransacciones(
            @RequestParam(required = false) String tipo) {
        var data = (tipo != null && !tipo.isBlank())
                ? dataService.getTransaccionesByTipo(tipo)
                : dataService.getTransacciones();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin transacciones");
        List<TransaccionMobileDto> reducida = data.stream()
                .map(t -> new TransaccionMobileDto(t.getMonto(), t.getTipo(), t.getEstado()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/transacciones/resumen")
    public ResponseEntity<ApiResponse<ResumenMobileDto>> getResumenTransacciones() {
        var full = dataService.getResumenTransacciones();
        return ResponseEntity.ok(new ApiResponse<>("mobile",
                new ResumenMobileDto(full.getMontoTotal(), full.getTotalAnomalias())));
    }

    @GetMapping("/cuentas")
    public ResponseEntity<ApiResponse<List<CuentaAnualMobileDto>>> getCuentasAnuales() {
        var data = dataService.getCuentasAnuales();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin cuentas anuales");
        List<CuentaAnualMobileDto> reducida = data.stream()
                .map(c -> new CuentaAnualMobileDto(c.getCuentaId(), c.getMonto(), c.getTransaccion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<List<CuentaAnualMobileDto>>> getCuentaById(@PathVariable int id) {
        var data = dataService.getCuentaAnualById(id);
        if (data.isEmpty()) throw new ResourceNotFoundException("Cuenta no encontrada: " + id);
        List<CuentaAnualMobileDto> reducida = data.stream()
                .map(c -> new CuentaAnualMobileDto(c.getCuentaId(), c.getMonto(), c.getTransaccion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/intereses")
    public ResponseEntity<ApiResponse<List<InteresMobileDto>>> getIntereses(
            @RequestParam(required = false) String tipo) {
        var data = (tipo != null && !tipo.isBlank())
                ? dataService.getInteresesByTipo(tipo)
                : dataService.getIntereses();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses");
        List<InteresMobileDto> reducida = data.stream()
                .map(i -> new InteresMobileDto(i.getCuentaId(), i.getSaldo(), i.getTipo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/intereses/{cuentaId}")
    public ResponseEntity<ApiResponse<List<InteresMobileDto>>> getInteresByCuenta(@PathVariable int cuentaId) {
        var data = dataService.getInteresByCuenta(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses para cuenta: " + cuentaId);
        List<InteresMobileDto> reducida = data.stream()
                .map(i -> new InteresMobileDto(i.getCuentaId(), i.getSaldo(), i.getTipo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }
}
package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.dto.ApiResponse;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualDto;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualResumenDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionResumenDto;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/web")
public class WebBffController {

    private final BffDataService dataService;

    public WebBffController(BffDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/transacciones")
    public ResponseEntity<ApiResponse<List<TransaccionDto>>> getTransacciones(
            @RequestParam(required = false) String tipo) {
        List<TransaccionDto> data = (tipo != null && !tipo.isBlank())
                ? dataService.getTransaccionesByTipo(tipo)
                : dataService.getTransacciones();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin transacciones");
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/transacciones/resumen")
    public ResponseEntity<ApiResponse<TransaccionResumenDto>> getResumenTransacciones() {
        return ResponseEntity.ok(new ApiResponse<>("web", dataService.getResumenTransacciones()));
    }

    @GetMapping("/cuentas")
    public ResponseEntity<ApiResponse<List<CuentaAnualDto>>> getCuentasAnuales() {
        List<CuentaAnualDto> data = dataService.getCuentasAnuales();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin cuentas anuales");
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/cuentas/resumen")
    public ResponseEntity<ApiResponse<List<CuentaAnualResumenDto>>> getResumenCuentas() {
        return ResponseEntity.ok(new ApiResponse<>("web", dataService.getResumenCuentas()));
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<List<CuentaAnualDto>>> getCuentaById(@PathVariable int id) {
        List<CuentaAnualDto> data = dataService.getCuentaAnualById(id);
        if (data.isEmpty()) throw new ResourceNotFoundException("Cuenta no encontrada: " + id);
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/intereses")
    public ResponseEntity<ApiResponse<List<InteresDto>>> getIntereses(
            @RequestParam(required = false) String tipo) {
        List<InteresDto> data = (tipo != null && !tipo.isBlank())
                ? dataService.getInteresesByTipo(tipo)
                : dataService.getIntereses();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses");
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/intereses/{cuentaId}")
    public ResponseEntity<ApiResponse<List<InteresDto>>> getInteresByCuenta(@PathVariable int cuentaId) {
        List<InteresDto> data = dataService.getInteresByCuenta(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses para cuenta: " + cuentaId);
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }
}
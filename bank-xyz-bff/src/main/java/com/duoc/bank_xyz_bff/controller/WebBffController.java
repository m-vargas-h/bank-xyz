package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.dto.ApiResponse;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web")
public class WebBffController {

    private final BffDataService dataService;

    public WebBffController(BffDataService dataService) {
        this.dataService = dataService;
    }

    // --- Transacciones ---
    @GetMapping("/transacciones")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTransacciones(
            @RequestParam(required = false) String tipo) {
        List<Map<String, Object>> data = (tipo != null && !tipo.isBlank())
                ? dataService.getTransaccionesByTipo(tipo)
                : dataService.getTransacciones();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin transacciones");
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/transacciones/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumenTransacciones() {
        return ResponseEntity.ok(new ApiResponse<>("web", dataService.getResumenTransacciones()));
    }

    // --- Cuentas anuales ---
    @GetMapping("/cuentas")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCuentasAnuales() {
        List<Map<String, Object>> data = dataService.getCuentasAnuales();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin cuentas anuales");
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/cuentas/resumen")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getResumenCuentas() {
        return ResponseEntity.ok(new ApiResponse<>("web", dataService.getResumenCuentas()));
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCuentaById(@PathVariable int id) {
        List<Map<String, Object>> data = dataService.getCuentaAnualById(id);
        if (data.isEmpty()) throw new ResourceNotFoundException("Cuenta no encontrada: " + id);
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    // --- Intereses ---
    @GetMapping("/intereses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getIntereses(
            @RequestParam(required = false) String tipo) {
        List<Map<String, Object>> data = (tipo != null && !tipo.isBlank())
                ? dataService.getInteresesByTipo(tipo)
                : dataService.getIntereses();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses");
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }

    @GetMapping("/intereses/{cuentaId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInteresByCuenta(@PathVariable int cuentaId) {
        List<Map<String, Object>> data = dataService.getInteresByCuenta(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses para cuenta: " + cuentaId);
        return ResponseEntity.ok(new ApiResponse<>("web", data));
    }
}
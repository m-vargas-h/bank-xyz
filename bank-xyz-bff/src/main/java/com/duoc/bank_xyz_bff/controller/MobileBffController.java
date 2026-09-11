package com.duoc.bank_xyz_bff.controller;

import com.duoc.bank_xyz_bff.dto.ApiResponse;
import com.duoc.bank_xyz_bff.exception.ResourceNotFoundException;
import com.duoc.bank_xyz_bff.service.BffDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mobile")
public class MobileBffController {

    private final BffDataService dataService;

    public MobileBffController(BffDataService dataService) {
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
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "monto", r.get("monto"),
                        "tipo", r.get("tipo"),
                        "estado", r.get("estado")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/transacciones/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumenTransacciones() {
        Map<String, Object> full = dataService.getResumenTransacciones();
        Map<String, Object> reducido = Map.of(
                "monto_total", full.get("monto_total"),
                "total_anomalias", full.get("total_anomalias"));
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducido));
    }

    // --- Cuentas anuales ---
    @GetMapping("/cuentas")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCuentasAnuales() {
        List<Map<String, Object>> data = dataService.getCuentasAnuales();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin cuentas anuales");
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "monto", r.get("monto"),
                        "transaccion", r.get("transaccion")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/cuentas/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCuentaById(@PathVariable int id) {
        List<Map<String, Object>> data = dataService.getCuentaAnualById(id);
        if (data.isEmpty()) throw new ResourceNotFoundException("Cuenta no encontrada: " + id);
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "monto", r.get("monto"),
                        "transaccion", r.get("transaccion")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    // --- Intereses ---
    @GetMapping("/intereses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getIntereses(
            @RequestParam(required = false) String tipo) {
        List<Map<String, Object>> data = (tipo != null && !tipo.isBlank())
                ? dataService.getInteresesByTipo(tipo)
                : dataService.getIntereses();
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses");
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "saldo", r.get("saldo"),
                        "tipo", r.get("tipo")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }

    @GetMapping("/intereses/{cuentaId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInteresByCuenta(@PathVariable int cuentaId) {
        List<Map<String, Object>> data = dataService.getInteresByCuenta(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin intereses para cuenta: " + cuentaId);
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "saldo", r.get("saldo"),
                        "tipo", r.get("tipo")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("mobile", reducida));
    }
}
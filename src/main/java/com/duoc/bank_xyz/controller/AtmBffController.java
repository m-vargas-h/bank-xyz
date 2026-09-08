package com.duoc.bank_xyz.controller;

import com.duoc.bank_xyz.dto.ApiResponse;
import com.duoc.bank_xyz.exception.ResourceNotFoundException;
import com.duoc.bank_xyz.service.BffDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/atm")
public class AtmBffController {

    private final BffDataService dataService;

    public AtmBffController(BffDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/saldo/{cuentaId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSaldo(@PathVariable int cuentaId) {
        List<Map<String, Object>> data = dataService.getInteresByCuenta(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Cuenta no encontrada: " + cuentaId);
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "saldo", r.get("saldo"),
                        "tipo", r.get("tipo")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("atm", reducida));
    }

    @GetMapping("/transacciones/{cuentaId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTransacciones(@PathVariable int cuentaId) {
        List<Map<String, Object>> data = dataService.getCuentaAnualById(cuentaId);
        if (data.isEmpty()) throw new ResourceNotFoundException("Sin transacciones para cuenta: " + cuentaId);
        List<Map<String, Object>> reducida = data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "monto", r.get("monto"),
                        "transaccion", r.get("transaccion")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>("atm", reducida));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumen() {
        Map<String, Object> full = dataService.getResumenTransacciones();
        Map<String, Object> reducido = Map.of(
                "total_procesadas", full.get("total_procesadas"),
                "total_anomalias", full.get("total_anomalias"));
        return ResponseEntity.ok(new ApiResponse<>("atm", reducido));
    }
}
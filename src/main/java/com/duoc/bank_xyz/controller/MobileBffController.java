package com.duoc.bank_xyz.controller;

import com.duoc.bank_xyz.service.BffDataService;
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
    public List<Map<String, Object>> getTransacciones(
            @RequestParam(required = false) String tipo) {
        List<Map<String, Object>> data = tipo != null && !tipo.isBlank()
                ? dataService.getTransaccionesByTipo(tipo)
                : dataService.getTransacciones();
        return data.stream()
                .map(r -> Map.of(
                        "monto", r.get("monto"),
                        "tipo", r.get("tipo"),
                        "estado", r.get("estado")))
                .collect(Collectors.toList());
    }

    @GetMapping("/transacciones/resumen")
    public Map<String, Object> getResumenTransacciones() {
        Map<String, Object> full = dataService.getResumenTransacciones();
        return Map.of(
                "monto_total", full.get("monto_total"),
                "total_anomalias", full.get("total_anomalias"));
    }

    // --- Cuentas anuales ---
    @GetMapping("/cuentas")
    public List<Map<String, Object>> getCuentasAnuales() {
        return dataService.getCuentasAnuales().stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "monto", r.get("monto"),
                        "transaccion", r.get("transaccion")))
                .collect(Collectors.toList());
    }

    @GetMapping("/cuentas/{id}")
    public List<Map<String, Object>> getCuentaById(@PathVariable int id) {
        return dataService.getCuentaAnualById(id).stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "monto", r.get("monto"),
                        "transaccion", r.get("transaccion")))
                .collect(Collectors.toList());
    }

    // --- Intereses ---
    @GetMapping("/intereses")
    public List<Map<String, Object>> getIntereses(
            @RequestParam(required = false) String tipo) {
        List<Map<String, Object>> data = tipo != null && !tipo.isBlank()
                ? dataService.getInteresesByTipo(tipo)
                : dataService.getIntereses();
        return data.stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "saldo", r.get("saldo"),
                        "tipo", r.get("tipo")))
                .collect(Collectors.toList());
    }

    @GetMapping("/intereses/{cuentaId}")
    public List<Map<String, Object>> getInteresByCuenta(@PathVariable int cuentaId) {
        return dataService.getInteresByCuenta(cuentaId).stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "saldo", r.get("saldo"),
                        "tipo", r.get("tipo")))
                .collect(Collectors.toList());
    }
}
package com.duoc.bank_xyz.controller;

import com.duoc.bank_xyz.service.BffDataService;
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

    // Saldo e interés de la cuenta
    @GetMapping("/saldo/{cuentaId}")
    public List<Map<String, Object>> getSaldo(@PathVariable int cuentaId) {
        return dataService.getInteresByCuenta(cuentaId).stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "saldo", r.get("saldo"),
                        "tipo", r.get("tipo")))
                .collect(Collectors.toList());
    }

    // Transacciones de la cuenta (solo las válidas)
    @GetMapping("/transacciones/{cuentaId}")
    public List<Map<String, Object>> getTransacciones(@PathVariable int cuentaId) {
        return dataService.getCuentaAnualById(cuentaId).stream()
                .map(r -> Map.of(
                        "cuenta_id", r.get("cuenta_id"),
                        "monto", r.get("monto"),
                        "transaccion", r.get("transaccion")))
                .collect(Collectors.toList());
    }

    // Resumen global mínimo
    @GetMapping("/resumen")
    public Map<String, Object> getResumen() {
        Map<String, Object> full = dataService.getResumenTransacciones();
        return Map.of(
                "total_procesadas", full.get("total_procesadas"),
                "total_anomalias", full.get("total_anomalias"));
    }
}
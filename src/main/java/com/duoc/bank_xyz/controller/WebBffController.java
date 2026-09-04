package com.duoc.bank_xyz.controller;

import com.duoc.bank_xyz.service.BffDataService;
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
    public List<Map<String, Object>> getTransacciones(
            @RequestParam(required = false) String tipo) {
        if (tipo != null && !tipo.isBlank()) {
            return dataService.getTransaccionesByTipo(tipo);
        }
        return dataService.getTransacciones();
    }

    @GetMapping("/transacciones/resumen")
    public Map<String, Object> getResumenTransacciones() {
        return dataService.getResumenTransacciones();
    }

    // --- Cuentas anuales ---
    @GetMapping("/cuentas")
    public List<Map<String, Object>> getCuentasAnuales() {
        return dataService.getCuentasAnuales();
    }

    @GetMapping("/cuentas/{id}")
    public List<Map<String, Object>> getCuentaById(@PathVariable int id) {
        return dataService.getCuentaAnualById(id);
    }

    @GetMapping("/cuentas/resumen")
    public List<Map<String, Object>> getResumenCuentas() {
        return dataService.getResumenCuentas();
    }

    // --- Intereses ---
    @GetMapping("/intereses")
    public List<Map<String, Object>> getIntereses(
            @RequestParam(required = false) String tipo) {
        if (tipo != null && !tipo.isBlank()) {
            return dataService.getInteresesByTipo(tipo);
        }
        return dataService.getIntereses();
    }

    @GetMapping("/intereses/{cuentaId}")
    public List<Map<String, Object>> getInteresByCuenta(@PathVariable int cuentaId) {
        return dataService.getInteresByCuenta(cuentaId);
    }
}
package com.duoc.ms_cuentas.controllers;

import com.duoc.ms_cuentas.services.CuentasService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas")
public class CuentasController {

    @Value("${ms-cuentas.descripcion:Microservicio de Cuentas}")
    private String descripcion;

    @GetMapping
    public List<Map<String, Object>> listarCuentas() {
        return List.of(
            Map.of("id", 1, "numero", "001-123456", "tipo", "Corriente", "saldo", 1500000),
            Map.of("id", 2, "numero", "001-654321", "tipo", "Ahorro", "saldo", 3200000),
            Map.of("id", 3, "numero", "001-111222", "tipo", "Corriente", "saldo", 850000)
        );
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("servicio", descripcion, "status", "UP");
    }

    @Autowired
    private CuentasService cuentasService;

    @GetMapping("/resilience")
    public CompletableFuture<List<Map<String, Object>>> getCuentasResiliencia() {
        return cuentasService.getCuentasConResiliencia();
    }

    @GetMapping("/ratelimit")
    public List<Map<String, Object>> getCuentasRateLimit() {
        return cuentasService.getCuentasConRateLimit();
    }
}
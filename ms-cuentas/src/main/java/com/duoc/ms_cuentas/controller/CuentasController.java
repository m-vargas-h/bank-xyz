package com.duoc.ms_cuentas.controller;

import com.duoc.ms_cuentas.services.CuentasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cuentas")
public class CuentasController {

    @Autowired
    private CuentasService cuentasService;

    @GetMapping
    public List<Map<String, Object>> listarCuentas() {
        return cuentasService.listarCuentas();
    }

    @GetMapping("/intereses")
    public List<Map<String, Object>> listarIntereses() {
        return cuentasService.listarIntereses();
    }

    @GetMapping("/resumen")
    public List<Map<String, Object>> getResumen() {
        return cuentasService.getResumenCuentas();
    }

    @GetMapping("/resilience")
    public CompletableFuture<List<Map<String, Object>>> getCuentasResiliencia() {
        return cuentasService.getCuentasConResiliencia();
    }

    @GetMapping("/ratelimit")
    public List<Map<String, Object>> getCuentasRateLimit() {
        return cuentasService.getCuentasConRateLimit();
    }
}
package com.duoc.ms_transacciones.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionesController {

    @Value("${ms-transacciones.descripcion:Microservicio de Transacciones}")
    private String descripcion;

    @GetMapping
    public List<Map<String, Object>> listarTransacciones() {
        return List.of(
            Map.of("id", 1, "monto", 150000, "tipo", "Débito", "fecha", "2026-09-01"),
            Map.of("id", 2, "monto", 320000, "tipo", "Crédito", "fecha", "2026-09-05"),
            Map.of("id", 3, "monto", 85000, "tipo", "Débito", "fecha", "2026-09-10")
        );
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("servicio", descripcion, "status", "UP");
    }
}
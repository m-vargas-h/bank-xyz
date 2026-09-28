package com.duoc.ms_transacciones.controllers;

import com.duoc.ms_transacciones.services.TransaccionesService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.concurrent.CompletableFuture;
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

    @Autowired
    private TransaccionesService transaccionesService;

    @GetMapping
    public List<Map<String, Object>> listarTransacciones() {
        return transaccionesService.listarTransacciones();
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("servicio", descripcion, "status", "UP");
    }

    @GetMapping("/resilience")
    public CompletableFuture<List<Map<String, Object>>> getTransaccionesResiliencia() {
        return transaccionesService.getTransaccionesConResiliencia();
    }

    @GetMapping("/ratelimit")
    public List<Map<String, Object>> getTransaccionesRateLimit() {
        return transaccionesService.getTransaccionesConRateLimit();
    }

    @PostMapping
    public Map<String, Object> crearTransaccion(@RequestBody Map<String, Object> body) {
        int monto = (int) body.get("monto");
        String tipo = (String) body.get("tipo");
        return transaccionesService.registrarTransaccion(monto, tipo);
    }

}
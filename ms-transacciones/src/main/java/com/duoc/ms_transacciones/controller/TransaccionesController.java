package com.duoc.ms_transacciones.controllers;

import com.duoc.ms_transacciones.services.TransaccionesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    @GetMapping("/resumen")
    public Map<String, Object> getResumen() {
        return transaccionesService.getResumen();
    }

    @GetMapping("/estado/{id}")
    public Map<String, Object> getEstado(@PathVariable int id) {
        return transaccionesService.obtenerEstado(id);
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
        Object cuentaId = body.get("cuentaId");
        Object monto = body.get("monto");
        Object tipo = body.get("tipo");
        if (!(cuentaId instanceof Number) || !(monto instanceof Number) || !(tipo instanceof String)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "cuentaId, monto y tipo son obligatorios");
        }
        return transaccionesService.registrarTransaccion(
            ((Number) cuentaId).intValue(), ((Number) monto).intValue(), (String) tipo);
    }
}
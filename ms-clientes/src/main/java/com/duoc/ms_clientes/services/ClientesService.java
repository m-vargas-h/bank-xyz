package com.duoc.ms_clientes.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ClientesService {

    @CircuitBreaker(name = "clientesService", fallbackMethod = "fallbackClientes")
    @Retry(name = "clientesService")
    @TimeLimiter(name = "clientesService")
    public CompletableFuture<List<Map<String, Object>>> getClientesConResiliencia() {
        return CompletableFuture.supplyAsync(() -> List.of(
            Map.of("id", 1, "nombre", "Juan Pérez", "rut", "12.345.678-9", "email", "juan@bankxyz.cl"),
            Map.of("id", 2, "nombre", "María González", "rut", "98.765.432-1", "email", "maria@bankxyz.cl"),
            Map.of("id", 3, "nombre", "Carlos Muñoz", "rut", "11.111.111-1", "email", "carlos@bankxyz.cl")
        ));
    }

    @RateLimiter(name = "clientesService", fallbackMethod = "fallbackRateLimit")
    public List<Map<String, Object>> getClientesConRateLimit() {
        return List.of(
            Map.of("id", 1, "nombre", "Juan Pérez", "rut", "12.345.678-9", "email", "juan@bankxyz.cl"),
            Map.of("id", 2, "nombre", "María González", "rut", "98.765.432-1", "email", "maria@bankxyz.cl"),
            Map.of("id", 3, "nombre", "Carlos Muñoz", "rut", "11.111.111-1", "email", "carlos@bankxyz.cl")
        );
    }

    public CompletableFuture<List<Map<String, Object>>> fallbackClientes(Exception e) {
        return CompletableFuture.completedFuture(
            List.of(Map.of("mensaje", "Servicio no disponible. Usando datos de respaldo.", "error", e.getMessage()))
        );
    }

    public List<Map<String, Object>> fallbackRateLimit(Exception e) {
        return List.of(Map.of("mensaje", "Límite de solicitudes alcanzado. Intente más tarde."));
    }
}
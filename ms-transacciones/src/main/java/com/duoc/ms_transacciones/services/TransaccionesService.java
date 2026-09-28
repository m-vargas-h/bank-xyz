package com.duoc.ms_transacciones.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TransaccionesService {

    @CircuitBreaker(name = "transaccionesService", fallbackMethod = "fallbackTransacciones")
    @Retry(name = "transaccionesService")
    @TimeLimiter(name = "transaccionesService")
    public CompletableFuture<List<Map<String, Object>>> getTransaccionesConResiliencia() {
        return CompletableFuture.supplyAsync(() -> List.of(
            Map.of("id", 1, "monto", 150000, "tipo", "Débito", "fecha", "2026-09-01"),
            Map.of("id", 2, "monto", 320000, "tipo", "Crédito", "fecha", "2026-09-05"),
            Map.of("id", 3, "monto", 85000, "tipo", "Débito", "fecha", "2026-09-10")
        ));
    }

    @RateLimiter(name = "transaccionesService", fallbackMethod = "fallbackRateLimit")
    public List<Map<String, Object>> getTransaccionesConRateLimit() {
        return List.of(
            Map.of("id", 1, "monto", 150000, "tipo", "Débito", "fecha", "2026-09-01"),
            Map.of("id", 2, "monto", 320000, "tipo", "Crédito", "fecha", "2026-09-05"),
            Map.of("id", 3, "monto", 85000, "tipo", "Débito", "fecha", "2026-09-10")
        );
    }

    public CompletableFuture<List<Map<String, Object>>> fallbackTransacciones(Exception e) {
        return CompletableFuture.completedFuture(
            List.of(Map.of("mensaje", "Servicio no disponible. Usando datos de respaldo.", "error", e.getMessage()))
        );
    }

    public List<Map<String, Object>> fallbackRateLimit(Exception e) {
        return List.of(Map.of("mensaje", "Límite de solicitudes alcanzado. Intente más tarde."));
    }
}
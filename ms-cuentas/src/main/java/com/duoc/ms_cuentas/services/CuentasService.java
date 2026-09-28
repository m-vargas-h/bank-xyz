package com.duoc.ms_cuentas.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CuentasService {

    @CircuitBreaker(name = "cuentasService", fallbackMethod = "fallbackCuentas")
    @Retry(name = "cuentasService")
    @TimeLimiter(name = "cuentasService")
    public CompletableFuture<List<Map<String, Object>>> getCuentasConResiliencia() {
        return CompletableFuture.supplyAsync(() -> List.of(
            Map.of("id", 1, "numero", "001-123456", "tipo", "Corriente", "saldo", 1500000),
            Map.of("id", 2, "numero", "001-654321", "tipo", "Ahorro", "saldo", 3200000),
            Map.of("id", 3, "numero", "001-111222", "tipo", "Corriente", "saldo", 850000)
        ));
    }

    @RateLimiter(name = "cuentasService", fallbackMethod = "fallbackRateLimit")
    public List<Map<String, Object>> getCuentasConRateLimit() {
        return List.of(
            Map.of("id", 1, "numero", "001-123456", "tipo", "Corriente", "saldo", 1500000),
            Map.of("id", 2, "numero", "001-654321", "tipo", "Ahorro", "saldo", 3200000),
            Map.of("id", 3, "numero", "001-111222", "tipo", "Corriente", "saldo", 850000)
        );
    }

    public CompletableFuture<List<Map<String, Object>>> fallbackCuentas(Exception e) {
        return CompletableFuture.completedFuture(
            List.of(Map.of("mensaje", "Servicio no disponible. Usando datos de respaldo.", "error", e.getMessage()))
        );
    }

    public List<Map<String, Object>> fallbackRateLimit(Exception e) {
        return List.of(Map.of("mensaje", "Límite de solicitudes alcanzado. Intente más tarde."));
    }
}
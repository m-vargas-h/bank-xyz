package com.duoc.ms_transacciones.services;

import com.duoc.ms_transacciones.events.TransaccionEvento;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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

    @Autowired
    private KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    private static final String TOPIC = "transaccion-registrada";

    public Map<String, Object> registrarTransaccion(int monto, String tipo) {
        TransaccionEvento evento = new TransaccionEvento(
            (int)(Math.random() * 1000),
            monto,
            tipo,
            java.time.LocalDate.now().toString(),
            "PENDIENTE"
        );
        kafkaTemplate.send(TOPIC, String.valueOf(evento.getId()), evento);
        return Map.of(
            "id", evento.getId(),
            "monto", evento.getMonto(),
            "tipo", evento.getTipo(),
            "fecha", evento.getFecha(),
            "estado", evento.getEstado(),
            "mensaje", "Transacción registrada y evento publicado en Kafka"
        );
    }

    @KafkaListener(topics = "transaccion-rechazada", groupId = "ms-transacciones-compensacion")
    public void procesarRechazo(@Payload TransaccionEvento evento) {
        System.out.println("[ms-transacciones] Compensación: transacción " + evento.getId()
            + " revertida → estado FALLIDA");
        // En un sistema real: actualizar BD con estado FALLIDA
    }
}
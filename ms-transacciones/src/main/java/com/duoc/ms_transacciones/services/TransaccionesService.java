package com.duoc.ms_transacciones.services;

import com.duoc.ms_transacciones.events.TransaccionEvento;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TransaccionesService {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    private static final String TOPIC = "transaccion-registrada";

    // --- GET desde BD ---
    public List<Map<String, Object>> listarTransacciones() {
        return jdbc.queryForList("SELECT * FROM transaccion_reporte");
    }

    @CircuitBreaker(name = "transaccionesService", fallbackMethod = "fallbackResiliencia")
    @Retry(name = "transaccionesService")
    @TimeLimiter(name = "transaccionesService")
    public CompletableFuture<List<Map<String, Object>>> getTransaccionesConResiliencia() {
        return CompletableFuture.supplyAsync(() ->
            jdbc.queryForList("SELECT * FROM transaccion_reporte")
        );
    }

    @RateLimiter(name = "transaccionesService")
    public List<Map<String, Object>> getTransaccionesConRateLimit() {
        return jdbc.queryForList("SELECT * FROM transaccion_reporte");
    }

    public CompletableFuture<List<Map<String, Object>>> fallbackResiliencia(Throwable t) {
        return CompletableFuture.supplyAsync(() ->
            List.of(Map.of("error", "Servicio no disponible", "detalle", t.getMessage()))
        );
    }

    // --- POST → Kafka ---
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

    // --- Consumer compensación ---
    @KafkaListener(topics = "transaccion-rechazada", groupId = "ms-transacciones-compensacion")
    public void procesarRechazo(@Payload TransaccionEvento evento) {
        System.out.println("[ms-transacciones] Compensación: transacción " + evento.getId()
            + " revertida → estado FALLIDA");
    }
}
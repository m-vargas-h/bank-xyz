package com.duoc.ms_cuentas.services;

import com.duoc.ms_cuentas.events.TransaccionEvento;
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
public class CuentasService {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    private static final String TOPIC_OUT = "cuenta-actualizada";
    private static final String TOPIC_RECHAZO = "transaccion-rechazada";

    // --- GET desde BD ---
    public List<Map<String, Object>> listarCuentas() {
        return jdbc.queryForList("SELECT * FROM cuenta_anual_reporte");
    }

    public List<Map<String, Object>> listarIntereses() {
        return jdbc.queryForList("SELECT * FROM interes_reporte");
    }

    public List<Map<String, Object>> getResumenCuentas() {
        return jdbc.queryForList("SELECT * FROM cuenta_anual_resumen");
    }

    @CircuitBreaker(name = "cuentasService", fallbackMethod = "fallbackResiliencia")
    @Retry(name = "cuentasService")
    @TimeLimiter(name = "cuentasService")
    public CompletableFuture<List<Map<String, Object>>> getCuentasConResiliencia() {
        return CompletableFuture.supplyAsync(() ->
            jdbc.queryForList("SELECT * FROM cuenta_anual_reporte")
        );
    }

    @RateLimiter(name = "cuentasService")
    public List<Map<String, Object>> getCuentasConRateLimit() {
        return jdbc.queryForList("SELECT * FROM cuenta_anual_reporte");
    }

    public CompletableFuture<List<Map<String, Object>>> fallbackResiliencia(Throwable t) {
        return CompletableFuture.supplyAsync(() ->
            List.of(Map.of("error", "Servicio no disponible", "detalle", t.getMessage()))
        );
    }

    // --- Consumer Kafka ---
    @KafkaListener(topics = "transaccion-registrada", groupId = "ms-cuentas-group")
    public void procesarTransaccion(@Payload TransaccionEvento evento) {
        System.out.println("[ms-cuentas] Evento recibido: " + evento.getId() + " | monto: " + evento.getMonto());

        if (evento.getMonto() > 2000000) {
            System.out.println("[ms-cuentas] Saldo insuficiente. Publicando transaccion-rechazada.");
            evento.setEstado("FALLIDA");
            kafkaTemplate.send(TOPIC_RECHAZO, String.valueOf(evento.getId()), evento);
        } else {
            System.out.println("[ms-cuentas] Saldo OK. Actualizando cuenta y publicando cuenta-actualizada.");
            evento.setEstado("COMPLETADA");
            kafkaTemplate.send(TOPIC_OUT, String.valueOf(evento.getId()), evento);
        }
    }
}
package com.duoc.ms_cuentas.services;

import com.duoc.ms_cuentas.events.TransaccionEvento;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
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

    @Autowired
    private KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    private static final String TOPIC_OUT = "cuenta-actualizada";
    private static final String TOPIC_RECHAZO = "transaccion-rechazada";

    @KafkaListener(topics = "transaccion-registrada", groupId = "ms-cuentas-group")
    public void procesarTransaccion(@Payload TransaccionEvento evento) {
        System.out.println("[ms-cuentas] Evento recibido: " + evento.getId() + " | monto: " + evento.getMonto());

        // Simulación: rechazar si monto > 2.000.000
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
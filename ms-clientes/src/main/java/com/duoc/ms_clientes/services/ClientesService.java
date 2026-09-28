package com.duoc.ms_clientes.services;

import com.duoc.ms_clientes.events.TransaccionEvento;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ClientesService {

    @Autowired
    private JdbcTemplate jdbc;

    // --- GET desde BD ---
    public List<Map<String, Object>> listarClientes() {
        return jdbc.queryForList("SELECT cuenta_id, nombre, tipo FROM interes_reporte");
    }

    public List<Map<String, Object>> listarInteresesCliente() {
        return jdbc.queryForList("SELECT * FROM interes_reporte");
    }

    @CircuitBreaker(name = "clientesService", fallbackMethod = "fallbackResiliencia")
    @Retry(name = "clientesService")
    @TimeLimiter(name = "clientesService")
    public CompletableFuture<List<Map<String, Object>>> getClientesConResiliencia() {
        return CompletableFuture.supplyAsync(() ->
            jdbc.queryForList("SELECT cuenta_id, nombre, tipo FROM interes_reporte")
        );
    }

    @RateLimiter(name = "clientesService")
    public List<Map<String, Object>> getClientesConRateLimit() {
        return jdbc.queryForList("SELECT cuenta_id, nombre, tipo FROM interes_reporte");
    }

    public CompletableFuture<List<Map<String, Object>>> fallbackResiliencia(Throwable t) {
        return CompletableFuture.supplyAsync(() ->
            List.of(Map.of("error", "Servicio no disponible", "detalle", t.getMessage()))
        );
    }

    // --- Consumers Kafka ---
    @KafkaListener(topics = "transaccion-registrada", groupId = "ms-clientes-group")
    public void recibirTransaccionRegistrada(@Payload TransaccionEvento evento) {
        System.out.println("[ms-clientes] transaccion-registrada recibida: id=" + evento.getId()
            + " | tipo=" + evento.getTipo() + " | estado=" + evento.getEstado());
    }

    @KafkaListener(topics = "cuenta-actualizada", groupId = "ms-clientes-group")
    public void recibirCuentaActualizada(@Payload TransaccionEvento evento) {
        System.out.println("[ms-clientes] cuenta-actualizada recibida: id=" + evento.getId()
            + " | estado=" + evento.getEstado() + " → registrando historial del cliente.");
    }
}
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CuentasService {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    @Autowired
    private KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    private static final String TOPIC_OUT = "cuenta-actualizada";
    private static final String TOPIC_RECHAZO = "transaccion-rechazada";
    private static final String OK = "OK";
    private static final String DUPLICADO = "DUPLICADO";

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

    public Map<String, Object> obtenerSaldo(int cuentaId) {
        List<Map<String, Object>> filas = jdbc.queryForList(
            "SELECT cuenta_id, titular, saldo, estado FROM cuenta WHERE cuenta_id = ?", cuentaId);
        if (filas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + cuentaId);
        }
        return filas.get(0);
    }

    @CircuitBreaker(name = "cuentasService")
    @Retry(name = "cuentasService", fallbackMethod = "fallbackResiliencia")
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

    // --- Consumer Kafka: aplica el movimiento sobre el saldo real ---
    @KafkaListener(topics = "transaccion-registrada", groupId = "ms-cuentas-group")
    public void procesarTransaccion(@Payload TransaccionEvento evento) {
        System.out.println("[ms-cuentas] Evento recibido: id=" + evento.getId()
            + " | cuenta=" + evento.getCuentaId()
            + " | " + evento.getTipo() + " | monto=" + evento.getMonto());

        String resultado = tx.execute(status -> aplicarMovimiento(evento));

        if (DUPLICADO.equals(resultado)) {
            System.out.println("[ms-cuentas] Evento duplicado ignorado: id=" + evento.getId());
            return;
        }

        String key = String.valueOf(evento.getId());
        if (OK.equals(resultado)) {
            System.out.println("[ms-cuentas] Movimiento aplicado. Publicando cuenta-actualizada.");
            evento.setEstado("COMPLETADA");
            kafkaTemplate.send(TOPIC_OUT, key, evento);
        } else {
            System.out.println("[ms-cuentas] Movimiento rechazado (" + resultado
                + "). Publicando transaccion-rechazada.");
            evento.setEstado("FALLIDA");
            evento.setMotivo(resultado);
            kafkaTemplate.send(TOPIC_RECHAZO, key, evento);
        }
    }

    // Una sola transacción de BD: registro idempotente + actualización atómica del saldo
    private String aplicarMovimiento(TransaccionEvento e) {
        int nuevo = jdbc.update(
            "INSERT IGNORE INTO movimiento_cuenta (transaccion_id, cuenta_id, tipo, monto, resultado) " +
            "VALUES (?, ?, ?, ?, 'PROCESANDO')",
            e.getId(), e.getCuentaId(), e.getTipo(), e.getMonto());
        if (nuevo == 0) {
            return DUPLICADO;
        }

        int filas;
        if ("deposito".equals(e.getTipo())) {
            filas = jdbc.update(
                "UPDATE cuenta SET saldo = saldo + ? WHERE cuenta_id = ? AND estado = 'ACTIVA'",
                e.getMonto(), e.getCuentaId());
        } else {
            filas = jdbc.update(
                "UPDATE cuenta SET saldo = saldo - ? WHERE cuenta_id = ? AND estado = 'ACTIVA' AND saldo >= ?",
                e.getMonto(), e.getCuentaId(), e.getMonto());
        }

        String resultado = OK;
        if (filas == 0) {
            Integer activa = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cuenta WHERE cuenta_id = ? AND estado = 'ACTIVA'",
                Integer.class, e.getCuentaId());
            resultado = (activa != null && activa > 0) ? "Saldo insuficiente" : "Cuenta inexistente o inactiva";
        }

        jdbc.update("UPDATE movimiento_cuenta SET resultado = ?, motivo = ? WHERE transaccion_id = ?",
            OK.equals(resultado) ? "APLICADO" : "RECHAZADO",
            OK.equals(resultado) ? null : resultado,
            e.getId());
        return resultado;
    }
}
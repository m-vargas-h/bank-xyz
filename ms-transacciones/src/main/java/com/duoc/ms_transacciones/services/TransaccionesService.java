package com.duoc.ms_transacciones.services;

import com.duoc.ms_transacciones.events.TransaccionEvento;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class TransaccionesService {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private KafkaTemplate<String, TransaccionEvento> kafkaTemplate;

    private static final String TOPIC = "transaccion-registrada";
    private static final Set<String> TIPOS_VALIDOS = Set.of("deposito", "retiro", "pago");

    // --- GET desde BD ---
    public List<Map<String, Object>> listarTransacciones() {
        return jdbc.queryForList("SELECT * FROM transaccion_reporte");
    }

    public Map<String, Object> getResumen() {
        return jdbc.queryForMap(
            "SELECT COALESCE(SUM(total_procesadas),0) AS total_procesadas, " +
            "COALESCE(SUM(monto_total),0) AS monto_total, " +
            "COALESCE(SUM(total_anomalias),0) AS total_anomalias FROM transaccion_resumen");
    }

    public Map<String, Object> obtenerEstado(int id) {
        List<Map<String, Object>> filas = jdbc.queryForList(
            "SELECT id, cuenta_id, tipo, monto, fecha, estado, motivo FROM transaccion_saga WHERE id = ?", id);
        if (filas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transacción no encontrada: " + id);
        }
        return filas.get(0);
    }

    @CircuitBreaker(name = "transaccionesService")
    @Retry(name = "transaccionesService", fallbackMethod = "fallbackResiliencia")
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

    // --- POST: persiste PENDIENTE y publica el evento (inicio de la Saga) ---
    public Map<String, Object> registrarTransaccion(int cuentaId, int monto, String tipo) {
        if (monto <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a 0");
        }
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo inválido. Use: deposito, retiro o pago");
        }

        String fecha = LocalDate.now().toString();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO transaccion_saga (cuenta_id, tipo, monto, fecha, estado) " +
                "VALUES (?, ?, ?, ?, 'PENDIENTE')", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, cuentaId);
            ps.setString(2, tipo);
            ps.setInt(3, monto);
            ps.setString(4, fecha);
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();

        TransaccionEvento evento = new TransaccionEvento(id, cuentaId, monto, tipo, fecha, "PENDIENTE");
        kafkaTemplate.send(TOPIC, String.valueOf(id), evento).whenComplete((resultado, error) -> {
            if (error != null) {
                jdbc.update("UPDATE transaccion_saga SET estado = 'FALLIDA', motivo = ? " +
                            "WHERE id = ? AND estado = 'PENDIENTE'",
                            "No se pudo publicar el evento en Kafka", id);
            }
        });

        return Map.of(
            "id", id,
            "cuentaId", cuentaId,
            "monto", monto,
            "tipo", tipo,
            "fecha", fecha,
            "estado", "PENDIENTE",
            "mensaje", "Transacción registrada y evento publicado en Kafka"
        );
    }

    // --- Consumers de la Saga: actualizan el estado persistido ---
    @KafkaListener(topics = "cuenta-actualizada", groupId = "ms-transacciones-compensacion")
    public void procesarConfirmacion(@Payload TransaccionEvento evento) {
        int filas = jdbc.update(
            "UPDATE transaccion_saga SET estado = 'COMPLETADA' WHERE id = ? AND estado = 'PENDIENTE'",
            evento.getId());
        System.out.println("[ms-transacciones] Confirmación: transacción " + evento.getId()
            + " → COMPLETADA (filas actualizadas: " + filas + ")");
    }

    @KafkaListener(topics = "transaccion-rechazada", groupId = "ms-transacciones-compensacion")
    public void procesarRechazo(@Payload TransaccionEvento evento) {
        int filas = jdbc.update(
            "UPDATE transaccion_saga SET estado = 'FALLIDA', motivo = ? WHERE id = ? AND estado = 'PENDIENTE'",
            evento.getMotivo(), evento.getId());
        System.out.println("[ms-transacciones] Compensación: transacción " + evento.getId()
            + " revertida → estado FALLIDA | motivo: " + evento.getMotivo()
            + " (filas actualizadas: " + filas + ")");
    }
}
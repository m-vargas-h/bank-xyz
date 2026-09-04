package com.duoc.bank_xyz.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BffDataService {

    private final JdbcTemplate jdbc;

    public BffDataService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // --- Transacciones ---
    public List<Map<String, Object>> getTransacciones() {
        return jdbc.queryForList("SELECT * FROM transaccion_reporte");
    }

    public List<Map<String, Object>> getTransaccionesByTipo(String tipo) {
        return jdbc.queryForList(
            "SELECT * FROM transaccion_reporte WHERE tipo = ?", tipo);
    }

    public Map<String, Object> getResumenTransacciones() {
        return jdbc.queryForMap(
            "SELECT SUM(total_procesadas) AS total_procesadas, " +
            "SUM(monto_total) AS monto_total, " +
            "SUM(total_anomalias) AS total_anomalias " +
            "FROM transaccion_resumen");
    }

    // --- Cuentas anuales ---
    public List<Map<String, Object>> getCuentasAnuales() {
        return jdbc.queryForList("SELECT * FROM cuenta_anual_reporte");
    }

    public List<Map<String, Object>> getCuentaAnualById(int cuentaId) {
        return jdbc.queryForList(
            "SELECT * FROM cuenta_anual_reporte WHERE cuenta_id = ?", cuentaId);
    }

    public List<Map<String, Object>> getResumenCuentas() {
        return jdbc.queryForList("SELECT * FROM cuenta_anual_resumen");
    }

    // --- Intereses ---
    public List<Map<String, Object>> getIntereses() {
        return jdbc.queryForList("SELECT * FROM interes_reporte");
    }

    public List<Map<String, Object>> getInteresesByTipo(String tipo) {
        return jdbc.queryForList(
            "SELECT * FROM interes_reporte WHERE tipo = ?", tipo);
    }

    public List<Map<String, Object>> getInteresByCuenta(int cuentaId) {
        return jdbc.queryForList(
            "SELECT * FROM interes_reporte WHERE cuenta_id = ?", cuentaId);
    }
}
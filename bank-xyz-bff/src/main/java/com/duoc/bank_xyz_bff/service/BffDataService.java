package com.duoc.bank_xyz_bff.service;

import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualDto;
import com.duoc.bank_xyz_bff.dto.cuenta.CuentaAnualResumenDto;
import com.duoc.bank_xyz_bff.dto.interes.InteresDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionDto;
import com.duoc.bank_xyz_bff.dto.transaccion.TransaccionResumenDto;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BffDataService {

    private final JdbcTemplate jdbc;

    public BffDataService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // --- Transacciones ---
    public List<TransaccionDto> getTransacciones() {
        return jdbc.query("SELECT * FROM transaccion_reporte",
                new BeanPropertyRowMapper<>(TransaccionDto.class));
    }

    public List<TransaccionDto> getTransaccionesByTipo(String tipo) {
        return jdbc.query("SELECT * FROM transaccion_reporte WHERE tipo = ?",
                new BeanPropertyRowMapper<>(TransaccionDto.class), tipo);
    }

    public TransaccionResumenDto getResumenTransacciones() {
        return jdbc.queryForObject(
                "SELECT SUM(total_procesadas) AS totalProcesadas, " +
                "SUM(monto_total) AS montoTotal, " +
                "SUM(total_anomalias) AS totalAnomalias " +
                "FROM transaccion_resumen",
                new BeanPropertyRowMapper<>(TransaccionResumenDto.class));
    }

    // --- Cuentas anuales ---
    public List<CuentaAnualDto> getCuentasAnuales() {
        return jdbc.query("SELECT * FROM cuenta_anual_reporte",
                new BeanPropertyRowMapper<>(CuentaAnualDto.class));
    }

    public List<CuentaAnualDto> getCuentaAnualById(int cuentaId) {
        return jdbc.query("SELECT * FROM cuenta_anual_reporte WHERE cuenta_id = ?",
                new BeanPropertyRowMapper<>(CuentaAnualDto.class), cuentaId);
    }

    public List<CuentaAnualResumenDto> getResumenCuentas() {
        return jdbc.query("SELECT * FROM cuenta_anual_resumen",
                new BeanPropertyRowMapper<>(CuentaAnualResumenDto.class));
    }

    // --- Intereses ---
    public List<InteresDto> getIntereses() {
        return jdbc.query("SELECT * FROM interes_reporte",
                new BeanPropertyRowMapper<>(InteresDto.class));
    }

    public List<InteresDto> getInteresesByTipo(String tipo) {
        return jdbc.query("SELECT * FROM interes_reporte WHERE tipo = ?",
                new BeanPropertyRowMapper<>(InteresDto.class), tipo);
    }

    public List<InteresDto> getInteresByCuenta(int cuentaId) {
        return jdbc.query("SELECT * FROM interes_reporte WHERE cuenta_id = ?",
                new BeanPropertyRowMapper<>(InteresDto.class), cuentaId);
    }
}
package com.duoc.bank_xyz.writer;

import com.duoc.bank_xyz.model.Transaccion;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TransaccionResumenWriter implements ItemWriter<Transaccion> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int totalProcesadas = 0;
    private double montoTotal = 0;
    private int totalAnomalias = 0;

    @Override
    public void write(Chunk<? extends Transaccion> chunk) throws Exception {
        for (Transaccion t : chunk) {
            if (t.getMonto() <= 0) {
                totalAnomalias++;
            } else {
                totalProcesadas++;
                montoTotal += t.getMonto();
            }
        }

        jdbcTemplate.update(
            "INSERT INTO transaccion_resumen (fecha_reporte, total_procesadas, monto_total, total_anomalias) " +
            "VALUES (?, ?, ?, ?)",
            LocalDate.now().toString(),
            totalProcesadas,
            montoTotal,
            totalAnomalias
        );

        System.out.println("[ResumenWriter] Resumen transacciones -> procesadas: " + totalProcesadas
                + " | monto total: " + montoTotal
                + " | anomalias: " + totalAnomalias);

        totalProcesadas = 0;
        montoTotal = 0;
        totalAnomalias = 0;
    }
}
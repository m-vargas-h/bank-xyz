package com.duoc.bank_xyz.writer;

import com.duoc.bank_xyz.model.CuentaAnual;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class CuentaAnualResumenWriter implements ItemWriter<CuentaAnual> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends CuentaAnual> chunk) throws Exception {
        Map<Integer, double[]> resumenPorCuenta = new HashMap<>();

        for (CuentaAnual c : chunk) {
            resumenPorCuenta.computeIfAbsent(c.getCuentaId(), k -> new double[]{0, 0});
            resumenPorCuenta.get(c.getCuentaId())[0]++;
            resumenPorCuenta.get(c.getCuentaId())[1] += c.getMonto();
        }

        for (Map.Entry<Integer, double[]> entry : resumenPorCuenta.entrySet()) {
            jdbcTemplate.update(
                "INSERT INTO cuenta_anual_resumen (cuenta_id, total_movimientos, monto_total, fecha_reporte) " +
                "VALUES (?, ?, ?, ?)",
                entry.getKey(),
                (int) entry.getValue()[0],
                entry.getValue()[1],
                LocalDate.now().toString()
            );

            System.out.println("[ResumenWriter] Cuenta " + entry.getKey()
                    + " -> movimientos: " + (int) entry.getValue()[0]
                    + " | monto total: " + entry.getValue()[1]);
        }
    }
}
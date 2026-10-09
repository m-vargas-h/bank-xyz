package com.duoc.bank_xyz_batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CuentaAnualResumenTasklet implements Tasklet {

    private final JdbcTemplate jdbc;

    public CuentaAnualResumenTasklet(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int filas = jdbc.update(
                "INSERT INTO cuenta_anual_resumen (cuenta_id, total_movimientos, monto_total, fecha_reporte) " +
                "SELECT cuenta_id, COUNT(*), SUM(monto), ? FROM cuenta_anual_reporte GROUP BY cuenta_id",
                LocalDate.now().toString());
        System.out.println("[ResumenTasklet] cuenta_anual_resumen -> " + filas + " cuentas resumidas");
        return RepeatStatus.FINISHED;
    }
}
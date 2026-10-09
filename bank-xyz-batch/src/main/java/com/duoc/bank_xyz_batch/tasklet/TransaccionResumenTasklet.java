package com.duoc.bank_xyz_batch.tasklet;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class TransaccionResumenTasklet implements Tasklet {

    private final JdbcTemplate jdbc;

    public TransaccionResumenTasklet(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobExecution job = chunkContext.getStepContext().getStepExecution().getJobExecution();

        // Anomalías = registros omitidos por el step principal (monto inválido, tipo inválido, fecha inválida...)
        long anomalias = job.getStepExecutions().stream()
                .filter(s -> s.getStepName().equals("dailyTransactionStep"))
                .mapToLong(StepExecution::getSkipCount)
                .sum();

        Map<String, Object> r = jdbc.queryForMap(
                "SELECT COUNT(*) AS total, COALESCE(SUM(monto), 0) AS monto FROM transaccion_reporte");

        int procesadas = ((Number) r.get("total")).intValue();
        double montoTotal = ((Number) r.get("monto")).doubleValue();

        jdbc.update("INSERT INTO transaccion_resumen (fecha_reporte, total_procesadas, monto_total, total_anomalias) " +
                        "VALUES (?, ?, ?, ?)",
                LocalDate.now().toString(), procesadas, montoTotal, (int) anomalias);

        System.out.println("[ResumenTasklet] transacciones -> procesadas: " + procesadas
                + " | monto total: " + montoTotal + " | anomalias: " + anomalias);
        return RepeatStatus.FINISHED;
    }
}
package com.duoc.bank_xyz_batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class LimpiezaTasklet implements Tasklet {

    private final JdbcTemplate jdbc;
    private final String[] tablas;

    public LimpiezaTasklet(JdbcTemplate jdbc, String... tablas) {
        this.jdbc = jdbc;
        this.tablas = tablas;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        for (String tabla : tablas) {
            int filas = jdbc.update("DELETE FROM " + tabla);
            System.out.println("[Limpieza] " + tabla + " -> " + filas + " filas eliminadas");
        }
        return RepeatStatus.FINISHED;
    }
}
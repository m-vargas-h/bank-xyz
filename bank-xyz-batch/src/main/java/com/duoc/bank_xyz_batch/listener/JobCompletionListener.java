package com.duoc.bank_xyz_batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JobCompletionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("[JobListener] Iniciando Job: "
                + jobExecution.getJobInstance().getJobName()
                + " | JobId: " + jobExecution.getJobId()
                + " | threads=" + jobExecution.getJobParameters().getLong("threads")
                + " | chunkSize=" + jobExecution.getJobParameters().getLong("chunkSize"));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long ms = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
        System.out.println("[JobListener] Job finalizado: "
                + jobExecution.getJobInstance().getJobName()
                + " | Status: " + jobExecution.getStatus()
                + " | Duración: " + ms + "ms");
        for (StepExecution s : jobExecution.getStepExecutions()) {
            System.out.println("[JobListener]   Step " + s.getStepName()
                    + " | " + s.getStatus()
                    + " | leídos=" + s.getReadCount()
                    + " | escritos=" + s.getWriteCount()
                    + " | omitidos=" + s.getSkipCount());
        }
    }
}
package com.duoc.bank_xyz.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("[JobListener] Iniciando Job: "
                + jobExecution.getJobInstance().getJobName()
                + " | JobId: " + jobExecution.getJobId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("[JobListener] Job finalizado: "
                + jobExecution.getJobInstance().getJobName()
                + " | Status: " + jobExecution.getStatus()
                + " | Duración: " + (jobExecution.getEndTime().toLocalTime()
                        .toNanoOfDay() - jobExecution.getStartTime().toLocalTime()
                        .toNanoOfDay()) / 1_000_000 + "ms");
    }
}
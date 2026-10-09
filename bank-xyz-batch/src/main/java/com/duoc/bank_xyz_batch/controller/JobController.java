package com.duoc.bank_xyz_batch.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job dailyTransactionReportJob;
    private final Job monthlyInterestJob;
    private final Job annualStatementJob;

    @Value("${batch.thread-pool-size}")
    private int defaultThreads;

    @Value("${batch.chunk-size}")
    private int defaultChunkSize;

    @Value("${batch.max-reintentos:3}")
    private int maxReintentos;

    public JobController(JobLauncher jobLauncher,
                         @Qualifier("dailyTransactionReportJob") Job dailyTransactionReportJob,
                         @Qualifier("monthlyInterestJob") Job monthlyInterestJob,
                         @Qualifier("annualStatementJob") Job annualStatementJob) {
        this.jobLauncher = jobLauncher;
        this.dailyTransactionReportJob = dailyTransactionReportJob;
        this.monthlyInterestJob = monthlyInterestJob;
        this.annualStatementJob = annualStatementJob;
    }

    @PostMapping("/daily-transaction")
    public String runDailyTransactionJob(@RequestParam(required = false) Integer threads,
                                         @RequestParam(required = false) Integer chunkSize) throws Exception {
        return ejecutar(dailyTransactionReportJob, threads, chunkSize);
    }

    @PostMapping("/monthly-interest")
    public String runMonthlyInterestJob(@RequestParam(required = false) Integer threads,
                                        @RequestParam(required = false) Integer chunkSize) throws Exception {
        return ejecutar(monthlyInterestJob, threads, chunkSize);
    }

    @PostMapping("/annual-statement")
    public String runAnnualStatementJob(@RequestParam(required = false) Integer threads,
                                        @RequestParam(required = false) Integer chunkSize) throws Exception {
        return ejecutar(annualStatementJob, threads, chunkSize);
    }

    private String ejecutar(Job job, Integer threads, Integer chunkSize) throws Exception {
        int t = threads != null ? threads : defaultThreads;
        int c = chunkSize != null ? chunkSize : defaultChunkSize;
        if (t < 1 || c < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "threads y chunkSize deben ser >= 1");
        }

        JobExecution execution = null;
        int intento = 0;
        while (intento < maxReintentos) {
            intento++;
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addLong("threads", (long) t)
                    .addLong("chunkSize", (long) c)
                    .toJobParameters();
            try {
                execution = jobLauncher.run(job, params);
            } catch (Exception e) {
                execution = null;
                System.out.println("[JobController] " + job.getName() + " intento " + intento
                        + " lanzó excepción: " + e.getMessage());
            }
            if (execution != null && execution.getStatus() == BatchStatus.COMPLETED) {
                break;
            }
            if (intento < maxReintentos) {
                System.out.println("[JobController] " + job.getName() + " intento " + intento
                        + " falló. Reintentando...");
                Thread.sleep(2000L * intento);
            }
        }

        boolean ok = execution != null && execution.getStatus() == BatchStatus.COMPLETED;
        String resultado = String.format("%s | estado=%s | intentos=%d | threads=%d | chunkSize=%d",
                job.getName(), ok ? "COMPLETED" : "FAILED", intento, t, c);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, resultado);
        }
        return resultado;
    }
}
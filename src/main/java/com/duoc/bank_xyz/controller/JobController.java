package com.duoc.bank_xyz.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobLauncher jobLauncher;

    @Value("${batch.thread-pool-size}")
    private int defaultThreadPoolSize;

    @Value("${batch.chunk-size}")
    private int defaultChunkSize;

    @Autowired
    @Qualifier("dailyTransactionReportJob")
    private Job dailyTransactionReportJob;

    @PostMapping("/daily-transaction")
    public String runDailyTransactionJob(
            @RequestParam(required = false) Integer threads,
            @RequestParam(required = false) Integer chunkSize) throws Exception {

        aplicarConfiguracion(threads, chunkSize);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("threads", (long) defaultThreadPoolSize)
                .addLong("chunkSize", (long) defaultChunkSize)
                .toJobParameters();
        jobLauncher.run(dailyTransactionReportJob, params);
        return String.format("dailyTransactionReportJob ejecutado | threads=%d | chunkSize=%d",
                defaultThreadPoolSize, defaultChunkSize);
    }

    @Autowired
    @Qualifier("monthlyInterestJob")
    private Job monthlyInterestJob;

    @PostMapping("/monthly-interest")
    public String runMonthlyInterestJob(
            @RequestParam(required = false) Integer threads,
            @RequestParam(required = false) Integer chunkSize) throws Exception {

        aplicarConfiguracion(threads, chunkSize);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("threads", (long) defaultThreadPoolSize)
                .addLong("chunkSize", (long) defaultChunkSize)
                .toJobParameters();
        jobLauncher.run(monthlyInterestJob, params);
        return String.format("monthlyInterestJob ejecutado | threads=%d | chunkSize=%d",
                defaultThreadPoolSize, defaultChunkSize);
    }

    @Autowired
    @Qualifier("annualStatementJob")
    private Job annualStatementJob;

    @PostMapping("/annual-statement")
    public String runAnnualStatementJob(
            @RequestParam(required = false) Integer threads,
            @RequestParam(required = false) Integer chunkSize) throws Exception {

        aplicarConfiguracion(threads, chunkSize);

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("threads", (long) defaultThreadPoolSize)
                .addLong("chunkSize", (long) defaultChunkSize)
                .toJobParameters();
        jobLauncher.run(annualStatementJob, params);
        return String.format("annualStatementJob ejecutado | threads=%d | chunkSize=%d",
                defaultThreadPoolSize, defaultChunkSize);
    }

    private void aplicarConfiguracion(Integer threads, Integer chunkSize) {
        if (threads != null) defaultThreadPoolSize = threads;
        if (chunkSize != null) defaultChunkSize = chunkSize;
    }
}
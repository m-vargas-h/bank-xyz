package com.duoc.bank_xyz.config;

import com.duoc.bank_xyz.listener.BankSkipListener;
import com.duoc.bank_xyz.listener.JobCompletionListener;
import com.duoc.bank_xyz.model.Transaccion;
import com.duoc.bank_xyz.policy.BankSkipPolicy;
import com.duoc.bank_xyz.processor.TransaccionProcessor;
import com.duoc.bank_xyz.writer.TransaccionResumenWriter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DailyTransactionJobConfig {

    @Value("${batch.thread-pool-size}")
    private int threadPoolSize;

    @Value("${batch.chunk-size}")
    private int chunkSize;

    @Bean
    public FlatFileItemReader<Transaccion> transaccionReader() {
        return new FlatFileItemReaderBuilder<Transaccion>()
                .name("transaccionReader")
                .resource(new ClassPathResource("transacciones.csv"))
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .linesToSkip(1)
                .targetType(Transaccion.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .dataSource(dataSource)
                .sql("INSERT INTO transaccion_reporte (transaccion_id, fecha, monto, tipo, estado) " +
                    "VALUES (:id, :fecha, :monto, :tipo, 'PROCESADO')")
                .beanMapped()
                .build();
    }

    @Bean
    public Step dailyTransactionStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    SynchronizedItemStreamReader<Transaccion> synchronizedTransaccionReader,
                                    TransaccionProcessor transaccionProcessor,
                                    JdbcBatchItemWriter<Transaccion> transaccionWriter,
                                    TaskExecutor dailyTransactionTaskExecutor,
                                    BankSkipPolicy bankSkipPolicy,
                                    BankSkipListener<Transaccion, Transaccion> bankSkipListener) {
        return new StepBuilder("dailyTransactionStep", jobRepository)
                .<Transaccion, Transaccion>chunk(chunkSize, transactionManager)
                .reader(synchronizedTransaccionReader)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)
                .taskExecutor(dailyTransactionTaskExecutor)
                .faultTolerant()
                .skipPolicy(bankSkipPolicy)
                .retry(org.springframework.dao.DataAccessException.class)
                .retryLimit(3)
                .backOffPolicy(new ExponentialBackOffPolicy())
                .listener((SkipListener<Transaccion, Transaccion>) bankSkipListener)
                .build();
    }

    @Bean
    public Step dailyTransactionResumenStep(JobRepository jobRepository,
                                            PlatformTransactionManager transactionManager,
                                            TransaccionResumenWriter transaccionResumenWriter,
                                            BankSkipPolicy bankSkipPolicy) {
        return new StepBuilder("dailyTransactionResumenStep", jobRepository)
                .<Transaccion, Transaccion>chunk(100, transactionManager)
                .reader(transaccionReader())
                .writer(transaccionResumenWriter)
                .faultTolerant()
                .skipPolicy(bankSkipPolicy)
                .build();
    }

    @Bean
    public Job dailyTransactionReportJob(JobRepository jobRepository,
                                        Step dailyTransactionStep,
                                        Step dailyTransactionResumenStep,
                                        JobCompletionListener jobCompletionListener) {
        return new JobBuilder("dailyTransactionReportJob", jobRepository)
                .listener(jobCompletionListener)
                .start(dailyTransactionStep)
                .next(dailyTransactionResumenStep)
                .build();
    }

    @Bean
    public TaskExecutor dailyTransactionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("daily-batch-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SynchronizedItemStreamReader<Transaccion> synchronizedTransaccionReader() {
        return new SynchronizedItemStreamReaderBuilder<Transaccion>()
                .delegate(transaccionReader())
                .build();
    }
}
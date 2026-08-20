package com.duoc.bank_xyz.config;

import com.duoc.bank_xyz.model.CuentaAnual;
import com.duoc.bank_xyz.policy.BankSkipPolicy;
import com.duoc.bank_xyz.processor.CuentaAnualProcessor;
import org.springframework.batch.core.Job;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AnnualStatementJobConfig {

    @Bean
    public FlatFileItemReader<CuentaAnual> cuentaAnualReader() {
        return new FlatFileItemReaderBuilder<CuentaAnual>()
                .name("cuentaAnualReader")
                .resource(new ClassPathResource("cuentas_anuales.csv"))
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .linesToSkip(1)
                .targetType(CuentaAnual.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CuentaAnual>()
                .dataSource(dataSource)
                .sql("INSERT INTO cuenta_anual_reporte (cuenta_id, fecha, transaccion, monto, descripcion, estado) " +
                     "VALUES (:cuentaId, :fecha, :transaccion, :monto, :descripcion, 'PROCESADO')")
                .beanMapped()
                .build();
    }

    @Bean
    public Step annualStatementStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    SynchronizedItemStreamReader<CuentaAnual> synchronizedCuentaAnualReader,
                                    CuentaAnualProcessor cuentaAnualProcessor,
                                    JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter,
                                    TaskExecutor annualStatementTaskExecutor,
                                    BankSkipPolicy bankSkipPolicy) {
        return new StepBuilder("annualStatementStep", jobRepository)
                .<CuentaAnual, CuentaAnual>chunk(5, transactionManager)
                .reader(synchronizedCuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
                .taskExecutor(annualStatementTaskExecutor)
                .faultTolerant()
                .skipPolicy(bankSkipPolicy)
                .build();
    }

    @Bean
    public Job annualStatementJob(JobRepository jobRepository,
                                  Step annualStatementStep) {
        return new JobBuilder("annualStatementJob", jobRepository)
                .start(annualStatementStep)
                .build();
    }

    @Bean
    public TaskExecutor annualStatementTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("annual-batch-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SynchronizedItemStreamReader<CuentaAnual> synchronizedCuentaAnualReader() {
        return new SynchronizedItemStreamReaderBuilder<CuentaAnual>()
                .delegate(cuentaAnualReader())
                .build();
    }
}
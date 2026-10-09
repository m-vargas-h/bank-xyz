package com.duoc.bank_xyz_batch.config;

import com.duoc.bank_xyz_batch.listener.BankSkipListener;
import com.duoc.bank_xyz_batch.listener.JobCompletionListener;
import com.duoc.bank_xyz_batch.model.CuentaAnual;
import com.duoc.bank_xyz_batch.policy.BankSkipPolicy;
import com.duoc.bank_xyz_batch.processor.CuentaAnualProcessor;
import com.duoc.bank_xyz_batch.tasklet.CuentaAnualResumenTasklet;
import com.duoc.bank_xyz_batch.tasklet.LimpiezaTasklet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
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
                .saveState(false)
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<CuentaAnual> synchronizedCuentaAnualReader() {
        return new SynchronizedItemStreamReaderBuilder<CuentaAnual>()
                .delegate(cuentaAnualReader())
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
    public Step annualStatementLimpiezaStep(JobRepository jobRepository,
                                            PlatformTransactionManager transactionManager,
                                            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("annualStatementLimpiezaStep", jobRepository)
                .tasklet(new LimpiezaTasklet(jdbcTemplate, "cuenta_anual_reporte", "cuenta_anual_resumen"),
                        transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step annualStatementStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    SynchronizedItemStreamReader<CuentaAnual> synchronizedCuentaAnualReader,
                                    CuentaAnualProcessor cuentaAnualProcessor,
                                    JdbcBatchItemWriter<CuentaAnual> cuentaAnualWriter,
                                    BankSkipPolicy bankSkipPolicy,
                                    BankSkipListener<CuentaAnual, CuentaAnual> bankSkipListener,
                                    @Value("#{jobParameters['threads']}") Long threads,
                                    @Value("#{jobParameters['chunkSize']}") Long chunkSize) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("annual-batch-");
        executor.setConcurrencyLimit(threads.intValue());

        return new StepBuilder("annualStatementStep", jobRepository)
                .<CuentaAnual, CuentaAnual>chunk(chunkSize.intValue(), transactionManager)
                .reader(synchronizedCuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
                .taskExecutor(executor)
                .throttleLimit(threads.intValue())
                .faultTolerant()
                .skipPolicy(bankSkipPolicy)
                .retry(org.springframework.dao.DataAccessException.class)
                .retryLimit(3)
                .backOffPolicy(new ExponentialBackOffPolicy())
                .listener((SkipListener<CuentaAnual, CuentaAnual>) bankSkipListener)
                .build();
    }

    @Bean
    public Step annualStatementResumenStep(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager,
                                           CuentaAnualResumenTasklet cuentaAnualResumenTasklet) {
        return new StepBuilder("annualStatementResumenStep", jobRepository)
                .tasklet(cuentaAnualResumenTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job annualStatementJob(JobRepository jobRepository,
                                  Step annualStatementLimpiezaStep,
                                  Step annualStatementStep,
                                  Step annualStatementResumenStep,
                                  JobCompletionListener jobCompletionListener) {
        return new JobBuilder("annualStatementJob", jobRepository)
                .listener(jobCompletionListener)
                .start(annualStatementLimpiezaStep)
                .next(annualStatementStep)
                .next(annualStatementResumenStep)
                .build();
    }
}
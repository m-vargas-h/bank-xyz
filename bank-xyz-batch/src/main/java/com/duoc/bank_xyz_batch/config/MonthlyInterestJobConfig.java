package com.duoc.bank_xyz_batch.config;

import com.duoc.bank_xyz_batch.listener.BankSkipListener;
import com.duoc.bank_xyz_batch.listener.JobCompletionListener;
import com.duoc.bank_xyz_batch.model.Interes;
import com.duoc.bank_xyz_batch.policy.BankSkipPolicy;
import com.duoc.bank_xyz_batch.processor.InteresProcessor;
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
public class MonthlyInterestJobConfig {

    @Bean
    public FlatFileItemReader<Interes> interesReader() {
        return new FlatFileItemReaderBuilder<Interes>()
                .name("interesReader")
                .resource(new ClassPathResource("intereses_trimestrales.csv"))
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .linesToSkip(1)
                .targetType(Interes.class)
                .saveState(false)
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<Interes> synchronizedInteresReader() {
        return new SynchronizedItemStreamReaderBuilder<Interes>()
                .delegate(interesReader())
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Interes> interesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Interes>()
                .dataSource(dataSource)
                .sql("INSERT INTO interes_reporte (cuenta_id, nombre, saldo, tipo, saldo_final, interes) " +
                     "VALUES (:cuentaId, :nombre, :saldo, :tipo, :saldoFinal, :interes)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step monthlyInterestLimpiezaStep(JobRepository jobRepository,
                                            PlatformTransactionManager transactionManager,
                                            JdbcTemplate jdbcTemplate) {
        return new StepBuilder("monthlyInterestLimpiezaStep", jobRepository)
                .tasklet(new LimpiezaTasklet(jdbcTemplate, "interes_reporte"), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step monthlyInterestStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    SynchronizedItemStreamReader<Interes> synchronizedInteresReader,
                                    InteresProcessor interesProcessor,
                                    JdbcBatchItemWriter<Interes> interesWriter,
                                    BankSkipPolicy bankSkipPolicy,
                                    BankSkipListener<Interes, Interes> bankSkipListener,
                                    @Value("#{jobParameters['threads']}") Long threads,
                                    @Value("#{jobParameters['chunkSize']}") Long chunkSize) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("monthly-batch-");
        executor.setConcurrencyLimit(threads.intValue());

        return new StepBuilder("monthlyInterestStep", jobRepository)
                .<Interes, Interes>chunk(chunkSize.intValue(), transactionManager)
                .reader(synchronizedInteresReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .taskExecutor(executor)
                .throttleLimit(threads.intValue())
                .faultTolerant()
                .skipPolicy(bankSkipPolicy)
                .retry(org.springframework.dao.DataAccessException.class)
                .retryLimit(3)
                .backOffPolicy(new ExponentialBackOffPolicy())
                .listener((SkipListener<Interes, Interes>) bankSkipListener)
                .build();
    }

    @Bean
    public Job monthlyInterestJob(JobRepository jobRepository,
                                  Step monthlyInterestLimpiezaStep,
                                  Step monthlyInterestStep,
                                  JobCompletionListener jobCompletionListener) {
        return new JobBuilder("monthlyInterestJob", jobRepository)
                .listener(jobCompletionListener)
                .start(monthlyInterestLimpiezaStep)
                .next(monthlyInterestStep)
                .build();
    }
}
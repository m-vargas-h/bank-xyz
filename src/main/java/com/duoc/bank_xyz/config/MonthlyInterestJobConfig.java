package com.duoc.bank_xyz.config;

import com.duoc.bank_xyz.listener.BankSkipListener;
import com.duoc.bank_xyz.listener.JobCompletionListener;
import com.duoc.bank_xyz.model.Interes;
import com.duoc.bank_xyz.policy.BankSkipPolicy;
import com.duoc.bank_xyz.processor.InteresProcessor;
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
public class MonthlyInterestJobConfig {

    @Value("${batch.thread-pool-size}")
    private int threadPoolSize;

    @Value("${batch.chunk-size}")
    private int chunkSize;

    @Bean
    public FlatFileItemReader<Interes> interesReader() {
        return new FlatFileItemReaderBuilder<Interes>()
                .name("interesReader")
                .resource(new ClassPathResource("intereses.csv"))
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .linesToSkip(1)
                .targetType(Interes.class)
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
    public Step monthlyInterestStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    SynchronizedItemStreamReader<Interes> synchronizedInteresReader,
                                    InteresProcessor interesProcessor,
                                    JdbcBatchItemWriter<Interes> interesWriter,
                                    TaskExecutor monthlyInterestTaskExecutor,
                                    BankSkipPolicy bankSkipPolicy,
                                    BankSkipListener<Interes, Interes> bankSkipListener) {
        return new StepBuilder("monthlyInterestStep", jobRepository)
                .<Interes, Interes>chunk(chunkSize, transactionManager)
                .reader(synchronizedInteresReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .taskExecutor(monthlyInterestTaskExecutor)
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
                                Step monthlyInterestStep,
                                JobCompletionListener jobCompletionListener) {
        return new JobBuilder("monthlyInterestJob", jobRepository)
                .listener(jobCompletionListener)
                .start(monthlyInterestStep)
                .build();
    }

    @Bean
    public TaskExecutor monthlyInterestTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("monthly-batch-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SynchronizedItemStreamReader<Interes> synchronizedInteresReader() {
        return new SynchronizedItemStreamReaderBuilder<Interes>()
                .delegate(interesReader())
                .build();
    }
}
package com.duoc.bank_xyz.config;

import com.duoc.bank_xyz.model.Transaccion;
import com.duoc.bank_xyz.processor.TransaccionProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DailyTransactionJobConfig {

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
                .sql("INSERT INTO transaccion_reporte (id, fecha, monto, tipo, estado) " +
                     "VALUES (:id, :fecha, :monto, :tipo, 'PROCESADO')")
                .beanMapped()
                .build();
    }

    @Bean
    public Step dailyTransactionStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<Transaccion> transaccionReader,
                                     TransaccionProcessor transaccionProcessor,
                                     JdbcBatchItemWriter<Transaccion> transaccionWriter) {
        return new StepBuilder("dailyTransactionStep", jobRepository)
                .<Transaccion, Transaccion>chunk(10, transactionManager)
                .reader(transaccionReader)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)
                .build();
    }

    @Bean
    public Job dailyTransactionReportJob(JobRepository jobRepository,
                                         Step dailyTransactionStep) {
        return new JobBuilder("dailyTransactionReportJob", jobRepository)
                .start(dailyTransactionStep)
                .build();
    }
}
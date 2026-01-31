package com.foo.hellobatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((StepContribution stepContribution, ChunkContext chunkContext) -> {
                    // Simulates step execution logic
                    log.info("Executing step1...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job helloWorldJob(Step step1) {
        return new JobBuilder("helloWorldJob", jobRepository)
                .start(step1)
                .build();
    }

    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job helloWorldJob) {
        return args -> {
            log.info("Starting the helloWorldJob...");
            jobLauncher.run(
                    helloWorldJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis()) // Ensure unique JobParameters
                            .toJobParameters()
            );
            log.info("Job execution completed.");
        };
    }

}



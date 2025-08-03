/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foo.transitions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;


    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((StepContribution stepContribution, ChunkContext chunkContext) -> {
                    log.info("Executing step1...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // Use of anonymous class vs lambda
    @Bean
    public Step step2() {
        return new StepBuilder("step2", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("Executing step2...");
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }


    @Bean
    public Step step3() {
        return new StepBuilder("step3", jobRepository)
                .tasklet((StepContribution stepContribution, ChunkContext chunkContext) -> {
                    log.info("Executing step3...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job helloWorldJob) {
        return args -> {
            log.info("Starting the transitionJobNext...");
            jobLauncher.run(
                    helloWorldJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis()) // Ensure unique JobParameters
                            .toJobParameters()
            );
            log.info("Job execution completed.");
        };
    }


    @Bean
    public Job transitionJobSimplestNext(JobRepository jobRepository) {
        return new JobBuilder("transitionJobNext", jobRepository)
                .start(step1())
                .next(step2())
                .next(step3())
                .build();
    }

//     Same as above, but using on() and from() to specify the transition
//    @Bean
//    public Job transitionJobSimpleNext(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("transitionJobNext", jobRepository)
//                .start(step1(jobRepository, transactionManager))
//                .on("COMPLETED").to(step2(jobRepository, transactionManager))
//                .from(step2(jobRepository, transactionManager)).on("COMPLETED").to(step3(jobRepository, transactionManager))
//                .from(step3(jobRepository, transactionManager)).end()
//                .build();
//    }

// Demo of fail()
//    @Bean
//    public Job transitionJobFaildDemo(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("transitionJobNext", jobRepository)
//                .start(step1(jobRepository, transactionManager))
//                .on("COMPLETED").to(step2(jobRepository, transactionManager))
//                .from(step2(jobRepository, transactionManager)).on("COMPLETED").fail()
//                .from(step3(jobRepository, transactionManager)).end()
//                .build();
//    }

    // Demo of fail()
//    @Bean
//    public Job transitionJobFaildDemo(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("transitionJobNext", jobRepository)
//                .start(step1(jobRepository, transactionManager))
//                .on("COMPLETED").to(step2(jobRepository, transactionManager))
//                .from(step2(jobRepository, transactionManager)).on("COMPLETED").fail()
//                .from(step3(jobRepository, transactionManager)).end()
//                .build();
//        }

//    @Bean
//    public Job transitionJobFaildDemo(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("transitionJobNext", jobRepository, transactionManager)
//
//                .start(step1(jobRepository, transactionManager))
//                .on("COMPLETED").to(step2(jobRepository, transactionManager))
//                .from(step2(jobRepository, transactionManager)).on("COMPLETED").stopAndRestart(step3(jobRepository, transactionManager))
//                .from(step3(jobRepository, transactionManager)).end()
//                .build();
//    }
}

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
package io.spring.batch.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Michael Minella
 */
@Configuration
@Slf4j
public class StepTranstionConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> This is step 1");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> This is step 2");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info(">> This is step 3");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }


//    @Bean
//    public Job transitionJobSimplestNext() {
//        return jobBuilderFactory.get("transitionJobNext")
//                .start(step1())
//                .next(step2())
//                .next(step3())
//                .build();
//    }

    // Same as above, but using on() and from() to specify the transition
//    @Bean
//    public Job transitionJobSimpleNext() {
//        return jobBuilderFactory.get("transitionJobNext")
//                .start(step1())
//                .on("COMPLETED").to(step2())
//                .from(step2()).on("COMPLETED").to(step3())
//                .from(step3()).end()
//                .build();
//    }

    // Demo of fail()
//    @Bean
//    public Job transitionJobFaildDemo() {
//        return jobBuilderFactory.get("transitionJobNext")
//                .start(step1())
//                .on("COMPLETED").to(step2())
//                .from(step2()).on("COMPLETED").fail()
//                .from(step3()).end()
//                .build();
//    }

    @Bean
    public Job transitionJobFaildDemo() {
        return jobBuilderFactory.get("transitionJobNext")
                .start(step1())
                .on("COMPLETED").to(step2())
                .from(step2()).on("COMPLETED").stopAndRestart(step3())
                .from(step3()).end()
                .build();
    }
}
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * step1 & step2 Steps are defined and  foo and bar flows both (step1 -> step2).
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class FlowConfiguration {

	private final JobRepository jobRepository;

	private final PlatformTransactionManager transactionManager;

	@Bean
	public Step step1() {
		return new StepBuilder("step1", jobRepository)
				.tasklet((StepContribution stepContribution, ChunkContext chunkContext)  -> {
					System.out.println("Step 1 from inside flow foo");
					return RepeatStatus.FINISHED;
				}, transactionManager).build();
	}

	@Bean
	public Step step2() {
		return new StepBuilder("step2", jobRepository)
				.tasklet((StepContribution stepContribution, ChunkContext chunkContext)  -> {
					System.out.println("Step 2 from inside flow foo");
					return RepeatStatus.FINISHED;
				}, transactionManager).build();
	}

	@Bean
	public Flow foo() {
		FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("foo");

		flowBuilder.start(step1())
				.next(step2())
				.end();

		return flowBuilder.build();
	}

	@Bean
	public Flow bar() {
		FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("bar");

		flowBuilder.start(step1())
				.next(step2())
				.end();

		return flowBuilder.build();
	}
}

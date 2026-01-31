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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SplitConfiguration {

	private final JobRepository jobRepository;

	private final PlatformTransactionManager transactionManager;

	@Bean
	public Step splitStep1() {
		return new StepBuilder("splitStep1", jobRepository)
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
						log.info("myStep was executed");
						return RepeatStatus.FINISHED;
					}
				}, transactionManager).build();
	}

	@Bean
	public Step splitStep2() {
		return new StepBuilder("splitStep2", jobRepository)
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
						log.info("myStep was executed");
						return RepeatStatus.FINISHED;
					}
				}, transactionManager).build();
	}

	@Bean
	public Job splitJob(@Qualifier("foo") Flow foo, @Qualifier("foo") Flow bar) {
		FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("split");

		Flow flow = flowBuilder.split(new SimpleAsyncTaskExecutor())
				.add(foo, bar)
				.end();

		return new JobBuilder("splitJob", jobRepository)
				.start(splitStep1())
				.next(splitStep2())
				.on("COMPLETED").to(flow)
				.end()
				.build();
	}
}

package com.foo.split;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchConfiguration {


	public static class CountingTasklet implements Tasklet {

		@Override
		public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
			System.out.println(String.format("%s has been executed on thread %s",
					chunkContext.getStepContext().getStepName(), Thread.currentThread().getName()));
			return RepeatStatus.FINISHED;
		}
	}

	private final JobRepository jobRepository;

	private final PlatformTransactionManager transactionManager;

	@Bean
	public Tasklet tasklet() {
		return new CountingTasklet();
	}

	@Bean
	public Flow flow1() {
		return new FlowBuilder<Flow>("flow1")
				.start(new StepBuilder("step1")
						.repository(jobRepository)  // Configure JobRepository
						.tasklet(tasklet())  // Configure Tasklet and TransactionManager
						.transactionManager(transactionManager)
						.build())
				.build();
	}

	@Bean
	public Flow flow2() {
		return new FlowBuilder<Flow>("flow2")
				.start(new StepBuilder("step2")
						.repository(jobRepository)
						.transactionManager(transactionManager)
						.tasklet(tasklet())
						.build())
				.next(new StepBuilder("step3")
						.repository(jobRepository)
						.transactionManager(transactionManager)
						.tasklet(tasklet())
						.build())
				.build();
	}

	@Bean
	public Job job() {
		return new JobBuilder("job")
				.repository(jobRepository)
				.start(flow1())
				.split(new SimpleAsyncTaskExecutor())
				.add(flow2())
				.end()
				.build();
	}
}
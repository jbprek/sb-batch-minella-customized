/*
 * Copyright 2016 the original author or authors.
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
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michael Minella
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class JobConfiguration extends DefaultBatchConfigurer implements ApplicationContextAware {

	
	public final JobBuilderFactory jobBuilderFactory;

	
	public final StepBuilderFactory stepBuilderFactory;

	
	public final JobExplorer jobExplorer;

	
	public final JobRepository jobRepository;

	
	public final JobRegistry jobRegistry;

	
	public final JobLauncher jobLauncher;

	private ApplicationContext applicationContext;

	@Bean
	public JobRegistryBeanPostProcessor jobRegistrar() throws Exception {
		JobRegistryBeanPostProcessor registrar = new JobRegistryBeanPostProcessor();

		registrar.setJobRegistry(this.jobRegistry);
		registrar.setBeanFactory(this.applicationContext.getAutowireCapableBeanFactory());
		registrar.afterPropertiesSet();

		return registrar;
	}

	@Bean
	public JobOperator jobOperator() throws Exception {
		SimpleJobOperator simpleJobOperator = new SimpleJobOperator();

		simpleJobOperator.setJobLauncher(this.jobLauncher);
		simpleJobOperator.setJobParametersConverter(new DefaultJobParametersConverter());
		simpleJobOperator.setJobRepository(this.jobRepository);
		simpleJobOperator.setJobExplorer(this.jobExplorer);
		simpleJobOperator.setJobRegistry(this.jobRegistry);

		simpleJobOperator.afterPropertiesSet();

		return simpleJobOperator;
	}

	@Bean
	@StepScope
	public Tasklet tasklet() {
		return (contribution, chunkContext) -> {
			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");

			log.info(
					String.format(">> I was run at %s",
							formatter.format(new Date())));
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Job job() {
		return jobBuilderFactory.get("job")
				.incrementer(new RunIdIncrementer())
				.start(stepBuilderFactory.get("step1")
						.tasklet(tasklet())
						.build())
				.build();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public JobLauncher getJobLauncher() {
		SimpleJobLauncher jobLauncher = null;
		try {
			jobLauncher = new SimpleJobLauncher();
			jobLauncher.setJobRepository(jobRepository);
			jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
			jobLauncher.afterPropertiesSet();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return jobLauncher;
	}
}

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

import java.util.ArrayList;
import java.util.List;

import io.spring.batch.components.CustomRetryableException;
import io.spring.batch.components.RetryItemProcessor;
import io.spring.batch.components.RetryItemWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Michael Minella
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class JobConfiguration implements ApplicationContextAware {

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
	public ListItemReader reader() {

		List<String> items = new ArrayList<>();

		for(int i = 1; i <= 12; i++) {
			items.add(String.valueOf(i));
		}

		ListItemReader<String> reader = new ListItemReader(items);

		return reader;
	}

	@Bean
	@StepScope
	public RetryItemProcessor processor(@Value("#{jobParameters['retry']}")String retry) {
		RetryItemProcessor processor = new RetryItemProcessor();

		processor.setRetry(StringUtils.hasText(retry) && retry.equalsIgnoreCase("processor"));

		return processor;
	}

	@Bean
	@StepScope
	public RetryItemWriter writer(@Value("#{jobParameters['retry']}")String retry) {
		RetryItemWriter writer = new RetryItemWriter();

		writer.setRetry(StringUtils.hasText(retry) && retry.equalsIgnoreCase("writer"));

		return writer;
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step")
				.<String, String>chunk(3)
				.reader(reader())
				.processor(processor(null))
				.writer(writer(null))
				.faultTolerant()
				.retry(CustomRetryableException.class)
				.retryLimit(15)
				.build();
	}

	@Bean
	public Job job() {
		return jobBuilderFactory.get("job")
				.start(step1())
				.build();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}

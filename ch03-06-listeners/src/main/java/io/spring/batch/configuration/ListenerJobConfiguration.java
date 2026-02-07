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

import io.spring.batch.listener.ChunkListener;
import io.spring.batch.listener.JobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Minella
 */
@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ListenerJobConfiguration {

	private final JobRepository jobRepository;

	private final PlatformTransactionManager transactionManager;

	@Bean
	public ItemReader<String> reader() {
		return new ListItemReader<>(Arrays.asList("one", "two", "three"));
	}

	@Bean
	public ItemWriter<String> writer() {
		return new ItemWriter<String>() {
			@Override
			public void write(Chunk<? extends String> items) throws Exception {
				for (String item : items) {
					log.info("Writing item " + item);
				}
			}
		};
	}

	@Bean
	public Step step1() {
		return new StepBuilder("step1", jobRepository)
				.<String, String>chunk(2)
				.faultTolerant()
				.listener(new ChunkListener())
				.reader(reader())
				.writer(writer())
				.build();
	}

	@Bean
	public Job listenerJob(JavaMailSender javaMailSender) {
		return jobBuilderFactory.get("listenerJob")
				.start(step1())
				.listener(new JobListener(javaMailSender))
				.build();
	}
}

package com.foo.transitions;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransitionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransitionsApplication.class, args);
    }
}

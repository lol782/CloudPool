package com.cloudpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Main entry point for the Spring Boot application.
 */

// Marks this class as a Spring Boot application.
@SpringBootApplication

// Enables caching support in the application.
@EnableCaching

// Enables asynchronous method execution.
@EnableAsync

// Enables scheduling support for scheduled tasks.
@EnableScheduling

// Enables method-level security annotations.
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class CloudpoolApplication {

    // Main method used to start the Spring Boot application.
    public static void main(String[] args) {

        // Starts the application and initializes Spring context.
        SpringApplication.run(CloudpoolApplication.class, args);
    }
}
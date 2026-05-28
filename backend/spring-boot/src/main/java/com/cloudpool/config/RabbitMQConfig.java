package com.cloudpool.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String FILE_PROCESSING_QUEUE = "cloudpool.file.processing";
    public static final String EMBEDDING_QUEUE = "cloudpool.embedding";
    public static final String BACKUP_QUEUE = "cloudpool.backup";
    public static final String CLEANUP_QUEUE = "cloudpool.cleanup";

    // Exchange
    public static final String EXCHANGE = "cloudpool.exchange";

    // Routing keys
    public static final String FILE_ROUTING_KEY = "cloudpool.file.*";
    public static final String EMBEDDING_ROUTING_KEY = "cloudpool.embedding.*";
    public static final String BACKUP_ROUTING_KEY = "cloudpool.backup.*";
    public static final String CLEANUP_ROUTING_KEY = "cloudpool.cleanup.*";

    /**
     * Declare exchange
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    /**
     * Declare file processing queue
     */
    @Bean
    public Queue fileProcessingQueue() {
        return QueueBuilder.durable(FILE_PROCESSING_QUEUE)
            .withArgument("x-message-ttl", 3600000) // 1 hour TTL
            .build();
    }

    /**
     * Bind file queue to exchange
     */
    @Bean
    public Binding fileBinding(Queue fileProcessingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(fileProcessingQueue)
            .to(exchange)
            .with(FILE_ROUTING_KEY);
    }

    /**
     * Declare embedding queue
     */
    @Bean
    public Queue embeddingQueue() {
        return QueueBuilder.durable(EMBEDDING_QUEUE)
            .withArgument("x-message-ttl", 3600000)
            .build();
    }

    /**
     * Bind embedding queue
     */
    @Bean
    public Binding embeddingBinding(Queue embeddingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(embeddingQueue)
            .to(exchange)
            .with(EMBEDDING_ROUTING_KEY);
    }

    /**
     * Declare backup queue
     */
    @Bean
    public Queue backupQueue() {
        return QueueBuilder.durable(BACKUP_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours
            .build();
    }

    /**
     * Bind backup queue
     */
    @Bean
    public Binding backupBinding(Queue backupQueue, TopicExchange exchange) {
        return BindingBuilder.bind(backupQueue)
            .to(exchange)
            .with(BACKUP_ROUTING_KEY);
    }

    /**
     * Declare cleanup queue
     */
    @Bean
    public Queue cleanupQueue() {
        return QueueBuilder.durable(CLEANUP_QUEUE)
            .withArgument("x-message-ttl", 86400000)
            .build();
    }

    /**
     * Bind cleanup queue
     */
    @Bean
    public Binding cleanupBinding(Queue cleanupQueue, TopicExchange exchange) {
        return BindingBuilder.bind(cleanupQueue)
            .to(exchange)
            .with(CLEANUP_ROUTING_KEY);
    }
}

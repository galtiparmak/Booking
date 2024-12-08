package com.geko.Booking.KafkaProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.Entity.Mongo.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class LogProducer {
    private static final String TOPIC = "log-topic";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Executor kafkaExecutor = Executors.newFixedThreadPool(10);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public LogProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> create(AuditLog log) {
        return CompletableFuture.runAsync(() -> {
            try {
                String json = objectMapper.writeValueAsString(log);
                kafkaTemplate.send(TOPIC, json);
            } catch (Exception e) {
                System.err.println("Failed to send message to Kafka: " + e.getMessage());
                throw new RuntimeException("Kafka send failed ", e);
            }
        }, kafkaExecutor);
    }
}

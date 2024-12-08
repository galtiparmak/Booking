package com.geko.Booking.KafkaProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.Entity.Mysql.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SearcherProducer {
    private static final String CREATE_TOPIC = "searcher-create-topic";
    private static final String DELETE_TOPIC = "searcher-delete-topic";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public SearcherProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void create(Searcher searcher) {
        try {
            String json = objectMapper.writeValueAsString(searcher);
            kafkaTemplate.send(CREATE_TOPIC, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Searcher searcher) {
        try {
            String json = objectMapper.writeValueAsString(searcher);
            kafkaTemplate.send(DELETE_TOPIC, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

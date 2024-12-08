package com.geko.Booking.KafkaProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.DTO.ListingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ListingNodeProducer {
    private static final String CREATE_TOPIC = "listing-node-create-topic";
    private static final String DELETE_TOPIC = "listing-node-delete-topic";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ListingNodeProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void create(ListingRequest listingRequest) {
        try {
            String json = objectMapper.writeValueAsString(listingRequest);
            kafkaTemplate.send(CREATE_TOPIC, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String listingId) {
        kafkaTemplate.send(DELETE_TOPIC, listingId);
    }
}

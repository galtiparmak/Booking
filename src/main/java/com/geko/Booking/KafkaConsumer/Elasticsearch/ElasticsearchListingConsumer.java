package com.geko.Booking.KafkaConsumer.Elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.DTO.ListingRequest;
import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Repository.Elasticsearch.ListingIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ElasticsearchListingConsumer {
    private static final String CREATE_TOPIC = "listing-create-topic";
    private static final String DELETE_TOPIC = "listing-delete-topic";
    private static final String GROUP = "booking-elasticsearch-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ListingIndexRepository listingIndexRepository;

    @Autowired
    public ElasticsearchListingConsumer(ListingIndexRepository listingIndexRepository) {
        this.listingIndexRepository = listingIndexRepository;
    }

    @KafkaListener(topics = CREATE_TOPIC, groupId = GROUP)
    public void consumeCreation(String listingRequestJson) {
        try {
            ListingRequest listingRequest = objectMapper.readValue(listingRequestJson, ListingRequest.class);
            ListingIndex listingIndex = ListingIndex
                    .builder()
                    .id(listingRequest.getId())
                    .title(listingRequest.getTitle())
                    .description(listingRequest.getDescription())
                    .price(listingRequest.getPrice())
                    .lat(listingRequest.getLat())
                    .lon(listingRequest.getLon())
                    .amenities(listingRequest.getAmenities())
                    .bookedDates(new ArrayList<>())
                    .ownerUsername(listingRequest.getOwnerUsername())
                    .build();
            listingIndexRepository.save(listingIndex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = DELETE_TOPIC, groupId = GROUP)
    public void consumeDeletion(String id) {
        if (listingIndexRepository.existsById(id)) {
            listingIndexRepository.deleteById(id);
        }
        else {
            throw new RuntimeException("Listing Index Not Found With Provided Id!!!");
        }
    }
}

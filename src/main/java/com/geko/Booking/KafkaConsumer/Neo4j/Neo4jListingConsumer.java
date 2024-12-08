package com.geko.Booking.KafkaConsumer.Neo4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.DTO.ListingRequest;
import com.geko.Booking.Entity.Neo4j.ListingNode;
import com.geko.Booking.Repository.Neo4j.ListingNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class Neo4jListingConsumer {
    private final ListingNodeRepository listingNodeRepository;
    private static final String CREATE_TOPIC = "listing-node-create-topic";
    private static final String DELETE_TOPIC = "listing-node-delete-topic";
    private static final String GROUP = "booking-neo4j-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public Neo4jListingConsumer(ListingNodeRepository listingNodeRepository) {
        this.listingNodeRepository = listingNodeRepository;
    }

    @KafkaListener(topics = CREATE_TOPIC, groupId = GROUP)
    public void consumeCreation(String listingRequestJson) {
        try {
            ListingRequest listingRequest = objectMapper.readValue(listingRequestJson, ListingRequest.class);
            ListingNode listingNode = ListingNode
                    .builder()
                    .id(listingRequest.getId())
                    .title(listingRequest.getTitle())
                    .lat(listingRequest.getLat())
                    .lon(listingRequest.getLon())
                    .amenities(listingRequest.getAmenities())
                    .nearbyListings(new ArrayList<>())
                    .similarListings(new ArrayList<>())
                    .ownerUsername(listingRequest.getOwnerUsername())
                    .build();
            listingNodeRepository.save(listingNode);
            setRelations(listingNode.getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = DELETE_TOPIC, groupId = GROUP)
    public void consumeDeletion(String listingId) {
        if (listingNodeRepository.existsById(listingId)) {
            listingNodeRepository.deleteListing(listingId);
        }
    }


    private void setRelations(String listingNode) {
        listingNodeRepository.createNearbyRelationships(listingNode, 100);
        listingNodeRepository.createSimilarAmenitiesRelationships(listingNode);
    }
}

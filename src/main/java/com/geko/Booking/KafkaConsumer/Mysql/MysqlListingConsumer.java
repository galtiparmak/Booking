package com.geko.Booking.KafkaConsumer.Mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.DTO.ListingRequest;
import com.geko.Booking.Entity.Mysql.Homeowner;
import com.geko.Booking.Entity.Mysql.Listing;
import com.geko.Booking.Repository.Mysql.HomeownerRepository;
import com.geko.Booking.Repository.Mysql.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MysqlListingConsumer {
    private static final String CREATE_TOPIC = "listing-create-topic";
    private static final String DELETE_TOPIC = "listing-delete-topic";
    private static final String GROUP = "booking-mysql-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ListingRepository listingRepository;
    private final HomeownerRepository homeownerRepository;

    @Autowired
    public MysqlListingConsumer(ListingRepository listingRepository,
                                HomeownerRepository homeownerRepository) {
        this.listingRepository = listingRepository;
        this.homeownerRepository = homeownerRepository;
    }

    @KafkaListener(topics = CREATE_TOPIC, groupId = GROUP)
    public void consumeCreation(String listingRequestJson) {
        try {
            ListingRequest listingRequest = objectMapper.readValue(listingRequestJson, ListingRequest.class);
            Homeowner homeowner = homeownerRepository.findByUsername(listingRequest.getOwnerUsername()).orElseThrow(
                    () -> new RuntimeException("No Such HomeownerService!")
            );

            Listing listing = Listing
                    .builder()
                    .id(listingRequest.getId())
                    .owner(homeowner)
                    .build();
            listingRepository.save(listing);
            homeowner.getListings().add(listing);
            homeownerRepository.save(homeowner);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = DELETE_TOPIC, groupId = GROUP)
    public void consumeDeletion(String id) {
        if (listingRepository.existsById(id)) {
            listingRepository.deleteById(id);
        }
        else {
            throw new RuntimeException("Listing Not Found With Provided Id!!!");
        }
    }
}

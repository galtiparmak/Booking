package com.geko.Booking.KafkaConsumer.Mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.Repository.Mysql.SearcherRepository;
import com.geko.Booking.Repository.Mysql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MysqlSearcherConsumer {
    private static final String CREATE_TOPIC = "searcher-create-topic";
    private static final String DELETE_TOPIC = "searcher-delete-topic";
    private static final String GROUP = "booking-mysql-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SearcherRepository searcherRepository;

    @Autowired
    public MysqlSearcherConsumer(SearcherRepository searcherRepository) {
        this.searcherRepository = searcherRepository;
    }

    @KafkaListener(topics = CREATE_TOPIC, groupId = GROUP)
    public void consumeCreation(String searcherJson) {
        try {
            Searcher searcher = objectMapper.readValue(searcherJson, Searcher.class);
            searcherRepository.save(searcher);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = DELETE_TOPIC, groupId = GROUP)
    public void consumeDeletion(String username) {
        if (searcherRepository.existsByUsername(username)) {
            searcherRepository.deleteByUsername(username);
        }
        else {
            throw new RuntimeException("No Searcher With Provided Username");
        }
    }
}

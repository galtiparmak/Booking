package com.geko.Booking.KafkaConsumer.Neo4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.Entity.Neo4j.SearcherNode;
import com.geko.Booking.Repository.Neo4j.SearcherNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Neo4jSearcherConsumer {
    private static final String CREATE_TOPIC = "searcher-create-topic";
    private static final String DELETE_TOPIC = "searcher-delete-topic";
    private static final String GROUP = "booking-neo4j-consumer-group";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SearcherNodeRepository searcherNodeRepository;

    @Autowired
    public Neo4jSearcherConsumer(SearcherNodeRepository searcherNodeRepository) {
        this.searcherNodeRepository = searcherNodeRepository;
    }

    @KafkaListener(topics = CREATE_TOPIC, groupId = GROUP)
    public void consumeCreation(String searcherJson) {
        try {
            Searcher searcher = objectMapper.readValue(searcherJson, Searcher.class);
            SearcherNode searcherNode = SearcherNode
                    .builder()
                    .id(String.valueOf(searcher.getId()))
                    .username(searcher.getUsername())
                    .build();
            searcherNodeRepository.save(searcherNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = DELETE_TOPIC, groupId = GROUP)
    public void consumeDeletion(String username) {
        if (searcherNodeRepository.existsByUsername(username)) {
            searcherNodeRepository.deleteByUsername(username);
        }
        else {
            throw new RuntimeException("No Such Searcher Node With Provided Username!!!");
        }
    }
}

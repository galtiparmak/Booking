package com.geko.Booking.Configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    // LOG
    @Bean
    public NewTopic logSaveTopic() {
        return new NewTopic("log-topic", 2, (short) 1);
    }

    // SEARCHER
    @Bean
    public NewTopic searcherCreateTopic() {
        return new NewTopic("searcher-create-topic", 2, (short) 1);
    }
    @Bean
    public NewTopic searcherDeleteTopic() {
        return new NewTopic("searcher-delete-topic", 2, (short) 1);
    }

    // LISTING
    @Bean
    public NewTopic listingCreateTopic() {
        return new NewTopic("listing-create-topic", 2, (short) 1);
    }
    @Bean
    public NewTopic listingDeleteTopic() {
        return new NewTopic("listing-delete-topic", 2, (short) 1);
    }

    // LISTING NODE
    @Bean
    public NewTopic listingNodeCreateTopic() {
        return new NewTopic("listing-node-create-topic", 2, (short) 1);
    }
    @Bean
    public NewTopic listingNodeDeleteTopic() {
        return new NewTopic("listing-node-delete-topic", 2, (short) 1);
    }
}

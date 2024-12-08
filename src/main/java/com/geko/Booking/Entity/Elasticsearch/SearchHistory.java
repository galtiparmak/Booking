package com.geko.Booking.Entity.Elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.*;

import java.util.List;

@Document(indexName = "search_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    @Id
    private String id;
    private String userId;
    private String query;
    private long timestamp;
    private List<String> filters;
    private List<String> results;
}


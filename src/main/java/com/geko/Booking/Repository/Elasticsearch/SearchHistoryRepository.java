package com.geko.Booking.Repository.Elasticsearch;

import com.geko.Booking.Entity.Elasticsearch.SearchHistory;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface SearchHistoryRepository extends ElasticsearchRepository<SearchHistory, String> {
    List<SearchHistory> findByUserId(String userId);
    List<SearchHistory> findByQueryContaining(String keyword);
}


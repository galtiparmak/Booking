package com.geko.Booking.Repository.Elasticsearch;

import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;
import java.util.Optional;

public interface ListingIndexRepository extends ElasticsearchRepository<ListingIndex, String> {
    List<ListingIndex> findByPriceBetween(double minPrice, double maxPrice);
    List<ListingIndex> findByAmenitiesContaining(String amenity);
    List<ListingIndex> findByLatAndLon(double lat, double lon);
    List<ListingIndex> findAll();
    Optional<ListingIndex> findById(String id);
    boolean existsById(String id);
    void deleteById(String id);
    List<ListingIndex> findAllByOwnerUsername(String ownerUsername);
    @Query("{\"from\": ?0, \"size\": ?1}")
    List<ListingIndex> findByFromAndSize(int from, int size);


}


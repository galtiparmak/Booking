package com.geko.Booking.Repository.Mongodb;

import com.geko.Booking.Entity.Mongo.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {
    List<Image> findByListingId(String listingId);

    Optional<Image> findFirstByListingId(String listingId);
}

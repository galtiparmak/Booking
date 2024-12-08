package com.geko.Booking.Repository.Mongodb;

import com.geko.Booking.Entity.Mongo.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByListingId(String listingId);
    List<Review> findByUsername(String username);
}

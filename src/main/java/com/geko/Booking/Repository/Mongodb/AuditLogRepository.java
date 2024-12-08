package com.geko.Booking.Repository.Mongodb;

import com.geko.Booking.Entity.Mongo.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findByListingId(String listingId);
}

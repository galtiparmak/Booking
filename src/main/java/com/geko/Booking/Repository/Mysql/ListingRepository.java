package com.geko.Booking.Repository.Mysql;

import com.geko.Booking.Entity.Mysql.Homeowner;
import com.geko.Booking.Entity.Mysql.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, String> {
    Optional<Listing> findById(String id);
    boolean existsById(String id);

    void deleteById(String id);
}

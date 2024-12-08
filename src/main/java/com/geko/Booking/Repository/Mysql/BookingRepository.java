package com.geko.Booking.Repository.Mysql;

import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Entity.Mysql.Searcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Optional<Booking> findById(String id);
    List<Booking> findBySearcherId(Long searcherId);
    List<Booking> findByHomeownerId(Long homeownerId);
}

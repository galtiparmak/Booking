package com.geko.Booking.Repository.Mysql;

import com.geko.Booking.Entity.Mysql.Homeowner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomeownerRepository extends JpaRepository<Homeowner, Long> {

    Optional<Homeowner> findByUsername(String username);

}

package com.geko.Booking.Repository.Mysql;

import com.geko.Booking.Entity.Mysql.Searcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearcherRepository extends JpaRepository<Searcher, Long> {
    void deleteById(Long id);
    boolean existsById(Long id);

    boolean existsByUsername(String username);
    void deleteByUsername(String username);

    Optional<Searcher> findByUsername(String username);
}

package com.geko.Booking.Entity.Mysql;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking implements Serializable {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "searcher_id", nullable = false)
    private Searcher searcher;

    @ManyToOne
    @JoinColumn(name = "homeowner_id", nullable = false)
    private Homeowner homeowner;

    @Column(nullable = false)
    private String listingId; // Listing reference (from Neo4j or Elasticsearch)

    @Column(nullable = false)
    private LocalDateTime bookingStart;

    @Column(nullable = false)
    private LocalDateTime bookingEnd;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
}

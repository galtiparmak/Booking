package com.geko.Booking.Entity.Elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingIndex {
    @Id
    private String id;
    private String title;
    private String description;
    private double price;
    private double lat;
    private double lon;
    private List<String> amenities;
    private List<BookingInterval> bookedDates;
    private String ownerUsername;

    public void addReservation(String bookingId,LocalDateTime start, LocalDateTime end) {
        BookingInterval bookingInterval = BookingInterval
                .builder()
                .bookingId(bookingId)
                .startDate(start)
                .endDate(end)
                .build();
        this.bookedDates.add(bookingInterval);
    }

    public void removeReservation(String bookingId) {
        this.bookedDates.removeIf(b -> b.getBookingId().equals(bookingId));
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BookingInterval {
    private String bookingId; // Reference to booking
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}



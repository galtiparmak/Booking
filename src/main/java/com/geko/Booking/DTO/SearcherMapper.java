package com.geko.Booking.DTO;

import com.geko.Booking.Entity.Mysql.Searcher;

import java.util.stream.Collectors;

public class SearcherMapper {
    public static SearcherDTO toDTO(Searcher searcher) {
        return SearcherDTO
                .builder()
                .username(searcher.getUsername())
                .email(searcher.getEmail())
                .role(searcher.getRole())
                .bookings(searcher.getBookings().stream()
                        .map(booking -> new BookingDTO(booking.getId(), booking.getListingId()))
                        .collect(Collectors.toList()))
                .build();
    }
}

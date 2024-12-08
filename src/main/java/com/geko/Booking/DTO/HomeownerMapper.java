package com.geko.Booking.DTO;

import com.geko.Booking.Entity.Mysql.Homeowner;

import java.util.stream.Collectors;

public class HomeownerMapper {
    public static HomeownerDTO toDTO(Homeowner homeowner) {
        return HomeownerDTO
                .builder()
                .username(homeowner.getUsername())
                .email(homeowner.getEmail())
                .phone(homeowner.getPhoneNumber())
                .bookings(homeowner.getBookings().stream()
                        .map(booking -> new BookingDTO(booking.getId(), booking.getListingId()))
                        .collect(Collectors.toList()))
                .listings(homeowner.getListings().stream()
                        .map(listing -> new ListingDTO(listing.getId()))
                        .collect(Collectors.toList()))
                .build();
    }
}

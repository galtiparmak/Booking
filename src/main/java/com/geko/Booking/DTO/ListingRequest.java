package com.geko.Booking.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingRequest {
    private String id;
    private String title;
    private String description;
    private double price;
    private double lat;
    private double lon;
    private List<String> amenities;
    private String ownerUsername;
}

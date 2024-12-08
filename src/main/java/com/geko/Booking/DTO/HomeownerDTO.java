package com.geko.Booking.DTO;

import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Entity.Mysql.Listing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeownerDTO {
    private String username;
    private String email;
    private String phone;
    private List<ListingDTO> listings;
    private List<BookingDTO> bookings;
}

package com.geko.Booking.DTO;

import com.geko.Booking.Entity.Mysql.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private String searcherUsername;
    private String listingId;
    private LocalDateTime start;
    private LocalDateTime end;
    private PaymentMethod paymentMethod;
}

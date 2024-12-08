package com.geko.Booking.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {
    private boolean success;
    private String message;
}

package com.geko.Booking.DTO;

import org.springframework.http.ResponseEntity;

public class ResponseMapper {
    public static ResponseEntity<Response> responder(Response response) {
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}

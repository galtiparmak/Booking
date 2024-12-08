package com.geko.Booking.Controller;

import com.geko.Booking.DTO.BookingRequest;
import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.ResponseMapper;
import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/make")
    public ResponseEntity<Response> makeBooking(@RequestBody BookingRequest bookingRequest) {
        Response response = bookingService.makeBooking(bookingRequest);
        return ResponseMapper.responder(response);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Response> cancelBooking(@RequestParam String username,
                                                  @RequestParam Booking booking) {
        Response response = bookingService.cancelBooking(username, booking);
        return ResponseMapper.responder(response);
    }
}

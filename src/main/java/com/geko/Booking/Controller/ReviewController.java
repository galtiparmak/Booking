package com.geko.Booking.Controller;

import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.ResponseMapper;
import com.geko.Booking.DTO.ReviewRequest;
import com.geko.Booking.Entity.Mongo.Review;
import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@PreAuthorize("hasRole('SEARCHER')")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/booking")
    public ResponseEntity<Response> reviewBooking(@RequestBody ReviewRequest reviewRequest) {
        Response response = reviewService.reviewBooking(reviewRequest);
        return ResponseMapper.responder(response);
    }

    @GetMapping("/reviewable-bookings-for-user")
    public ResponseEntity<List<Booking>> reviewableBookingsListForUser(@RequestParam String username) {
        return ResponseEntity.ok(reviewService.reviewableBookingsListForUser(username));
    }

    @GetMapping("/get-reviews-for-listing")
    public ResponseEntity<List<Review>> getReviewsForListing(@RequestParam String listingId) {
        return ResponseEntity.ok(reviewService.getReviewsForListing(listingId));
    }
}

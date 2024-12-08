package com.geko.Booking.Controller;

import com.geko.Booking.DTO.ListingImage;
import com.geko.Booking.Service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/user")
    public ResponseEntity<List<ListingImage>> findRecommendationsForUser(@RequestParam String username) {
        return ResponseEntity.ok(recommendationService.findRecommendationsForUser(username));
    }

    @GetMapping("/listing")
    public ResponseEntity<List<ListingImage>> findRecommendationsForListing(@RequestParam String listingId) {
        return ResponseEntity.ok(recommendationService.findRecommendationsForListing(listingId));
    }

    @PostMapping("/user-viewed")
    public ResponseEntity<Void> viewed(@RequestParam String username,
                                       @RequestParam String listingId) {
        recommendationService.viewed(username, listingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user-liked")
    public ResponseEntity<Void> liked(@RequestParam String username,
                                       @RequestParam String listingId) {
        recommendationService.liked(username, listingId);
        return ResponseEntity.ok().build();
    }
}

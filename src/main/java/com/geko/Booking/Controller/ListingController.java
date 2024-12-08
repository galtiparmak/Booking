package com.geko.Booking.Controller;

import com.geko.Booking.DTO.ListingImage;
import com.geko.Booking.Service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listing")
public class ListingController {
    private final ListingService listingService;

    @Autowired
    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ListingImage>> getListings(@RequestParam int page,
                                                          @RequestParam int size) {
        return ResponseEntity.ok(listingService.getListings(page, size));
    }

    @GetMapping("/homeowners")
    public ResponseEntity<List<ListingImage>> getHomeownersListings(@RequestParam String username) {
        return ResponseEntity.ok(listingService.homeownerListings(username));
    }

    @GetMapping("/searcher-old-booked-listings")
    public ResponseEntity<List<ListingImage>> getSearcherOldBookedListings(@RequestParam String username) {
        return ResponseEntity.ok(listingService.searcherOldBookedListings(username));
    }
}

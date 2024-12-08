package com.geko.Booking.Controller;

import com.geko.Booking.DTO.HomeownerDTO;
import com.geko.Booking.DTO.ListingRequest;
import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.ResponseMapper;
import com.geko.Booking.Service.HomeownerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/homeowner")
@PreAuthorize("hasRole('HOMEOWNER')")
public class HomeownerController {
    private final HomeownerService homeownerService;

    @Autowired
    public HomeownerController(HomeownerService homeownerService) {
        this.homeownerService = homeownerService;
    }

    @GetMapping("/me")
    public ResponseEntity<HomeownerDTO> getInfo(@RequestParam String username) {
        return ResponseEntity.ok(homeownerService.getInfo(username));
    }

    @PutMapping("/updatePhoneNumber")
    public ResponseEntity<Response> updatePhoneNumber(@RequestParam String username,
                                                      @RequestParam String phoneNum) {
        Response response = homeownerService.addPhoneNumber(username, phoneNum);
        return ResponseMapper.responder(response);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<Response> changePassword(@RequestParam String username,
                                                   @RequestParam String oldPassword,
                                                   @RequestParam String newPassword) {
        Response response = homeownerService.changePassword(username, oldPassword, newPassword);
        return ResponseMapper.responder(response);
    }

    @PostMapping("/createListing")
    public ResponseEntity<Response> createListing(@RequestBody ListingRequest listingRequest) {
        Response response = homeownerService.createListing(listingRequest);
        return ResponseMapper.responder(response);
    }

    @DeleteMapping("/deleteListing")
    public ResponseEntity<Response> deleteListing(@RequestParam String homeownerUsername,
                                                  @RequestParam String listingId) {
        Response response = homeownerService.deleteListing(homeownerUsername, listingId);
        return ResponseMapper.responder(response);
    }

}

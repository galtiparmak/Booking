package com.geko.Booking.Controller;

import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.ResponseMapper;
import com.geko.Booking.Entity.Mongo.Image;
import com.geko.Booking.Service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Response> uploadImages(@RequestParam String username,
                                                @RequestParam String listingId,
                                                @RequestParam List<String> paths) {
        Response response = imageService.uploadImages(username, listingId, paths);
        return ResponseMapper.responder(response);
    }

    @GetMapping("/getSingle")
    public ResponseEntity<Image> getImage(@RequestParam String listingId) {
        return ResponseEntity.ok(imageService.getSingleImage(listingId));
    }
}

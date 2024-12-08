package com.geko.Booking.Controller;

import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.ResponseMapper;
import com.geko.Booking.DTO.SearcherDTO;
import com.geko.Booking.Service.SearcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/searcher")
@PreAuthorize("hasRole('SEARCHER')")
public class SearcherController {
    private final SearcherService searcherService;

    @Autowired
    public SearcherController(SearcherService searcherService) {
        this.searcherService = searcherService;
    }

    @GetMapping("/me")
    public ResponseEntity<SearcherDTO> getInfo(@RequestParam String username) {
        return ResponseEntity.ok(searcherService.getInfo(username));
    }

    @PutMapping("/updatePhoneNumber")
    public ResponseEntity<Response> updatePhoneNumber(@RequestParam String username,
                                                      @RequestParam String phoneNum) {
        Response response = searcherService.addPhoneNumber(username, phoneNum);
        return ResponseMapper.responder(response);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<Response> changePassword(@RequestParam String username,
                                                   @RequestParam String oldPassword,
                                                   @RequestParam String newPassword) {
        Response response = searcherService.changePassword(username, oldPassword, newPassword);
        return ResponseMapper.responder(response);
    }
}

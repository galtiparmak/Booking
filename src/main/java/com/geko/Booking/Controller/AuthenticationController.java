package com.geko.Booking.Controller;

import com.geko.Booking.Authentication.AuthenticationRequest;
import com.geko.Booking.Authentication.AuthenticationResponse;
import com.geko.Booking.Authentication.RegisterRequest;
import com.geko.Booking.Service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register-searcher")
    public ResponseEntity<AuthenticationResponse> registerSearcher(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.registerSearcher(request));
    } // http://localhost:8080/api/auth/register-searcher

    @PostMapping("/register-homeowner")
    public ResponseEntity<AuthenticationResponse> registerHomeowner(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.registerHomeowner(request));
    } // http://localhost:8080/api/auth/register-homeowner

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    } // http://localhost:8080/api/auth/authenticate
}

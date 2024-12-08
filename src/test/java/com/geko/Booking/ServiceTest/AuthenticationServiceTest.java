package com.geko.Booking.ServiceTest;

import com.geko.Booking.Authentication.AuthenticationRequest;
import com.geko.Booking.Authentication.AuthenticationResponse;
import com.geko.Booking.Authentication.RegisterRequest;
import com.geko.Booking.Entity.Mysql.Homeowner;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.Entity.Mysql.User;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Mysql.HomeownerRepository;
import com.geko.Booking.Repository.Mysql.UserRepository;
import com.geko.Booking.Service.AuthenticationService;
import com.geko.Booking.Service.JwtService;
import com.geko.Booking.Service.SearcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HomeownerRepository homeownerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SearcherService searcherService;
    @Mock
    private LogProducer logProducer;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerSearcher_success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("searcher1", "searcher1@example.com", "password123");
        Searcher searcher = new Searcher();
        searcher.setUsername(request.getUsername());
        searcher.setEmail(request.getEmail());
        searcher.setPassword("encodedPassword");

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(Searcher.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.registerSearcher(request);

        // Assert
        verify(searcherService, times(1)).create(any(Searcher.class));
        verify(logProducer, times(1)).create(any());
        assertEquals("jwtToken", response.getToken());
    }

    @Test
    void registerHomeowner_success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("homeowner1", "homeowner1@example.com", "password123");
        Homeowner homeowner = new Homeowner();
        homeowner.setUsername(request.getUsername());
        homeowner.setEmail(request.getEmail());
        homeowner.setPassword("encodedPassword");

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(Homeowner.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.registerHomeowner(request);

        // Assert
        verify(homeownerRepository, times(1)).save(any(Homeowner.class));
        verify(logProducer, times(1)).create(any());
        assertEquals("jwtToken", response.getToken());
    }

    @Test
    void login_success() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user1", "password123");
        Searcher user = new Searcher();
        user.setUsername(request.getUsername());
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.login(request);

        // Assert
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(logProducer, times(1)).create(any());
        assertEquals("jwtToken", response.getToken());
    }

    @Test
    void login_userNotFound() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("nonexistentUser", "password123");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.login(request));
        verify(logProducer, never()).create(any());
    }
}


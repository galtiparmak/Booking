package com.geko.Booking.Service;

import com.geko.Booking.Authentication.AuthenticationRequest;
import com.geko.Booking.Authentication.AuthenticationResponse;
import com.geko.Booking.Authentication.RegisterRequest;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mongo.AuditLog;
import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.Entity.Mysql.Homeowner;
import com.geko.Booking.Entity.Mysql.Role;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.Entity.Mysql.User;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.KafkaProducer.SearcherProducer;
import com.geko.Booking.Repository.Mysql.HomeownerRepository;
import com.geko.Booking.Repository.Mysql.SearcherRepository;
import com.geko.Booking.Repository.Mysql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final HomeownerRepository homeownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SearcherService searcherService;
    private final LogProducer logProducer;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 HomeownerRepository homeownerRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager,
                                 SearcherService searcherService,
                                 LogProducer logProducer) {
        this.userRepository = userRepository;
        this.homeownerRepository = homeownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.searcherService = searcherService;
        this.logProducer = logProducer;
    }

    public AuthenticationResponse registerSearcher(RegisterRequest request) {
        Searcher searcher = new Searcher();
        searcher.setUsername(request.getUsername());
        searcher.setEmail(request.getEmail());
        searcher.setPassword(passwordEncoder.encode(request.getPassword()));

        searcherService.create(searcher);
        logProducer.create(LogMapper.createLog(request.getUsername(), Action.SEARCHER_REGISTERED));
        var token = jwtService.generateToken(searcher);

        return AuthenticationResponse
                .builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse registerHomeowner(RegisterRequest request) {
        Homeowner homeowner = new Homeowner();
        homeowner.setUsername(request.getUsername());
        homeowner.setEmail(request.getEmail());
        homeowner.setPassword(passwordEncoder.encode(request.getPassword()));

        homeownerRepository.save(homeowner);
        logProducer.create(LogMapper.createLog(request.getUsername(), Action.HOMEOWNER_REGISTERED));
        var token = jwtService.generateToken(homeowner);

        return AuthenticationResponse
                .builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
        User user = optionalUser.get();
        logProducer.create(LogMapper.createLog(request.getUsername(), Action.LOGIN));
        var token = jwtService.generateToken(user);

        return AuthenticationResponse
                .builder()
                .token(token)
                .build();
    }
}

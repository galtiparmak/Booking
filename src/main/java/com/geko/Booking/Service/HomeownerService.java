package com.geko.Booking.Service;

import com.geko.Booking.DTO.*;
import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.Entity.Mysql.Homeowner;
import com.geko.Booking.Entity.Mysql.Listing;
import com.geko.Booking.KafkaProducer.ListingNodeProducer;
import com.geko.Booking.KafkaProducer.ListingProducer;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Elasticsearch.ListingIndexRepository;
import com.geko.Booking.Repository.Mysql.HomeownerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HomeownerService {
    private final HomeownerRepository homeownerRepository;
    private final LogProducer logProducer;
    private final ListingProducer listingProducer;
    private final ListingNodeProducer listingNodeProducer;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager mysqlTransactionManager;
    @Autowired
    @Qualifier("neo4jTransactionManager")
    private PlatformTransactionManager neo4jTransactionManager;

    private final String REDIS_HOMEOWNER_KEY = "homeowners: ";

    @Autowired
    public HomeownerService(HomeownerRepository homeownerRepository,
                            LogProducer logProducer,
                            ListingProducer listingProducer,
                            ListingNodeProducer listingNodeProducer,
                            PasswordEncoder passwordEncoder,
                            RedisService redisService) {
        this.homeownerRepository = homeownerRepository;
        this.logProducer = logProducer;
        this.listingProducer = listingProducer;
        this.listingNodeProducer = listingNodeProducer;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }


    public HomeownerDTO getInfo(String username) {
        String redisKey = REDIS_HOMEOWNER_KEY + username;
        HomeownerDTO cachedHomeowner = (HomeownerDTO) redisService.getValue(redisKey);

        if (cachedHomeowner != null) {
            return cachedHomeowner;
        }

        Homeowner homeowner = getHomeowner(username);
        redisService.saveValue(redisKey, HomeownerMapper.toDTO(homeowner), Duration.ofMinutes(10));

        return HomeownerMapper.toDTO(homeowner);
    }

    public Response addPhoneNumber(String username, String phone) {
        try {
            String redisKey = REDIS_HOMEOWNER_KEY + username;

            Homeowner homeowner = getHomeowner(username);
            homeowner.setPhoneNumber(phone);
            homeownerRepository.save(homeowner);
            redisService.saveValue(redisKey, HomeownerMapper.toDTO(homeowner), Duration.ofMinutes(10));
            logProducer.create(LogMapper.createLog(username, Action.USER_UPDATED));

            return Response
                    .builder()
                    .success(true)
                    .message("Phone number changed successfully")
                    .build();
        } catch (Exception e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Error " + e.getMessage())
                    .build();
        }
    }

    public Response changePassword(String username, String oldPassword, String newPassword) {
        try {
            Homeowner homeowner = getHomeowner(username);
            if (passwordEncoder.matches(oldPassword, homeowner.getPassword())) {
                homeowner.setPassword(passwordEncoder.encode(newPassword));
                homeownerRepository.save(homeowner);
                logProducer.create(LogMapper.createLog(username, Action.USER_UPDATED));
                return Response
                        .builder()
                        .success(true)
                        .message("Password changed successfully")
                        .build();
            }
            else {
                return Response
                        .builder()
                        .success(false)
                        .message("Provided password does not match with the current password")
                        .build();
            }
        } catch (Exception e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Error " + e.getMessage())
                    .build();
        }
    }

    public Response createListing(ListingRequest request) {
        try {
            request.setId(generateUUID());
            createForMysqlAndElasticsearch(request);
            createForNeo4j(request);
            logProducer.create(LogMapper.createLog(request.getOwnerUsername(), Action.LISTING_CREATED));

            return Response
                    .builder()
                    .success(true)
                    .message("Listing created successfully")
                    .build();
        } catch (RuntimeException e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Error at listing creation")
                    .build();
        }
    }

    @Transactional("transactionManager")
    public void createForMysqlAndElasticsearch(ListingRequest listingRequest) {
        try {
            listingProducer.create(listingRequest);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error at creating listing for mysql and elasticsearch");
        }
    }

    @Transactional("neo4jTransactionManager")
    public void createForNeo4j(ListingRequest listingRequest) {
        try {
            listingNodeProducer.create(listingRequest);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error at creating listing for neo4j");
        }
    }

    public Response deleteListing(String username, String listingId) {
        Homeowner homeowner = getHomeowner(username);
        Listing listing = null;
        for (Listing l : homeowner.getListings()) {
            if (l.getId().equals(listingId)) {
                listing = l;
                break;
            }
        }
        if (listing == null) {
            return Response
                    .builder()
                    .success(false)
                    .message("User has no such listing")
                    .build();
        }

        deleteFromMysqlAndElasticsearch(listingId);
        deleteFromNeo4j(listingId);
        homeowner.getListings().remove(listing);
        logProducer.create(LogMapper.createLog(username, Action.LISTING_DELETED, listingId));

        return Response
                .builder()
                .success(true)
                .message("Listing deleted successfully")
                .build();
    }

    @Transactional("transactionManager")
    public void deleteFromMysqlAndElasticsearch(String listingId) {
        try {
            listingProducer.delete(listingId);
        } catch (RuntimeException e) {
            System.err.println("Error at listing deletion from mysql and elasticsearch " + e.getMessage());
        }
    }

    @Transactional("neo4jTransactionManager")
    public void deleteFromNeo4j(String listingId) {
        try {
            listingNodeProducer.delete(listingId);
        } catch (RuntimeException e) {
            System.err.println("Error at listing deletion from neo4j " + e.getMessage());
        }
    }

    private Homeowner getHomeowner(String username) {
        Optional<Homeowner> optionalHomeowner = homeownerRepository.findByUsername(username);
        if (optionalHomeowner.isEmpty()) {
            throw new RuntimeException("Homeowner with provided username could not found");
        }
        return optionalHomeowner.get();
    }

    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}

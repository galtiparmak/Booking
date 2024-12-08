package com.geko.Booking.Service;

import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.SearcherDTO;
import com.geko.Booking.DTO.SearcherMapper;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mongo.Review;
import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.Entity.Neo4j.SearcherNode;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Mongodb.ReviewRepository;
import com.geko.Booking.Repository.Mysql.SearcherRepository;
import com.geko.Booking.Repository.Neo4j.SearcherNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class SearcherService {
    private final SearcherRepository searcherRepository;
    private final SearcherNodeRepository searcherNodeRepository;
    private final LogProducer logProducer;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager mysqlTransactionManager;
    @Autowired
    @Qualifier("neo4jTransactionManager")
    private PlatformTransactionManager neo4jTransactionManager;

    @Autowired
    public SearcherService(SearcherRepository searcherRepository,
                           SearcherNodeRepository searcherNodeRepository,
                           LogProducer logProducer,
                           PasswordEncoder passwordEncoder,
                           RedisService redisService) {
        this.searcherRepository = searcherRepository;
        this.searcherNodeRepository = searcherNodeRepository;
        this.logProducer = logProducer;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }


    public SearcherDTO getInfo(String username) {
        String redisKey = "searchers: " + username;
        SearcherDTO cachedSearcherDTO = (SearcherDTO) redisService.getValue(username);

        if (cachedSearcherDTO != null) {
            return cachedSearcherDTO;
        }

        Searcher searcher = getSearcher(username);
        redisService.saveValue(redisKey, SearcherMapper.toDTO(searcher), Duration.ofMinutes(10));
        return SearcherMapper.toDTO(searcher);
    }

    public Response addPhoneNumber(String username, String phone) {
        try {
            String redisKey = "searchers: " + username;

            Searcher searcher = getSearcher(username);
            searcher.setPhoneNumber(phone);

            searcherRepository.save(searcher);
            redisService.saveValue(redisKey, searcher, Duration.ofMinutes(10));
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
            Searcher searcher = getSearcher(username);
            if (passwordEncoder.matches(oldPassword, searcher.getPassword())) {
                searcher.setPassword(passwordEncoder.encode(newPassword));
                searcherRepository.save(searcher);
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

    public Response create(Searcher searcher) {
        try {
            createForMysql(searcher);
            createForNeo4j(searcher.getUsername());

            return Response
                    .builder()
                    .success(true)
                    .message("Searcher created successfully")
                    .build();
        } catch (RuntimeException e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Exception at creation " + e.getMessage())
                    .build();
        }
    }

    @Transactional("transactionManager")
    public void createForMysql(Searcher searcher) {
        try {
            searcherRepository.save(searcher);
        }catch (RuntimeException e) {
            System.err.println("Error at mysql searcher creation " + e.getMessage());
        }
    }

    @Transactional("neo4jTransactionManager")
    public void createForNeo4j(String username) {
        try {
            SearcherNode searcherNode = new SearcherNode();
            searcherNode.setUsername(username);
            searcherNode.setBookedListings(new ArrayList<>());
            searcherNode.setLikedListings(new ArrayList<>());
            searcherNode.setViewedListings(new ArrayList<>());
            searcherNodeRepository.save(searcherNode);
        } catch (RuntimeException e) {
            System.err.println("Error at neo4j searcher creation " + e.getMessage());
        }
    }

    private Searcher getSearcher(String username) {
        Optional<Searcher> optionalSearcher = searcherRepository.findByUsername(username);
        if (optionalSearcher.isEmpty()) {
            throw new RuntimeException("No Searcher Found!!!");
        }
        return optionalSearcher.get();
    }
}

package com.geko.Booking.Service;

import com.geko.Booking.DTO.ListingImage;
import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Neo4j.ListingNode;
import com.geko.Booking.Entity.Neo4j.SearcherNode;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Elasticsearch.ListingIndexRepository;
import com.geko.Booking.Repository.Neo4j.ListingNodeRepository;
import com.geko.Booking.Repository.Neo4j.SearcherNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {
    private final ListingNodeRepository listingNodeRepository;
    private final SearcherNodeRepository searcherNodeRepository;
    private final ListingService listingService;
    private final LogProducer logProducer;

    @Autowired
    public RecommendationService(ListingNodeRepository listingNodeRepository,
                                 SearcherNodeRepository searcherNodeRepository,
                                 ListingService listingService,
                                 LogProducer logProducer) {
        this.listingNodeRepository = listingNodeRepository;
        this.searcherNodeRepository = searcherNodeRepository;
        this.listingService = listingService;
        this.logProducer = logProducer;
    }

    public List<ListingImage> findRecommendationsForUser(String username) {
        List<String> list = findRecommendationsForUserHelper(username);
        List<ListingImage> listings = new ArrayList<>();

        for (String id : list) {
            listings.add(listingService.getListing(id));
        }

        return listings;
    }

    public List<ListingImage> findRecommendationsForListing(String listingId) {
        List<String> list = findRecommendationsForListingHelper(listingId);
        List<ListingImage> listings = new ArrayList<>();

        for (String id : list) {
            listings.add(listingService.getListing(id));
        }

        return listings;
    }

    public void viewed(String username, String listingId) {
        SearcherNode searcherNode = getSearcher(username);
        ListingNode listingNode = getListing(listingId);
        searcherNode.getViewedListings().add(listingNode);
        logProducer.create(LogMapper.createLog(username, Action.VIEWED_LISTING, listingId));
    }

    public void liked(String username, String listingId) {
        SearcherNode searcherNode = getSearcher(username);
        ListingNode listingNode = getListing(listingId);
        searcherNode.getLikedListings().add(listingNode);
        logProducer.create(LogMapper.createLog(username, Action.LIKED_LISTING, listingId));
    }

    private SearcherNode getSearcher(String username) {
        Optional<SearcherNode> optionalSearcherNode = searcherNodeRepository.findByUsername(username);
        if(optionalSearcherNode.isEmpty()) {
            throw new RuntimeException("No such user!!!");
        }
        return optionalSearcherNode.get();
    }

    private ListingNode getListing(String id) {
        Optional<ListingNode> optionalListingNode = listingNodeRepository.findById(id);
        if (optionalListingNode.isEmpty()) {
            throw  new RuntimeException("No such listing!!!");
        }
        return optionalListingNode.get();
    }

    private List<String> findRecommendationsForUserHelper(String username) {
        return searcherNodeRepository.findRecommendations(username);
    }

    private List<String> findRecommendationsForListingHelper(String listingId) {
        return listingNodeRepository.getRecommendations(listingId);
    }
}

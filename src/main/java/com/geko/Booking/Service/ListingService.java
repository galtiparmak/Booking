package com.geko.Booking.Service;

import com.geko.Booking.DTO.ListingImage;
import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mongo.Image;
import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Elasticsearch.ListingIndexRepository;
import com.geko.Booking.Repository.Mysql.BookingRepository;
import com.geko.Booking.Repository.Mysql.SearcherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {
    private final ListingIndexRepository listingIndexRepository;
    private final ImageService imageService;
    private final SearcherRepository searcherRepository;
    private final BookingRepository bookingRepository;
    private final LogProducer logProducer;
    private final RedisService redisService;

    @Autowired
    public ListingService(ListingIndexRepository listingIndexRepository,
                          ImageService imageService,
                          SearcherRepository searcherRepository,
                          BookingRepository bookingRepository,
                          LogProducer logProducer,
                          RedisService redisService) {
        this.listingIndexRepository = listingIndexRepository;
        this.imageService = imageService;
        this.searcherRepository = searcherRepository;
        this.bookingRepository = bookingRepository;
        this.logProducer = logProducer;
        this.redisService = redisService;
    }

    public List<ListingImage> getListings(int page, int size) {
        List<ListingImage> list = new ArrayList<>();
        int from = (page - 1) * size;

        List<ListingIndex> listings = listingIndexRepository.findByFromAndSize(from, size);

        for (ListingIndex l : listings) {
            list.add(getListing(l.getId()));
        }

        logProducer.create(LogMapper.createLog("", Action.LISTINGS_UPLOADED));
        return list;
    }

    public ListingImage getListing(String listingId) {
        try {
            String redisKey = "listings: " + listingId;
            ListingImage cachedImage = (ListingImage) redisService.getValue(redisKey);

            if (cachedImage != null) {
                return cachedImage;
            }

            ListingIndex listingIndex = getListingIndex(listingId);
            List<Image> images = imageService.getImagesForListing(listingId);

            cachedImage = ListingImage
                    .builder()
                    .listingIndex(listingIndex)
                    .images(images)
                    .build();

            redisService.saveValue(redisKey, cachedImage, Duration.ofMinutes(60));

            return cachedImage;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<ListingImage> homeownerListings(String username) {
        List<ListingImage> list = new ArrayList<>();
        List<ListingIndex> listings = listingIndexRepository.findAllByOwnerUsername(username);

        for (ListingIndex l : listings) {
            list.add(getListing(l.getId()));
        }

        return list;
    }

    public List<ListingImage> searcherOldBookedListings(String username) {
        List<String> listingIds = searchersOldBookedListingsId(username);
        List<ListingImage> list = new ArrayList<>();

        for (String s : listingIds) {
            list.add(getListing(s));
        }

        return list;
    }

    private ListingIndex getListingIndex(String listingId) {
        Optional<ListingIndex> optionalListingIndex = listingIndexRepository.findById(listingId);
        if (optionalListingIndex.isEmpty()) {
            throw new RuntimeException("No such listing");
        }
        return optionalListingIndex.get();
    }

    private List<String> searchersOldBookedListingsId(String username) {
        Optional<Searcher> optionalSearcher = searcherRepository.findByUsername(username);
        if (optionalSearcher.isEmpty()) {
            throw new RuntimeException("No searcher exists with provided username");
        }
        Searcher searcher = optionalSearcher.get();

        List<Booking> bookings = bookingRepository.findBySearcherId(searcher.getId());
        List<String> ids = new ArrayList<>();

        for (Booking b : bookings) {
            ids.add(b.getListingId());
        }

        return ids;
    }
}

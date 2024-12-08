package com.geko.Booking.Service;

import com.geko.Booking.DTO.Response;
import com.geko.Booking.DTO.ReviewRequest;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.Entity.Mongo.Review;
import com.geko.Booking.Entity.Mysql.Booking;
import com.geko.Booking.Entity.Mysql.Searcher;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Mongodb.ReviewRepository;
import com.geko.Booking.Repository.Mysql.BookingRepository;
import com.geko.Booking.Repository.Mysql.SearcherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final SearcherRepository searcherRepository;
    private final BookingRepository bookingRepository;
    private final LogProducer logProducer;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         SearcherRepository searcherRepository,
                         BookingRepository bookingRepository,
                         LogProducer logProducer) {
        this.reviewRepository = reviewRepository;
        this.searcherRepository = searcherRepository;
        this.bookingRepository = bookingRepository;
        this.logProducer = logProducer;
    }

    public Response reviewBooking(ReviewRequest request) {
        try {
            if (!isReviewable(request.getUsername(), request.getBookingId())) {
                return Response
                        .builder()
                        .success(false)
                        .message("Booking cannot be reviewable")
                        .build();
            }

            if (request.getRating() < 1 || request.getRating() > 5) {
                return Response
                        .builder()
                        .success(false)
                        .message("Rating must be between 1 and 5.")
                        .build();
            }

            Booking booking = getBooking(request.getBookingId());

            Review review = Review
                    .builder()
                    .comment(request.getComment())
                    .listingId(booking.getListingId())
                    .rating(request.getRating())
                    .username(request.getUsername())
                    .build();
            reviewRepository.save(review);
            logProducer.create(LogMapper.createLog(request.getUsername(), Action.REVIEWED, booking.getListingId()));

            return Response
                    .builder()
                    .success(true)
                    .message("Listing reviewed successfully")
                    .build();

        } catch (RuntimeException e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Error occurred " + e.getMessage())
                    .build();
        }
    }

    public boolean isReviewable(String username, String bookingId) {
        if (username == null || bookingId == null) {
            return false;
        }

        Booking booking = getBooking(bookingId);

        boolean bookingEnded = !booking.getBookingEnd().isAfter(LocalDateTime.now());
        boolean withinReviewPeriod = !LocalDateTime.now().isBefore(booking.getBookingEnd().plusDays(1));
        boolean usernameMatches = booking.getSearcher().getUsername().equals(username);

        return bookingEnded && withinReviewPeriod && usernameMatches;
    }


    public List<Booking> reviewableBookingsListForUser(String username) {
        Searcher searcher = getSearcher(username);
        List<Booking> reviewable = new ArrayList<>();
        for (Booking b : searcher.getBookings()) {
            if (isReviewable(username, b.getId())) {
                reviewable.add(b);
            }
        }
        return reviewable;
    }

    public List<Review> getReviewsForListing(String listingId) {
        return reviewRepository.findByListingId(listingId);
    }

    private Searcher getSearcher(String username) {
        Optional<Searcher> optionalSearcher = searcherRepository.findByUsername(username);
        if (optionalSearcher.isEmpty()) {
            throw new RuntimeException("No Searcher Found!!!");
        }
        return optionalSearcher.get();
    }

    private Booking getBooking(String bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new RuntimeException("No such booking found");
        }
        return optionalBooking.get();
    }
}

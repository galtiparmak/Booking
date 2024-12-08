package com.geko.Booking.Service;

import com.geko.Booking.DTO.BookingRequest;
import com.geko.Booking.Entity.Mongo.LogMapper;
import com.geko.Booking.DTO.Response;
import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Entity.Mongo.Action;
import com.geko.Booking.Entity.Mysql.*;
import com.geko.Booking.Entity.Neo4j.ListingNode;
import com.geko.Booking.Entity.Neo4j.SearcherNode;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Elasticsearch.ListingIndexRepository;
import com.geko.Booking.Repository.Mysql.*;
import com.geko.Booking.Repository.Neo4j.ListingNodeRepository;
import com.geko.Booking.Repository.Neo4j.SearcherNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {
    private final SearcherRepository searcherRepository;
    private final HomeownerRepository homeownerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ListingIndexRepository listingIndexRepository;
    private final SearcherNodeRepository searcherNodeRepository;
    private final ListingNodeRepository listingNodeRepository;
    private final LogProducer logProducer;

    @Autowired
    @Qualifier("neo4jTransactionManager")
    private PlatformTransactionManager neo4jTransactionManager;


    @Autowired
    public BookingService(SearcherRepository searcherRepository,
                          HomeownerRepository homeownerRepository,
                          BookingRepository bookingRepository,
                          PaymentRepository paymentRepository,
                          ListingIndexRepository listingIndexRepository,
                          SearcherNodeRepository searcherNodeRepository,
                          ListingNodeRepository listingNodeRepository,
                          LogProducer logProducer) {
        this.searcherRepository = searcherRepository;
        this.homeownerRepository = homeownerRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.listingIndexRepository = listingIndexRepository;
        this.searcherNodeRepository = searcherNodeRepository;
        this.listingNodeRepository = listingNodeRepository;
        this.logProducer = logProducer;
    }

    public Response makeBooking(BookingRequest bookingRequest) {
        try {
            Searcher searcher = getSearcher(bookingRequest.getSearcherUsername());
            ListingIndex listingIndex = getListingIndex(bookingRequest.getListingId());
            Homeowner homeowner = getHomeowner(listingIndex.getOwnerUsername());

            Booking booking = new Booking();
            booking.setId(generateUUID());
            booking.setListingId(bookingRequest.getListingId());
            booking.setSearcher(searcher);
            booking.setHomeowner(homeowner);
            booking.setBookingStart(bookingRequest.getStart());
            booking.setBookingEnd(bookingRequest.getEnd());

            listingIndex.addReservation(booking.getId(), bookingRequest.getStart(), bookingRequest.getEnd());
            homeowner.getBookings().add(booking);
            searcher.getBookings().add(booking);

            Payment payment = Payment
                    .builder()
                    .booking(booking)
                    .paymentDate(LocalDateTime.now())
                    .paymentMethod(bookingRequest.getPaymentMethod())
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .amount(calculateTotalPrice(bookingRequest.getStart(), bookingRequest.getEnd(), listingIndex.getPrice()))
                    .build();

            payment = paymentRepository.save(payment);
            booking.setPayment(payment);
            logProducer.create(LogMapper.createLog(bookingRequest.getSearcherUsername(), Action.PAYMENT_PENDING, bookingRequest.getListingId()));

            bookingRepository.save(booking);
            homeownerRepository.save(homeowner);
            searcherRepository.save(searcher);
            listingIndexRepository.save(listingIndex);

            bookedRelations(bookingRequest.getSearcherUsername(), bookingRequest.getListingId());

            logProducer.create(LogMapper.createLog(bookingRequest.getSearcherUsername(), Action.PAYMENT_COMPLETED, bookingRequest.getListingId()));
            logProducer.create(LogMapper.createLog(bookingRequest.getSearcherUsername(), Action.BOOKED_LISTING, bookingRequest.getListingId()));

            return Response
                    .builder()
                    .success(true)
                    .message("Booking is done successfully")
                    .build();

        } catch (IllegalArgumentException e) {
            return  Response
                    .builder()
                    .success(false)
                    .message("End date must be after start date")
                    .build();
        } catch (RuntimeException e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Error occurred while booking")
                    .build();
        }
    }

    @Transactional("neo4jTransactionManager")
    public void bookedRelations(String searcherUsername, String listingId) {
        SearcherNode searcherNode = getSearcherNode(searcherUsername);
        ListingNode listingNode = getListingNode(listingId);
        searcherNode.getBookedListings().add(listingNode);
    }

    public Response cancelBooking(String username, Booking booking) {
        try {
            if (!username.equals(booking.getSearcher().getUsername())) {
                return Response
                        .builder()
                        .success(false)
                        .message("Not Authorized")
                        .build();
            }

            if (LocalDateTime.now().isAfter(booking.getBookingStart().minusDays(1))) {
                return Response
                        .builder()
                        .success(false)
                        .message("Cancellations can only be made until one day to start date\n" +
                                "No further cancellations are accepted after that.")
                        .build();
            }

            Searcher searcher = booking.getSearcher();
            Homeowner homeowner = booking.getHomeowner();
            ListingIndex listingIndex = getListingIndex(booking.getListingId());

            searcher.getBookings().remove(booking);
            homeowner.getBookings().remove(booking);
            listingIndex.removeReservation(booking.getId());

            paymentRepository.deleteByBooking(booking);
            logProducer.create(LogMapper.createLog(username, Action.PAYMENT_REFUNDED, booking.getId()));

            bookingRepository.delete(booking);
            homeownerRepository.save(homeowner);
            searcherRepository.save(searcher);
            listingIndexRepository.save(listingIndex);

            logProducer.create(LogMapper.createLog(username, Action.CANCELLED_BOOKING, booking.getId()));

            return Response
                    .builder()
                    .success(true)
                    .message("Booking cancelled successfully")
                    .build();
        } catch (RuntimeException e) {
            return Response
                    .builder()
                    .success(false)
                    .message("Error occurred at booking cancellation")
                    .build();
        }
    }

    private Searcher getSearcher(String username) {
        Optional<Searcher> optionalSearcher = searcherRepository.findByUsername(username);
        if (optionalSearcher.isEmpty()) {
            throw new RuntimeException("No Searcher Found!!!");
        }
        return optionalSearcher.get();
    }

    private ListingIndex getListingIndex(String listingId) {
        Optional<ListingIndex> optionalListingIndex = listingIndexRepository.findById(listingId);
        if (optionalListingIndex.isEmpty()) {
            throw new RuntimeException("No Listing Found!!!");
        }
        return optionalListingIndex.get();
    }

    private Homeowner getHomeowner(String username) {
        Optional<Homeowner> optionalHomeowner = homeownerRepository.findByUsername(username);
        if (optionalHomeowner.isEmpty()) {
            throw new RuntimeException("No Homeowner Found!!!");
        }
        return optionalHomeowner.get();
    }

    private ListingNode getListingNode(String listingId) {
        Optional<ListingNode> optionalListingNode = listingNodeRepository.findById(listingId);
        if (optionalListingNode.isEmpty()) {
            throw new RuntimeException("No Listing Found!!!");
        }
        return optionalListingNode.get();
    }

    private SearcherNode getSearcherNode(String username) {
        Optional<SearcherNode> optionalSearcherNode = searcherNodeRepository.findByUsername(username);
        if (optionalSearcherNode.isEmpty()) {
            throw new RuntimeException("No Searcher Found!!!");
        }
        return optionalSearcherNode.get();
    }

    private double calculateTotalPrice(LocalDateTime start, LocalDateTime end, double price) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        int nights = (int) java.time.Period.between(startDate, endDate).getDays();
        if (nights <= 0) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        return nights * price;
    }

    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}

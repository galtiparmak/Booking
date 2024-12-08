package com.geko.Booking.ServiceTest;

import com.geko.Booking.DTO.BookingRequest;
import com.geko.Booking.DTO.Response;
import com.geko.Booking.Entity.Elasticsearch.ListingIndex;
import com.geko.Booking.Entity.Mysql.*;
import com.geko.Booking.KafkaProducer.LogProducer;
import com.geko.Booking.Repository.Elasticsearch.ListingIndexRepository;
import com.geko.Booking.Repository.Mysql.*;
import com.geko.Booking.Repository.Neo4j.ListingNodeRepository;
import com.geko.Booking.Repository.Neo4j.SearcherNodeRepository;
import com.geko.Booking.Service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private SearcherRepository searcherRepository;
    @Mock
    private HomeownerRepository homeownerRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ListingIndexRepository listingIndexRepository;
    @Mock
    private SearcherNodeRepository searcherNodeRepository;
    @Mock
    private ListingNodeRepository listingNodeRepository;
    @Mock
    private LogProducer logProducer;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMakeBooking_Success() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setSearcherUsername("searcher1");
        request.setListingId("listing1");
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(5));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Searcher searcher = new Searcher();
        searcher.setUsername("searcher1");
        Homeowner homeowner = new Homeowner();
        homeowner.setUsername("owner1");
        ListingIndex listingIndex = new ListingIndex();
        listingIndex.setId("listing1");
        listingIndex.setPrice(100.0);

        when(searcherRepository.findByUsername("searcher1")).thenReturn(Optional.of(searcher));
        when(homeownerRepository.findByUsername("owner1")).thenReturn(Optional.of(homeowner));
        when(listingIndexRepository.findById("listing1")).thenReturn(Optional.of(listingIndex));
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        // Act
        Response response = bookingService.makeBooking(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Booking is done successfully", response.getMessage());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(logProducer, times(2)).create(any());
    }

    @Test
    void testMakeBooking_InvalidDates() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setStart(LocalDateTime.now().plusDays(5));
        request.setEnd(LocalDateTime.now().plusDays(1));

        // Act
        Response response = bookingService.makeBooking(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("End date must be after start date", response.getMessage());
        verifyNoInteractions(bookingRepository, logProducer);
    }

    @Test
    void testMakeBooking_SearcherNotFound() {
        // Arrange
        BookingRequest request = new BookingRequest();
        request.setSearcherUsername("nonexistent");

        when(searcherRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> bookingService.makeBooking(request));
        assertEquals("No Searcher Found!!!", exception.getMessage());
    }

    @Test
    void testCancelBooking_Success() {
        // Arrange
        Booking booking = new Booking();
        Searcher searcher = new Searcher();
        searcher.setUsername("searcher1");
        booking.setSearcher(searcher);
        booking.setBookingStart(LocalDateTime.now().plusDays(2));
        booking.setId("booking1");

        when(bookingRepository.findById("booking1")).thenReturn(Optional.of(booking));
        when(listingIndexRepository.findById(anyString())).thenReturn(Optional.of(new ListingIndex()));

        // Act
        Response response = bookingService.cancelBooking("searcher1", booking);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Booking cancelled successfully", response.getMessage());
        verify(bookingRepository, times(1)).delete(booking);
        verify(logProducer, times(2)).create(any());
    }

    @Test
    void testCancelBooking_Unauthorized() {
        // Arrange
        Booking booking = new Booking();
        Searcher searcher = new Searcher();
        searcher.setUsername("searcher2");
        booking.setSearcher(searcher);

        // Act
        Response response = bookingService.cancelBooking("searcher1", booking);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Not Authorized", response.getMessage());
        verifyNoInteractions(bookingRepository, logProducer);
    }
}


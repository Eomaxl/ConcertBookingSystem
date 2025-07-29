package com.concertbooking.system;

import com.concertbooking.model.Booking;
import com.concertbooking.model.Concert;
import com.concertbooking.model.User;
import com.concertbooking.repository.BookingRepository;
import com.concertbooking.repository.ConcertRepository;
import com.concertbooking.repository.impl.BookingRepositoryImpl;
import com.concertbooking.repository.impl.ConcertRepositoryImpl;
import com.concertbooking.service.BookingService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConcertTicketBookingSystem {
    private static volatile ConcertTicketBookingSystem instance;
    private final ConcertRepository concertRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    private ConcertTicketBookingSystem() {
        this.concertRepository = new ConcertRepositoryImpl();
        this.bookingRepository = new BookingRepositoryImpl();
        this.bookingService = new BookingService(bookingRepository, concertRepository);
    }

    public static ConcertTicketBookingSystem getInstance() {
        if (instance == null) {
            synchronized (ConcertTicketBookingSystem.class) {
                if (instance == null) {
                    instance = new ConcertTicketBookingSystem();
                }
            }
        }
        return instance;
    }

    public void addConcert(Concert concert){
        if(concert != null){
            concertRepository.addConcert(concert);
        }
    }

    public List<Concert> searchConcertByArtists(String artist){
        return concertRepository.searchByArtist(artist);
    }

    public List<Concert> searchConcerstByVenue(String venue){
        return concertRepository.searchByVenue(venue);
    }

    public Optional<Concert> searchConcertById(String id){
        return concertRepository.findById(id);
    }

    public Booking bookTickets(String userId, String concertId, List<String> seatIds, User user){
        return bookingService.bookSeat(userId, concertId, seatIds,user);
    }

    public boolean cancelBooking(String bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}

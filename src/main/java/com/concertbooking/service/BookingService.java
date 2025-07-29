package com.concertbooking.service;

import com.concertbooking.exception.SeatNotAvailableException;
import com.concertbooking.model.Booking;
import com.concertbooking.model.Concert;
import com.concertbooking.model.Seat;
import com.concertbooking.model.User;
import com.concertbooking.repository.BookingRepository;
import com.concertbooking.repository.ConcertRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;

    public BookingService(BookingRepository bookingRepository, ConcertRepository concertRepository) {
        this.bookingRepository = bookingRepository;
        this.concertRepository = concertRepository;
    }

    public Booking bookSeat(String userId, String concertId, List<String> seatIds, User user) throws SeatNotAvailableException {
        Concert concert = concertRepository.findById(concertId).orElseThrow(() -> new SeatNotAvailableException("Concert not found with concert id :"+concertId));
        List<Seat> seats = concert.getSeats().stream().filter((obj) -> seatIds.contains(obj.getId())).collect(Collectors.toList());
        if (seats.size() != seatIds.size()) {
            throw new SeatNotAvailableException("one or more seats are not available");
        }
        for(Seat seat: seats){
            if(!seat.book()){
                throw new SeatNotAvailableException("seat"+seat.getSeatNumber()+" is not available");
            }
        }
        double totalPrice = seats.stream().mapToDouble(Seat::getSeatPrice).sum();
        Booking booking = new Booking(UUID.randomUUID().toString(), user, concert, seats, totalPrice);
        booking.confirm();
        bookingRepository.addBooking(booking);
        return booking;
    }

    public boolean cancelBooking(String bookingId){
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.cancel();
                    booking.getSeats().forEach(Seat::release);
                    return true;
                })
                .orElse(false);
    }
}

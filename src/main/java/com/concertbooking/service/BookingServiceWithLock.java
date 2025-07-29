package com.concertbooking.service;

import com.concertbooking.exception.SeatNotAvailableException;
import com.concertbooking.model.*;
import com.concertbooking.repository.BookingRepository;
import com.concertbooking.repository.ConcertRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BookingServiceWithLock {
    private final BookingRepository bookingRepository;
    private final ConcertRepository concertRepository;
    private final ReentrantLock bookingLock = new ReentrantLock();

    public BookingServiceWithLock(BookingRepository bookingRepository, ConcertRepository concertRepository) {
        this.bookingRepository = bookingRepository;
        this.concertRepository = concertRepository;
    }

    public Booking bookSeats(String userId, String concertId, List<Seat> seatIds, User user) throws SeatNotAvailableException {
        bookingLock.lock();
        try {
            Concert concert = concertRepository.findById(concertId).orElseThrow(() -> new NullPointerException("Concert not found"));
            List<Seat> seatsToBook = concert.getSeats().stream().filter((seat -> seatIds.contains(seat.getId()))).collect(Collectors.toList());

            if (seatsToBook.size() != seatIds.size()) {
                throw new SeatNotAvailableException(seatsToBook.size() + " seats are not available");
            }

            for (Seat seat : seatsToBook) {
                if(seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new SeatNotAvailableException("One or more seats are not available");
                }
                seat.setStatus(SeatStatus.BOOKED);
            }

            double totalPrice = seatsToBook.stream().mapToDouble(Seat::getSeatPrice).sum();
            Booking booking = new Booking(UUID.randomUUID().toString(), user, concert, seatsToBook, totalPrice);

            booking.confirm();
            bookingRepository.addBooking(booking);
            return booking;
        } finally {
            bookingLock.unlock();
        }
    }

    public boolean cancelBooking(String bookingId) {
        bookingLock.lock(); // Acquire the lock
        try {
            return bookingRepository.findById(bookingId)
                    .map(booking -> {
                        booking.cancel(); // This now just sets status to CANCELLED
                        booking.getSeats().forEach(seat -> seat.setStatus(SeatStatus.AVAILABLE)); // Set status directly
                        return true;
                    })
                    .orElse(false);
        } finally {
            bookingLock.unlock(); // Release the lock
        }
    }
}

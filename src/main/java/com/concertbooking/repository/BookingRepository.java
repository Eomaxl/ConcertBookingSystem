package com.concertbooking.repository;

import com.concertbooking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    void addBooking(Booking booking);

    Optional<Booking> findById(String id);

    List<Booking> findAll();

    List<Booking> findByUserId(String userId);

    void deleteBooking(String id);
}

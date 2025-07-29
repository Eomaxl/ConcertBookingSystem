package com.concertbooking.repository.impl;

import com.concertbooking.model.Booking;
import com.concertbooking.repository.BookingRepository;

import java.util.*;
import java.util.stream.Collectors;

public class BookingRepositoryImpl implements BookingRepository {
    private final Map<String, Booking> bookings = new HashMap<>();

    @Override
    public void addBooking(Booking booking) {
        if(booking == null){
            throw new IllegalArgumentException("Booking is null");
        }

        if(bookings.containsKey(booking.getId())){
            throw new IllegalArgumentException("Booking already exists");
        }

        bookings.put(booking.getId(), booking);
    }

    @Override
    public Optional<Booking> findById(String id) {
        if(id == null || id.isEmpty()){
            throw new IllegalArgumentException("Booking id is null or empty");
        }
        if(!bookings.containsKey(id)){
            return Optional.empty();
        }
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findAll(){
        return new ArrayList<>(bookings.values());
    }

    @Override
    public List<Booking> findByUserId(String userId) {
        if(userId == null || userId.isEmpty()){
            throw new IllegalArgumentException("Booking id is null or empty");
        }
        return bookings.values().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBooking(String id) {
        if(id == null || id.isEmpty()){
            throw new IllegalArgumentException("Booking id is null or empty");
        }

        if(!bookings.containsKey(id)){
            throw new IllegalArgumentException("Booking does not exist");
        }

        bookings.remove(id);
    }
}

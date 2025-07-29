package com.concertbooking.model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Booking {
    private final String id;
    private final User user;
    private final Concert concert;
    private final List<Seat> seats;
    private final double totalPrice;
    private final AtomicReference<BookingStatus> status;

    public Booking(String id, User user, Concert concert, List<Seat> seats, double totalPrice) {
        this.id = id;
        this.user = user;
        this.concert = concert;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = new AtomicReference<>(BookingStatus.PENDING);
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Concert getConcert() {
        return concert;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public BookingStatus getStatus() {
        return status.get();
    }

    public boolean confirm(){
        return status.compareAndSet(BookingStatus.PENDING, BookingStatus.CONFIRM);
    }

    public boolean cancel(){
        return status.getAndSet(BookingStatus.CANCELLED) != BookingStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.concertbooking.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Seat {
    private final String id;
    private final String seatNumber;
    private final SeatType seatType;
    private final double seatPrice;
    private final AtomicReference<SeatStatus> status;

    public Seat(String id, String seatNumber, SeatType seatType, double seatPrice, AtomicReference<String> status) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.seatPrice = seatPrice;
        this.status = new AtomicReference<>(SeatStatus.AVAILABLE);
    }

    public String getId() {
        return id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public double getSeatPrice() {
        return seatPrice;
    }

    public SeatStatus getStatus() {
        return status.get();
    }

    public boolean book(){
        return status.compareAndSet(SeatStatus.AVAILABLE, SeatStatus.BOOKED);
    }

    public boolean reserve(){
        return status.compareAndSet(SeatStatus.AVAILABLE, SeatStatus.RESERVED);
    }

    public boolean release() {
        return status.compareAndSet(SeatStatus.BOOKED, SeatStatus.RESERVED);
    }

    public boolean unreserve(){
        return status.compareAndSet(SeatStatus.RESERVED, SeatStatus.AVAILABLE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

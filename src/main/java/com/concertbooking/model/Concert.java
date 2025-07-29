package com.concertbooking.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Concert {
    private final String id;
    private final String artistName;
    private final String venue;
    private final LocalDateTime dateTime;
    private final List<Seat> seats;


    public Concert(String id, String artistName, String venue, LocalDateTime dateTime, List<Seat> seats) {
        this.id = id;
        this.artistName = artistName;
        this.venue = venue;
        this.dateTime = dateTime;
        this.seats = seats;
    }

    public String getId() {
        return id;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Concert concert = (Concert) o;
        return Objects.equals(id, concert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

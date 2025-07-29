package com.concertbooking.repository;

import com.concertbooking.model.Concert;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository {
    void addConcert(Concert concert);

    Optional<Concert> findById(String Id);

    List<Concert> findAll();

    List<Concert> searchByArtist(String artist);

    List<Concert> searchByTitle(String title);
}

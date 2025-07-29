package com.concertbooking.repository.impl;

import com.concertbooking.model.Concert;
import com.concertbooking.repository.ConcertRepository;

import java.util.*;
import java.util.stream.Collectors;

public class ConcertRepositoryImpl implements ConcertRepository {

    private final Map<String, Concert> concerts = new HashMap<String, Concert>();

    @Override
    public void addConcert(Concert concert){
        if(concert == null){
            throw new NullPointerException("Concert is null");
        }

        if(concerts.containsKey(concert.getId())){
            throw new IllegalArgumentException("Concert already exists");
        }

        concerts.put(concert.getId(), concert);
    }

    @Override
    public Optional<Concert> findById(String id){
        if(id == null || id.isEmpty()){
            throw new NullPointerException("id is null or empty");
        }
        if(!concerts.containsKey(id)){
            throw new IllegalArgumentException("Concert does not exist");
        }
        return Optional.ofNullable(concerts.get(id));
    }

    @Override
    public List<Concert> findAll(){
        return new ArrayList<Concert> (concerts.values());
    }

    @Override
    public List<Concert> searchByArtist(String artist){
        return concerts.values().stream()
                .filter((concert)-> concert.getArtistName().equals(artist))
                .collect(Collectors.toList());
    }

    @Override
    public List<Concert> searchByVenue(String venue){
        return concerts.values().stream()
                .filter((concert) -> concert.getVenue().equals(venue))
                .collect(Collectors.toList());
    }
}

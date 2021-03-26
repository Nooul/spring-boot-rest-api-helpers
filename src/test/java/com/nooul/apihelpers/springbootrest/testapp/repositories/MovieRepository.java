package com.nooul.apihelpers.springbootrest.testapp.repositories;

import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.testapp.entities.Movie;

import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends BaseRepository<Movie, Long> {
}

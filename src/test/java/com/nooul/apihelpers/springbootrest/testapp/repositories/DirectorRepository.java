package com.nooul.apihelpers.springbootrest.testapp.repositories;

import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.testapp.entities.Director;

import org.springframework.stereotype.Repository;

@Repository
public interface DirectorRepository extends BaseRepository<Director, Long> {
}

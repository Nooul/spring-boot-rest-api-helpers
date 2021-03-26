package com.nooul.apihelpers.springbootrest.testapp.repositories;

import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.testapp.entities.Actor;

import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends BaseRepository<Actor, Long> {
}

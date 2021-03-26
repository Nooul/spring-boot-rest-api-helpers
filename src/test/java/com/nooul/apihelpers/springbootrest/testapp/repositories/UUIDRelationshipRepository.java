package com.nooul.apihelpers.springbootrest.testapp.repositories;

import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.testapp.entities.UUIDRelationship;

import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UUIDRelationshipRepository extends BaseRepository<UUIDRelationship, UUID> {
}

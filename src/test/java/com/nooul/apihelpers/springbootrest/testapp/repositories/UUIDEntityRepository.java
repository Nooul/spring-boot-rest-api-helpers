package com.nooul.apihelpers.springbootrest.testapp.repositories;

import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.testapp.entities.UUIDEntity;

import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UUIDEntityRepository extends BaseRepository<UUIDEntity, UUID> {
}

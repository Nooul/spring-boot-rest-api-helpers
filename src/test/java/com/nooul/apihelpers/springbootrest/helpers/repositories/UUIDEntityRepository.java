package com.nooul.apihelpers.springbootrest.helpers.repositories;

import com.nooul.apihelpers.springbootrest.helpers.entities.UUIDEntity;
import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;

import java.util.UUID;

public interface UUIDEntityRepository extends BaseRepository<UUIDEntity, UUID> {
}

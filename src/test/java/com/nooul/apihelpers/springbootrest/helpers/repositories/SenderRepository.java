package com.nooul.apihelpers.springbootrest.helpers.repositories;

import com.nooul.apihelpers.springbootrest.helpers.entities.Sender;
import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;

import java.util.UUID;

public interface SenderRepository extends BaseRepository<Sender, UUID> {
}

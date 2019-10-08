package springboot.rest.helpers.repositories;

import springboot.rest.helpers.entities.UUIDRelationship;
import springboot.rest.repositories.BaseRepository;

import java.util.UUID;

public interface UUIDRelationshipRepository extends BaseRepository<UUIDRelationship, UUID> {
}

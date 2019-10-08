package springboot.rest.helpers.repositories;

import springboot.rest.helpers.entities.UUIDEntity;
import springboot.rest.repositories.BaseRepository;

import java.util.UUID;

public interface UUIDEntityRepository extends BaseRepository<UUIDEntity, UUID> {
}

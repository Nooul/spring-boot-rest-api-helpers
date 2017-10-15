package reactAdmin.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;

@NoRepositoryBean
public interface BaseRepository<T> extends PagingAndSortingRepository<T, Integer> {
    Page<T> findByIdIn(Collection<Integer> ids, Pageable pageable);
    Page<T> findAll(Specification<T> spec, Pageable pageable);
}

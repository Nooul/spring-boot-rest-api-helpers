package reactAdmin.rest.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactAdmin.rest.entities.FilterWrapper;
import reactAdmin.rest.repositories.BaseRepository;
import reactAdmin.rest.specifications.ReactAdminSpecifications;
import reactAdmin.rest.utils.ApiUtils;

@Service
public class FilterService<T> {
    @Autowired
    protected ApiUtils utils;

    @Autowired
    private ReactAdminSpecifications<T> specification;

    public Iterable<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T> repo) {
        return utils.filterByHelper(repo, specification, filterWrapper);
    }
}

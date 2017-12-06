package reactAdmin.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import reactAdmin.rest.entities.FilterWrapper;
import reactAdmin.rest.repositories.BaseRepository;
import reactAdmin.rest.specifications.ReactAdminSpecifications;
import reactAdmin.rest.utils.ApiUtils;

public abstract class BaseController<T> {

    @Autowired
    protected ApiUtils utils;

    @Autowired
    private ReactAdminSpecifications<T> specification;

    public Iterable<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T> repo) {
        return utils.filterByHelper(repo, specification, filterWrapper);
    }
}

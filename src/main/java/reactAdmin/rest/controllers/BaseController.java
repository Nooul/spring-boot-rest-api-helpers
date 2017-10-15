package reactAdmin.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import reactAdmin.rest.repositories.BaseRepository;
import reactAdmin.rest.specifications.ReactAdminSpecifications;
import reactAdmin.rest.utils.ApiUtils;

public abstract class BaseController<T> {

    @Autowired
    private ApiUtils utils;

    @Autowired
    private ReactAdminSpecifications<T> specification;

    public Iterable<T> filterBy(String filterStr, String rangeStr, String sortStr, BaseRepository<T> repo) {
        return utils.filterByHelper(repo, specification, filterStr, rangeStr, sortStr);
    }
}

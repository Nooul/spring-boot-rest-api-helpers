package reactAdmin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import reactAdmin.repositories.BaseRepository;
import reactAdmin.specifications.ReactAdminSpecifications;
import reactAdmin.utils.ApiUtils;

public abstract class BaseController<T> {

    @Autowired
    private ApiUtils utils;

    @Autowired
    private ReactAdminSpecifications<T> specification;

    public Iterable<T> filterBy(String filterStr, String rangeStr, String sortStr, BaseRepository<T> repo) {
        return utils.filterByHelper(repo, specification, filterStr, rangeStr, sortStr);
    }
}

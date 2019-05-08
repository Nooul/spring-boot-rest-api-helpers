package reactAdmin.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactAdmin.rest.entities.FilterWrapper;
import reactAdmin.rest.helpers.entities.Movie;
import reactAdmin.rest.helpers.entities.UUID;
import reactAdmin.rest.helpers.repositories.MovieRepository;
import reactAdmin.rest.helpers.repositories.UuidRepository;
import reactAdmin.rest.services.FilterService;

import java.util.Arrays;

@RestController
@RequestMapping("uuids")
public class UuidController {

    @Autowired
    private UuidRepository repository;

    @Autowired
    FilterService<UUID, String> filterService;

    @GetMapping
    public Iterable<UUID> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        FilterWrapper wrapper = filterService.extractFilterWrapper(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, "uuid", Arrays.asList("uuid"));
    }
}

package springboot.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.rest.entities.QueryParamWrapper;
import springboot.rest.helpers.entities.UUID;
import springboot.rest.helpers.repositories.UuidRepository;
import springboot.rest.services.FilterService;
import springboot.rest.utils.QueryParamExtractor;

import java.util.Arrays;

@RestController
@RequestMapping("uuids")
public class UuidController {

    @Autowired
    private UuidRepository repository;

    @Autowired
    private FilterService<UUID, String> filterService;

    @GetMapping
    public Iterable<UUID> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        QueryParamWrapper wrapper = QueryParamExtractor.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, "uuid", Arrays.asList("uuid"));
    }
}

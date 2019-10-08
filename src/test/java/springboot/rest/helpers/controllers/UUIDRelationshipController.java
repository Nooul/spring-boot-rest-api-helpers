package springboot.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.rest.entities.FilterWrapper;
import springboot.rest.helpers.entities.UUIDRelationship;
import springboot.rest.helpers.repositories.UUIDRelationshipRepository;
import springboot.rest.services.FilterService;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("uuidrelationship")
public class UUIDRelationshipController {
    @Autowired
    private UUIDRelationshipRepository repository;

    @Autowired
    FilterService<UUIDRelationship, UUID> filterService;

    @GetMapping
    public Iterable<UUIDRelationship> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        FilterWrapper wrapper = filterService.extractFilterWrapper(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, "uuid", Arrays.asList("uuid"));
    }
}

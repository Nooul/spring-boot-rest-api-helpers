package springboot.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.rest.entities.FilterWrapper;
import springboot.rest.helpers.entities.Actor;
import springboot.rest.helpers.repositories.ActorRepository;
import springboot.rest.services.FilterService;

import java.util.Arrays;

@RestController
@RequestMapping("actors")
public class ActorController {


    @Autowired
    private ActorRepository repository;

    @Autowired
    FilterService<Actor, Long> filterService;

    @GetMapping
    public Iterable<Actor> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        FilterWrapper wrapper = filterService.extractFilterWrapper(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("firstName", "lastName"));
    }
}

package springboot.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.rest.entities.QueryParamWrapper;
import springboot.rest.helpers.entities.Actor;
import springboot.rest.helpers.repositories.ActorRepository;
import springboot.rest.services.FilterService;
import springboot.rest.utils.QueryParamExtracter;

import java.util.Arrays;

@RestController
@RequestMapping("actors")
public class ActorController {


    @Autowired
    private ActorRepository repository;

    @Autowired
    private FilterService<Actor, Long> filterService;


    @GetMapping
    public Iterable<Actor> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        QueryParamWrapper wrapper = QueryParamExtracter.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("firstName", "lastName"));
    }
}

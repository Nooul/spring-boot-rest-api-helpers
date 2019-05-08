package springboot.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.rest.entities.FilterWrapper;
import springboot.rest.helpers.entities.Movie;
import springboot.rest.helpers.repositories.MovieRepository;
import springboot.rest.services.FilterService;

import java.util.Arrays;

@RestController
@RequestMapping("movies")
public class MovieController {


    @Autowired
    private MovieRepository repository;

    @Autowired
    FilterService<Movie, Long> filterService;

    @GetMapping
    public Iterable<Movie> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        FilterWrapper wrapper = filterService.extractFilterWrapper(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("name"));
    }
}


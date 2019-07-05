package springboot.rest.helpers.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.rest.entities.QueryParamWrapper;
import springboot.rest.helpers.entities.Category;
import springboot.rest.helpers.entities.Movie;
import springboot.rest.helpers.repositories.MovieRepository;
import springboot.rest.services.FilterService;
import springboot.rest.utils.QueryParamExtracter;

import java.util.Arrays;

@RestController
@RequestMapping("movies")
public class MovieController {


    @Autowired
    private MovieRepository repository;

    @Autowired
    private FilterService<Movie, Long> filterService;

    @Autowired
    private QueryParamExtracter queryParamExtracter;

    @GetMapping
    public Iterable<Movie> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name="sort") String sortStr) {
        QueryParamWrapper wrapper = queryParamExtracter.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("name"));
    }
}


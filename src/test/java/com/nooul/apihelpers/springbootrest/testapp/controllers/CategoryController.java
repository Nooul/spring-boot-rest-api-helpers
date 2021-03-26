package com.nooul.apihelpers.springbootrest.testapp.controllers;

import com.nooul.apihelpers.springbootrest.entities.QueryParamWrapper;
import com.nooul.apihelpers.springbootrest.services.FilterService;
import com.nooul.apihelpers.springbootrest.testapp.entities.Category;
import com.nooul.apihelpers.springbootrest.testapp.repositories.CategoryRepository;
import com.nooul.apihelpers.springbootrest.utils.QueryParamExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("categories")
public class CategoryController {

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private FilterService<Category, Long> filterService;

    @GetMapping
    public Iterable<Category> filterBy(@RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr,
            @RequestParam(required = false, name = "sort") String sortStr) {
        QueryParamWrapper wrapper = QueryParamExtractor.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, Arrays.asList("name"));
    }
}

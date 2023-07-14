package com.nooul.apihelpers.springbootrest.helpers.controllers;

import com.nooul.apihelpers.springbootrest.entities.QueryParamWrapper;
import com.nooul.apihelpers.springbootrest.helpers.entities.Sender;
import com.nooul.apihelpers.springbootrest.helpers.repositories.SenderRepository;
import com.nooul.apihelpers.springbootrest.services.FilterService;
import com.nooul.apihelpers.springbootrest.utils.QueryParamExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("senders")
public class SenderController {
    @Autowired
    private SenderRepository repository;

    @Autowired
    FilterService<Sender, UUID> filterService;

    @GetMapping
    public Iterable<Sender> filterBy(
            @RequestParam(required = false, name = "filter") String filterStr,
            @RequestParam(required = false, name = "range") String rangeStr, @RequestParam(required = false, name = "sort") String sortStr) {
        QueryParamWrapper wrapper = QueryParamExtractor.extract(filterStr, rangeStr, sortStr);
        return filterService.filterBy(wrapper, repository, "id");
    }
}

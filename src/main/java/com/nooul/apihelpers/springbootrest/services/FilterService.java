package com.nooul.apihelpers.springbootrest.services;


import com.nooul.apihelpers.springbootrest.entities.QueryParamWrapper;
import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.specifications.CustomSpecifications;
import org.apache.commons.text.CaseUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
//from: https://github.com/Nooul/spring-boot-rest-api-helpers/blob/master/src/main/java/com/nooul/apihelpers/springbootrest/services/FilterService.java
public class FilterService<T, I extends Serializable> {

    @Autowired
    private Environment env;

    @Autowired
    private CustomSpecifications<T> specifications;


    public long countBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo) {
        JSONObject filter = queryParamWrapper.getFilter();
        JSONArray filterOr = queryParamWrapper.getFilterOr();
        String usesSnakeCase = env.getProperty("spring-boot-rest-api-helpers.use-snake-case");
        if (filter != null && filter.length() > 0) {
            HashMap<String, Object> map = (HashMap<String, Object>) filter.toMap();

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                map = convertToCamelCase(map);
            }

            return repo.count(
                    specifications.build(map));

        } else if (filterOr != null && filterOr.length() > 0) {
            List list = filterOr.toList();
            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                //map = convertToCamelCase(map); TODO for list
            }
            return repo.count(specifications.build(list));

        } else {
            return repo.count();
        }
    }

    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo) {
        return filterByHelper(repo, specifications, queryParamWrapper, "id", new ArrayList<>());
    }

    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo, String primaryKeyName) {

        return filterByHelper(repo, specifications, queryParamWrapper, primaryKeyName, new ArrayList<>());
    }

    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo, String primaryKeyName, List<String> searchOnlyInFields) {

        return filterByHelper(repo, specifications, queryParamWrapper, primaryKeyName, searchOnlyInFields);
    }

    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo, List<String> searchOnlyInFields) {

        return filterByHelper(repo, specifications, queryParamWrapper, "id", searchOnlyInFields);
    }

    private List<Sort.Order> sortHelper(JSONArray sort, String primaryKeyName) {

        List<Sort.Order> sortOrders = new ArrayList<>();
        String usesSnakeCase = env.getProperty("spring-boot-rest-api-helpers.use-snake-case");
        if (sort.length() % 2 != 0) {
            throw new IllegalArgumentException("sort should have even length given as array e.g ['name', 'ASC', 'birthDate', 'DESC']");
        }
        for (int i = 0; i < sort.length(); i = i + 2) {
            String sortBy;
            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                sortBy = convertToCamelCase((String) sort.get(i));
            } else {
                sortBy = (String) sort.get(i);
            }

            sortOrders.add(new Sort.Order(Sort.Direction.valueOf((String) sort.get(i + 1)), sortBy));
        }
        if (sortOrders.isEmpty()) {
            sortOrders.add(new Sort.Order(Sort.Direction.ASC, primaryKeyName));
        }

        return sortOrders;
    }

    private <T> Page<T> filterByHelper(BaseRepository<T, I> repo,
                                       CustomSpecifications<T> specifications,
                                       QueryParamWrapper queryParamWrapper,
                                       String primaryKeyName,
                                       List<String> searchOnlyInFields) {
        String usesSnakeCase = env.getProperty("spring-boot-rest-api-helpers.use-snake-case");

        Sort sortObj;
        JSONObject filter = queryParamWrapper.getFilter();
        JSONArray filterOr = queryParamWrapper.getFilterOr();
        JSONArray range = queryParamWrapper.getRange();
        JSONArray sort = queryParamWrapper.getSort();

        int page = 0;
        int size = Integer.MAX_VALUE;
        if (range.length() == 2) {
            page = (Integer) range.get(0);
            size = (Integer) range.get(1);
        }

        sortObj = Sort.by(sortHelper(sort, primaryKeyName));
        Page result;
        if (filter != null && filter.length() > 0) {
            HashMap<String, Object> map = (HashMap<String, Object>) filter.toMap();

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                map = convertToCamelCase(map);
            }


            result = repo.findAll(specifications.build(map, searchOnlyInFields), PageRequest.of(page, size, sortObj));

        } else if (filterOr != null && filterOr.length() > 0) {

            List list = filterOr.toList();
            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                //map = convertToCamelCase(map); TODO for list
            }

            result = repo.findAll(
                    specifications.build(list)
                    , PageRequest.of(page, size, sortObj));

        } else {
            result = repo.findAll(PageRequest.of(page, size, sortObj));
        }
        return result;
    }

    private HashMap<String, Object> convertToCamelCase(HashMap<String, Object> snakeCaseMap) {
        HashMap<String, Object> camelCaseMap = new HashMap<>();
        for (String key : snakeCaseMap.keySet()) {
            Object val = snakeCaseMap.get(key);
            camelCaseMap.put(convertToCamelCase(key), val);
        }
        return camelCaseMap;
    }

    public String convertToCamelCase(String snakeCaseStr) {
        return CaseUtils.toCamelCase(snakeCaseStr,false, new char[]{'_'});
    }
}
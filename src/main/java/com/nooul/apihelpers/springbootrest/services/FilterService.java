/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.services;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.nooul.apihelpers.springbootrest.entities.QueryParamWrapper;
import com.nooul.apihelpers.springbootrest.repositories.BaseRepository;
import com.nooul.apihelpers.springbootrest.specifications.CustomSpecifications;
import com.nooul.apihelpers.springbootrest.utils.JSONUtils;
import com.nooul.apihelpers.springbootrest.utils.StringUtils;

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
import java.util.Map;

@Service
public class FilterService<T, I extends Serializable> {

    @Autowired
    private Environment env;

    @Autowired
    private CustomSpecifications<T> specifications;

    /**
     * Filter a repository
     * 
     * @param queryParamWrapper
     * @param repo
     * @return
     */
    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo) {
        return filterByHelper(repo, specifications, queryParamWrapper, "id", new ArrayList<>());
    }

    /**
     * Filter a repository
     * 
     * @param queryParamWrapper
     * @param repo
     * @param primaryKeyName
     * @return
     */
    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo, String primaryKeyName) {
        return filterByHelper(repo, specifications, queryParamWrapper, primaryKeyName, new ArrayList<>());
    }

    /**
     * Filter a repository
     * 
     * @param queryParamWrapper
     * @param repo
     * @param primaryKeyName
     * @param searchOnlyInFields
     * @return
     */
    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo, String primaryKeyName,
            List<String> searchOnlyInFields) {
        return filterByHelper(repo, specifications, queryParamWrapper, primaryKeyName, searchOnlyInFields);
    }

    /**
     * Filter a repository
     * 
     * @param queryParamWrapper
     * @param repo
     * @param searchOnlyInFields
     * @return
     */
    public Page<T> filterBy(QueryParamWrapper queryParamWrapper, BaseRepository<T, I> repo,
            List<String> searchOnlyInFields) {
        return filterByHelper(repo, specifications, queryParamWrapper, "id", searchOnlyInFields);
    }

    /**
     * Filter a repository
     * 
     * @param repo
     * @param specifications
     * @param queryParamWrapper
     * @param primaryKeyName
     * @param searchOnlyInFields
     * @return
     */
    private Page<T> filterByHelper(BaseRepository<T, I> repo, CustomSpecifications<T> specifications,
            QueryParamWrapper queryParamWrapper, String primaryKeyName, List<String> searchOnlyInFields) {
        boolean useSnakeCase = Boolean.parseBoolean(env.getProperty("spring-boot-rest-api-helpers.use-snake-case"));

        // get range
        JSONArray range = queryParamWrapper.getRange();
        int page = 0;
        int size = Integer.MAX_VALUE;
        if (range.length() == 2) {
            page = (Integer) range.get(0);
            size = (Integer) range.get(1);
        }

        // sort items
        Sort sort = Sort.by(sortHelper(queryParamWrapper.getSort(), primaryKeyName));

        // filters items
        JSONObject filter = queryParamWrapper.getFilter();
        JSONArray filterOr = queryParamWrapper.getFilterOr();
        Page<T> result;
        if (filter != null && filter.length() > 0) {
            // simple filter
            result = repo.findAll((Specification<T>) (root, query, builder) -> {
                HashMap<String, Object> map = JSONUtils.toMap(filter);
                return specifications.customSpecificationBuilder(builder, query, root, map, searchOnlyInFields,
                        useSnakeCase);
            }, PageRequest.of(page, size, sort));

        } else if (filterOr != null && filterOr.length() > 0) {
            // or filter
            result = repo.findAll((Specification<T>) (root, query, builder) -> {
                List<Object> list = JSONUtils.toList(filterOr);
                List<Map<String, Object>> listofmap = new ArrayList<Map<String, Object>>();
                for (Object element : list) {
                    Map<String, Object> elementhash = JSONUtils.toMap((JSONObject) element);
                    listofmap.add(elementhash);
                }
                return specifications.customSpecificationBuilder(builder, query, root, listofmap, searchOnlyInFields,
                        useSnakeCase);
            }, PageRequest.of(page, size, sort));
        } else {
            // return all
            result = repo.findAll(PageRequest.of(page, size, sort));
        }
        return result;
    }

    /**
     * Sort a repository
     * 
     * @param sort
     * @param primaryKeyName
     * @return
     */
    private List<Sort.Order> sortHelper(JSONArray sort, String primaryKeyName) {
        boolean useSnakeCase = Boolean.parseBoolean(env.getProperty("spring-boot-rest-api-helpers.use-snake-case"));

        // validate sort order
        List<Sort.Order> sortOrders = new ArrayList<>();
        if (sort.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "sort should have even length given as array e.g ['name', 'ASC', 'birthDate', 'DESC']");
        }

        // sort items
        for (int i = 0; i < sort.length(); i = i + 2) {
            String sortBy;
            if (useSnakeCase)
                sortBy = StringUtils.toCamelCase((String) sort.get(i));
            else
                sortBy = (String) sort.get(i);
            sortOrders.add(new Sort.Order(Sort.Direction.valueOf((String) sort.get(i + 1)), sortBy));
        }
        if (sortOrders.isEmpty()) {
            sortOrders.add(new Sort.Order(Sort.Direction.ASC, primaryKeyName));
        }

        return sortOrders;
    }

    /**
     * Convert a string to camel case
     * 
     * @param s
     * @return
     */
    public String toCamelCase(String s) {
        boolean useSnakeCase = Boolean.parseBoolean(env.getProperty("spring-boot-rest-api-helpers.use-snake-case"));

        if (useSnakeCase)
            return StringUtils.toCamelCase(s);
        else
            return s;
    }

}
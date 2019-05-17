package springboot.rest.services;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import springboot.rest.entities.FilterWrapper;
import springboot.rest.repositories.BaseRepository;
import springboot.rest.specifications.CustomSpecifications;
import springboot.rest.utils.JSON;

import java.io.Serializable;
import java.util.*;

@Service
//from: https://github.com/zifnab87/spring-boot-rest-api-helpers/blob/master/src/main/java/springboot/rest/services/FilterService.java
public class FilterService<T,I extends Serializable> {

    @Autowired
    private Environment env;

    @Autowired
    private CustomSpecifications<T> specifications;


    public FilterWrapper extractFilterWrapper(String filterStr, String rangeStr, String sortStr) {
        Object filterJsonOrArray;
        if (StringUtils.isBlank(filterStr)) {
            filterStr = "{}";
        }
        filterJsonOrArray = new JSONTokener(filterStr).nextValue();
        JSONObject filter = null;
        JSONArray filterOr = null;
        if (filterJsonOrArray instanceof JSONObject) {
            filter = JSON.toJsonObject(filterStr);
        }
        else if (filterJsonOrArray instanceof JSONArray){
            filterOr = JSON.toJsonArray(filterStr);
        }
        JSONArray range;
        if (StringUtils.isBlank(rangeStr)) {
            rangeStr = "[]";
        }
        range = JSON.toJsonArray(rangeStr);

        JSONArray sort;
        if (StringUtils.isBlank(sortStr)) {
            sortStr = "[]";
        }
        sort = JSON.toJsonArray(sortStr);


        return new FilterWrapper(filter, filterOr, range, sort);
    }

    public long countBy(FilterWrapper filterWrapper, BaseRepository<T,I> repo) {
        JSONObject filter = filterWrapper.getFilter();
        JSONArray filterOr = filterWrapper.getFilterOr();
        String usesSnakeCase = env.getProperty("spring-boot-rest-api-helpers.use-snake-case");
        if (filter != null && filter.length() > 0) {
            HashMap<String,Object> map = (HashMap<String,Object>) filter.toMap();

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                map = convertToCamelCase(map);
            }

            return repo.count(Specification.where(
                    specifications.customSpecificationBuilder(map)));

        }
        else if (filterOr != null && filterOr.length() > 0) {
            List list = filterOr.toList();

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                //map = convertToCamelCase(map); TODO for list
            }

            return repo.count(Specification.where(
                    specifications.customSpecificationBuilder(list)));

        }
        else {
            return repo.count();
        }
    }

    public Page<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T,I> repo) {
        return filterByHelper(repo, specifications, filterWrapper, "id", new ArrayList<>());
    }

    public Page<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T, I> repo, String primaryKeyName) {

        return filterByHelper(repo, specifications, filterWrapper, primaryKeyName, new ArrayList<>());
    }

    public Page<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T, I> repo, String primaryKeyName, List<String> searchOnlyInFields) {

        return filterByHelper(repo, specifications, filterWrapper, primaryKeyName, searchOnlyInFields);
    }

    public Page<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T, I> repo, List<String> searchOnlyInFields) {

        return filterByHelper(repo, specifications, filterWrapper, "id", searchOnlyInFields);
    }


    private <T> Page<T> filterByHelper(BaseRepository<T, I> repo,
                                       CustomSpecifications<T> specifications,
                                       FilterWrapper filterWrapper,
                                       String primaryKeyName,
                                       List<String> searchOnlyInFields) {
        String usesSnakeCase = env.getProperty("spring-boot-rest-api-helpers.use-snake-case");

        String sortBy = primaryKeyName;
        String order = "DESC";
        JSONObject filter = filterWrapper.getFilter();
        JSONArray filterOr = filterWrapper.getFilterOr();
        JSONArray range = filterWrapper.getRange();
        JSONArray sort = filterWrapper.getSort();

        int page = 0;
        int size = Integer.MAX_VALUE;
        if (range.length() == 2) {
            page = (Integer) range.get(0);
            size = (Integer) range.get(1);
        }



        if (sort.length() == 2) {
            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                sortBy = convertToCamelCase((String) sort.get(0));
            }
            else {
                sortBy = (String) sort.get(0);
            }
            order = (String) sort.get(1);
        }

        Sort.Direction sortDir = Sort.Direction.DESC;
        if (order.equalsIgnoreCase("ASC")) {
            sortDir = Sort.Direction.ASC;
        }

        if (filter != null && filter.length() > 0) {
            HashMap<String,Object> map = (HashMap<String,Object>) filter.toMap();

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                map = convertToCamelCase(map);
            }

            return repo.findAll(Specification.where(
                    specifications.customSpecificationBuilder(
                            map, searchOnlyInFields)
            ), PageRequest.of(page,size, sortDir, sortBy));

        }
        else if (filterOr != null && filterOr.length() > 0) {
            List list = filterOr.toList();

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                //map = convertToCamelCase(map); TODO for list
            }

            return repo.findAll(Specification.where(
                    specifications.customSpecificationBuilder(list)
            ), PageRequest.of(page,size, sortDir, sortBy));

        }
        else {
            return repo.findAll(PageRequest.of(page, size, sortDir, sortBy));
        }
    }

    private HashMap<String, Object> convertToCamelCase(HashMap<String, Object> snakeCaseMap) {
        Set<String> keys = snakeCaseMap.keySet();
        HashMap<String, Object> camelCaseMap = new HashMap<>(snakeCaseMap);
        for (String key: keys) {
            Object val = snakeCaseMap.get(key);
            camelCaseMap.put(convertToCamelCase(key), val);
        }
        return camelCaseMap;
    }

    private String convertToCamelCase(String snakeCaseStr) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, snakeCaseStr);
    }
}
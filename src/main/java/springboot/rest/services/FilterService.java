package springboot.rest.services;

import com.google.common.base.CaseFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import springboot.rest.entities.QueryParamWrapper;
import springboot.rest.repositories.BaseRepository;
import springboot.rest.specifications.CustomSpecifications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
//from: https://github.com/zifnab87/spring-boot-rest-api-helpers/blob/master/src/main/java/springboot/rest/services/FilterService.java
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
                    specifications.customSpecificationBuilder(map));

        } else if (filterOr != null && filterOr.length() > 0) {

            return repo.count((Specification<T>) (root, query, builder) -> {
                List list = filterOr.toList();
                if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                    //map = convertToCamelCase(map); TODO for list
                }
                return specifications.customSpecificationBuilder(builder, query, root, list);
            });

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
            result = repo.findAll(
                    (Specification<T>) (root, query, builder) -> {

                        HashMap<String, Object> map = (HashMap<String, Object>) filter.toMap();

                        if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                            map = convertToCamelCase(map);
                        }

                        return specifications.customSpecificationBuilder(builder, query, root,
                                map, searchOnlyInFields
                        );
                    }, PageRequest.of(page, size, sortObj));

        } else if (filterOr != null && filterOr.length() > 0) {
            result = repo.findAll(
                    (Specification<T>) (root, query, builder) -> {
                        List list = filterOr.toList();
                        if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                            //map = convertToCamelCase(map); TODO for list
                        }
                        return specifications.customSpecificationBuilder(builder, query, root, list);
                    }
                    , PageRequest.of(page, size, sortObj));

        } else {
            result = repo.findAll(PageRequest.of(page, size, sortObj));
        }
        return result;
    }

    private HashMap<String, Object> convertToCamelCase(HashMap<String, Object> snakeCaseMap) {
        Set<String> keys = snakeCaseMap.keySet();
        HashMap<String, Object> camelCaseMap = new HashMap<>(snakeCaseMap);
        for (String key : keys) {
            Object val = snakeCaseMap.get(key);
            camelCaseMap.put(convertToCamelCase(key), val);
        }
        return camelCaseMap;
    }

    private String convertToCamelCase(String snakeCaseStr) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, snakeCaseStr);
    }
}
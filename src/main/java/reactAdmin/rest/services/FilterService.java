package reactAdmin.rest.services;

import com.google.common.base.CaseFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import reactAdmin.rest.entities.FilterWrapper;
import reactAdmin.rest.repositories.BaseRepository;
import reactAdmin.rest.specifications.ReactAdminSpecifications;
import reactAdmin.rest.utils.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class FilterService<T> {

    @Autowired
    private Environment env;

    @Autowired
    private ReactAdminSpecifications<T> specifications;


    public FilterWrapper extractFilterWrapper(String filterStr, String rangeStr, String sortStr) {
        JSONObject filter = null;
        if (filterStr != null) {
            filter = JSON.toJsonObject(filterStr);
        }
        JSONArray range = null;
        if (rangeStr != null) {
            range = JSON.toJsonArray(rangeStr);
        }

        JSONArray sort = null;
        if (sortStr != null) {
            sort = JSON.toJsonArray(sortStr);
        }

        return new FilterWrapper(filter, range, sort);
    }

    public Iterable<T> filterBy(FilterWrapper filterWrapper, BaseRepository<T> repo) {
        return filterByHelper(repo, specifications, filterWrapper);
    }

    private <T> Page<T> filterByHelper(BaseRepository<T> repo, ReactAdminSpecifications<T> specifications, FilterWrapper filterWrapper) {
        String usesSnakeCase = env.getProperty("react-admin-api.use-snake-case");

        String sortBy = "id";
        String order = "DESC";
        JSONObject filter = filterWrapper.filter;
        JSONArray range = filterWrapper.range;
        JSONArray sort = filterWrapper.sort;

        int page = 0;
        int size = Integer.MAX_VALUE;
        if (range != null) {
            page = (Integer) range.get(0) - 1;
            size = (Integer) range.get(1);
        }



        if (range != null) {
            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                sortBy = convertToCamelCase((String) sort.get(0));
            }
            else {
                sortBy = (String) sort.get(0);
            }
            order = (String) sort.get(1);
        }

        Sort.Direction sortDir = Sort.Direction.DESC;
        if (order.equals("ASC")) {
            sortDir = Sort.Direction.ASC;
        }

        if (filter == null || filter.length() == 0) {
            return repo.findAll(new PageRequest(page, size, sortDir, sortBy));
        }
        else if (filter.has("id")) {
            List idsList = (ArrayList) filter.toMap().get("id");
            return repo.findByIdIn(makeListsInteger(idsList), new PageRequest(page, size, sortDir, sortBy));
        }
        else {
            boolean containsQ = false;
            String text = "";
            if (filter.has("q")) {
                text = (String) filter.get("q");
                containsQ = true;
            }

            HashMap<String,Object> map = (HashMap<String,Object>) filter.toMap();

            if (containsQ) {
                map.remove("q");
            }

            if (usesSnakeCase != null && usesSnakeCase.equals("true")) {
                map = convertToCamelCase(map);
            }
            if (map.isEmpty() && containsQ) {
                return repo.findAll(Specifications.where(specifications.seachInAllAttributes(text)), new PageRequest(page,size, sortDir, sortBy));
            }
            else if(!map.isEmpty() && !containsQ) {
                return repo.findAll(Specifications.where(specifications.equalToEachColumn(map)), new PageRequest(page,size, sortDir, sortBy));
            }
            else if(!map.isEmpty() && containsQ) {
                return repo.findAll(Specifications.where(specifications.equalToEachColumn(map)).and(specifications.seachInAllAttributes(text)), new PageRequest(page,size, sortDir, sortBy));
            }
            else {
                return repo.findAll(new PageRequest(page,size, sortDir, sortBy));
            }
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

    private List<Integer> makeListsInteger(List list) {
        if (!list.isEmpty() && list.get(0) instanceof Integer) {
            return (List<Integer>) list;
        }
        else if (!list.isEmpty() && list.get(0) instanceof String) {
            List<Integer> intList = new ArrayList<>();
            for (Object o : list) {
                intList.add(Integer.parseInt((String)o));
            }
            return intList;
        }
        else {
            return new ArrayList<>();
        }
    }
}

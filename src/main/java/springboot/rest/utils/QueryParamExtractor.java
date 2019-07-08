package springboot.rest.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import springboot.rest.entities.QueryParamWrapper;

public class QueryParamExtractor {

    public static QueryParamWrapper extract(String filterStr, String rangeStr, String sortStr) {
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


        return new QueryParamWrapper(filter, filterOr, range, sort);
    }
}

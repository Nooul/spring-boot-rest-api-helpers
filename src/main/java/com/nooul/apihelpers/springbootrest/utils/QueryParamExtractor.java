package com.nooul.apihelpers.springbootrest.utils;

import com.nooul.apihelpers.springbootrest.entities.QueryParamWrapper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class QueryParamExtractor {

    public static QueryParamWrapper extract(String filterStr, String rangeStr, String sortStr) {



        Object filterJsonOrArray;
        if (StringUtils.isBlank(filterStr)) {
            filterStr = "{}";
        }

        //https://stackoverflow.com/a/18368345
        filterStr = filterStr.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        filterStr = filterStr.replaceAll("\\+", "%2B");
        try {
            //https://stackoverflow.com/a/6926987/986160
            filterStr = URLDecoder.decode(filterStr.replace("+", "%2B"), "UTF-8")
                    .replace("%2B", "+");
        } catch (UnsupportedEncodingException e) {
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

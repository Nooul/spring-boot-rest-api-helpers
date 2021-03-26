/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.utils;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONTokener;
import com.nooul.apihelpers.springbootrest.entities.QueryParamWrapper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Query parameters extractor
 */
public class QueryParamExtractor {

    /**
     * Extract query parameters
     * 
     * @param filterStr
     * @param rangeStr
     * @param sortStr
     * @return
     */
    public static QueryParamWrapper extract(String filterStr, String rangeStr, String sortStr) {

        // validate filter string
        if (StringUtils.isBlank(filterStr)) {
            filterStr = "{}";
        }
        // decode filter string
        filterStr = filterStr.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        filterStr = filterStr.replaceAll("\\+", "%2B");
        try {
            filterStr = URLDecoder.decode(filterStr.replace("+", "%2B"), "UTF-8").replace("%2B", "+");
        } catch (UnsupportedEncodingException e) {
        }
        // tokenize filter string
        Object filterTokenized = new JSONTokener(filterStr).nextValue();
        JSONObject filter = null;
        JSONArray filterOr = null;
        if (filterTokenized instanceof JSONObject) {
            filter = JSONUtils.toJsonObject(filterStr);
        } else if (filterTokenized instanceof JSONArray) {
            filterOr = JSONUtils.toJsonArray(filterStr);
        }

        // get range
        JSONArray range;
        if (StringUtils.isBlank(rangeStr)) {
            rangeStr = "[]";
        }
        range = JSONUtils.toJsonArray(rangeStr);

        // get sort
        JSONArray sort;
        if (StringUtils.isBlank(sortStr)) {
            sortStr = "[]";
        }
        sort = JSONUtils.toJsonArray(sortStr);

        return new QueryParamWrapper(filter, filterOr, range, sort);
    }
}
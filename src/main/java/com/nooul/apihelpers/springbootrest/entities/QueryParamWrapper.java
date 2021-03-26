/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.entities;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

public class QueryParamWrapper {

    /**
     * Filter
     */
    private final JSONObject filter;

    /**
     * Or filter
     */
    private final JSONArray filterOr;

    /**
     * Range
     */
    private final JSONArray range;

    /**
     * Sort
     */
    private final JSONArray sort;

    /**
     * Get filter
     * 
     * @return
     */
    public JSONObject getFilter() {
        return this.filter;
    }

    /**
     * Get or filter
     * 
     * @return
     */
    public JSONArray getFilterOr() {
        return this.filterOr;
    }

    /**
     * Get range
     * 
     * @return
     */
    public JSONArray getRange() {
        return this.range;
    }

    /**
     * Get sort
     * 
     * @return
     */
    public JSONArray getSort() {
        return this.sort;
    }

    /**
     * Constructor
     * 
     * @param filter
     * @param filterOr
     * @param range
     * @param sort
     */
    public QueryParamWrapper(JSONObject filter, JSONArray filterOr, JSONArray range, JSONArray sort) {
        this.filter = filter;
        this.filterOr = filterOr;
        this.range = range;
        this.sort = sort;
    }

}

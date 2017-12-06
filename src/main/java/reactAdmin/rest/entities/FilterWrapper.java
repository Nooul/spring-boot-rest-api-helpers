package reactAdmin.rest.entities;

import org.json.JSONArray;
import org.json.JSONObject;

public class FilterWrapper {
    public JSONArray sort;
    public JSONObject filter;
    public JSONArray range;

    public FilterWrapper(JSONObject filter, JSONArray range, JSONArray sort) {
        this.sort = sort;
        this.filter = filter;
        this.range = range;
    }
}

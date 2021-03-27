/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * JSON utilities
 */
public class JSONUtils {

    /**
     * Convert a string to a JSONObject
     * 
     * @param s
     * @return
     */
    public static JSONObject toJsonObject(String s) {
        JSONObject jsonObj = new JSONObject(s);
        return jsonObj;
    }

    /**
     * Convert a tring to a JSON Array
     * 
     * @param s
     * @return
     */
    public static JSONArray toJsonArray(String s) {
        JSONArray jsonArr = new JSONArray(s);
        return jsonArr;
    }

    /**
     * Convert a JSONOjbect to map
     * 
     * @param o
     * @return
     */
    public static HashMap<String, Object> toMap(JSONObject o) {
        HashMap<String, Object> ret = new HashMap<String, Object>();
        Iterator<String> keysiterator = o.keys();
        while (keysiterator.hasNext()) {
            String key = keysiterator.next();
            Object val = o.get(key);
            if (val instanceof JSONArray) {
                ArrayList<Object> listdata = new ArrayList<Object>();
                for (int i = 0; i < ((JSONArray) val).length(); i++) {
                    Object sumelem = ((JSONArray) val).get(i);
                    if (sumelem instanceof JSONObject)
                        listdata.add(toMap((JSONObject) ((JSONArray) val).get(i)));
                    else
                        listdata.add(((JSONArray) val).get(i));
                }

                ret.put(key, listdata);
            } else if (val instanceof JSONObject) {
                HashMap<String, Object> mapdata = toMap((JSONObject) val);
                ret.put(key, mapdata);
            } else {
                boolean isnull = false;
                if (o.get(key).getClass().getSimpleName().toLowerCase().equals("string")
                        || o.get(key).getClass().getSimpleName().toLowerCase().equals("")) {
                    String valObj = val.toString();
                    isnull = StringUtils.isBlank(valObj) || valObj.equalsIgnoreCase("null");
                } else {
                    isnull = val == null;
                }
                if (isnull)
                    ret.put(key, null);
                else
                    ret.put(key, o.get(key));

            }
        }
        return ret;
    }

    /**
     * Convert a JSONArray to list
     * 
     * @param a
     * @return
     */
    public static List<Object> toList(JSONArray a) {
        List<Object> ret = new ArrayList<Object>();
        a.iterator().forEachRemaining(ret::add);
        return ret;
    }

}
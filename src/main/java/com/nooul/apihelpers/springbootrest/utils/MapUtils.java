/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.utils;

import java.util.HashMap;
import java.util.Set;

/**
 * String utilities
 */
public class MapUtils {

    /**
     * Convert a map to camer case
     * 
     * @param map
     * @return
     */
    public static HashMap<String, Object> toCamelCase(HashMap<String, Object> map) {
        Set<String> keys = map.keySet();
        HashMap<String, Object> camelCaseMap = new HashMap<>(map);
        for (String key : keys) {
            Object val = map.get(key);
            camelCaseMap.put(StringUtils.toCamelCase(key), val);
        }
        return camelCaseMap;
    }
}

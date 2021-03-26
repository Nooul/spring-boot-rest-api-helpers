/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Url utilities
 */
public class UrlUtils {

    /**
     * Decode a URI
     * 
     * @param s
     * @return
     */
    public static String decodeURI(String s) {
        if (s == null)
            return null;

        String ret = null;
        try {
            ret = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            ret = s;
        }

        return ret;
    }

    /**
     * Encode a URI
     * 
     * @param s
     * @return
     */
    public static String encodeURI(String s) {
        String ret = null;
        try {
            ret = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            ret = s;
        }
        return ret;
    }
}
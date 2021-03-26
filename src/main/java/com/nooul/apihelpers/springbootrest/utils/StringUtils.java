/*
 * https://github.com/Nooul/spring-boot-rest-api-helpers
 * 
 * Released under the MIT License.
 * Please refer to LICENSE file for licensing information.
 */

package com.nooul.apihelpers.springbootrest.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * String utilities
 */
public class StringUtils {

    /**
     * Convert a string to camel case
     * 
     * @param str
     * @param capitalizeFirstLetter
     * @param delimiters
     * @return
     */
    public static String toCamelCase(String str, final boolean capitalizeFirstLetter, final char... delimiters) {
        if (str.isEmpty()) {
            return str;
        }
        str = str.toLowerCase();
        final int strLen = str.length();
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        final Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        boolean capitalizeNext = false;
        if (capitalizeFirstLetter) {
            capitalizeNext = true;
        }
        for (int index = 0; index < strLen;) {
            final int codePoint = str.codePointAt(index);

            if (delimiterSet.contains(codePoint)) {
                capitalizeNext = outOffset != 0;
                index += Character.charCount(codePoint);
            } else if (capitalizeNext || outOffset == 0 && capitalizeFirstLetter) {
                final int titleCaseCodePoint = Character.toTitleCase(codePoint);
                newCodePoints[outOffset++] = titleCaseCodePoint;
                index += Character.charCount(titleCaseCodePoint);
                capitalizeNext = false;
            } else {
                newCodePoints[outOffset++] = codePoint;
                index += Character.charCount(codePoint);
            }
        }
        if (outOffset != 0) {
            return new String(newCodePoints, 0, outOffset);
        }
        return str;
    }

    /**
     * Convert a string to camel case
     * 
     * @param snakeCaseStr
     * @return
     */
    public static String toCamelCase(String snakeCaseStr) {
        return StringUtils.toCamelCase(snakeCaseStr, false, new char[] { '_' });
    }

    /**
     * Converts an array of delimiters to a hash set of code points
     * 
     * @param delimiters
     * @return
     */
    private static Set<Integer> generateDelimiterSet(final char[] delimiters) {
        final Set<Integer> delimiterHashSet = new HashSet<>();
        delimiterHashSet.add(Character.codePointAt(new char[] { ' ' }, 0));
        if (delimiters != null && delimiters.length == 0) {
            return delimiterHashSet;
        }

        for (int index = 0; index < delimiters.length; index++) {
            delimiterHashSet.add(Character.codePointAt(delimiters, index));
        }
        return delimiterHashSet;
    }

    /**
     * Check if a string is blank
     * 
     * @param cs
     * @return
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen = cs != null ? cs.length() : 0;
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}

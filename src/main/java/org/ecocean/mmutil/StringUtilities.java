/*
 * This file is a part of Wildbook.
 * Copyright (C) 2015 WildMe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wildbook.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ecocean.mmutil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * StringUtilities provides helpful static utility functions dealing
 * (mostly) with strings.
 */
public class StringUtilities
{
    /**
     * Return true if the given String is null or "".
     */
    public static boolean isEmpty (String s)
    {
        return null == s || "".equals(s);
    }

    /**
     * Return true if the given String is null or "".
     */
    public static boolean isEmptyOrZero (String s)
    {
        return null == s || "".equals(s) || "0".equals(s);
    }

    /**
     * Return true if the given collection is null or has no elements.
     */
    public static boolean isEmpty (Collection c)
    {
        return null == c || c.size() < 1;
    }

    /**
     * Return true if the given map is null or has no elements.
     */
    public static boolean isEmpty (Map m)
    {
        return null == m || m.size() < 1;

    }

    /**
     * Return true if the given array is null or has no elements.
     */
    public static boolean isEmpty (Object[] a)
    {
        return null == a || a.length < 1;
    }

    /**
     * Return true if the given array is null or has no elements.
     */
    public static boolean isEmpty (int[] a)
    {
        return null == a || a.length < 1;
    }

    /**
     * Return true if the given array is null or has no elements.
     */
    public static boolean isEmpty (float[] a)
    {
        return null == a || a.length < 1;
    }

    /**
     * Return true if the given array is null or has no elements.
     */
    public static boolean isEmpty (double[] a)
    {
        return null == a || a.length < 1;
    }

    /**
     * Compare two strings, returning true if they are both equal or
     * both null.
     */
    public static boolean equals (String a, String b)
    {
        return StringUtilities.compareTo (a, b) == 0;
    }

    /**
     * Compare two strings.  They are considered equal if both are
     * null.  Null sorts after any non-null value.
     */
    public static int compareTo (String a, String b)
    {
        if (a == null && b == null) return 0;

        if (a == null)
        {
            if (b == null)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else if (b == null)
        {
            return -1;
        }

        return a.compareTo (b);
    }

    /**
     *  The amount two floating point numbers can differ and still
     *  be considered equal.  Used in roughlyEqual().
     */
    static final double EPSILON = .01;

    /**
     * Return true if the two floats are about the same.
     *
     * @param a First value.
     * @param b Second value.
     */
    public static boolean roughlyEqual (float a, float b)
    {
        return Math.abs (a - b) < EPSILON;
    }

    /**
     * Return true if the two doubles are about the same.
     *
     * @param a First value.
     * @param b Second value.
     */
    public static boolean roughlyEqual (double a, double b)
    {
        return Math.abs (a - b) < EPSILON;
    }

    /**
     * Split a string on newline markers ("\n"), returning an array
     * of resulting Strings.
     **/
    public static String[] splitLines(String str)
    {
        String mark = "\\n";
        ArrayList<String> result = new ArrayList<String>();

        int i = 0;
        while ((i = str.indexOf (mark)) != -1)
        {
            result.add (str.substring (0, i));
            str = str.substring (i + mark.length ());
        }
        result.add (str);

        return result.toArray (new String[result.size ()]);
    }

    public static String getHashOf(final String hashMe) {
        return DigestUtils.md5Hex(hashMe);
    }

    public static String getHashOfCommaList(String hashMe) {
        String returnString = null;

        StringTokenizer tokenizer = new StringTokenizer(hashMe, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim().toLowerCase();
            if (!token.equals("")) {
                String md5 = DigestUtils.md5Hex(token);
                if (returnString == null) {
                    returnString = md5;
                } else {
                    returnString += "," + md5;
                }
            }
        }

        return returnString;
    }
}

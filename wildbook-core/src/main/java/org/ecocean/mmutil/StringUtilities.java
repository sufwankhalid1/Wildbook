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
import java.util.StringTokenizer;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * StringUtilities provides helpful static utility functions dealing
 * (mostly) with strings.
 */
public class StringUtilities
{
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

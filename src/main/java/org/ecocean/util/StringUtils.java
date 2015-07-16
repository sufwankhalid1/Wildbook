package org.ecocean.util;

import java.util.StringTokenizer;

import org.apache.commons.codec.digest.DigestUtils;

public class StringUtils {
    private StringUtils()
    {
        // prevent instantiation
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

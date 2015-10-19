package org.ecocean.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;

public class WildbookUtils {
    private WildbookUtils() {
        //prevent instantiation
    }

    public static String hashAndSaltPassword(final String clearTextPassword, final String salt) {
        return new Sha512Hash(clearTextPassword, salt, 200000).toHex();
    }

    public static ByteSource getSalt() {
        return new SecureRandomNumberGenerator().nextBytes();
    }

    public static <T, U> List<U> convertList(final List<T> from, final Function<T, U> func){
        return from.stream().map(func).collect(Collectors.toList());
    }
}

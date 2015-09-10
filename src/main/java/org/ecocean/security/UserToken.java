package org.ecocean.security;

import org.apache.shiro.authc.UsernamePasswordToken;

public class UserToken {
    private final User user;
    private final UsernamePasswordToken token;

    public UserToken(final User user, final UsernamePasswordToken token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public UsernamePasswordToken getToken() {
        return token;
    }

    public void clear() {
        token.clear();
    }
}

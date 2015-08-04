package org.ecocean.rest;

public class UserInfo {
    final SimpleUser user;

    public UserInfo(final SimpleUser user)
    {
        this.user = user;
    }

    public SimpleUser getUser()
    {
        return user;
    }
}

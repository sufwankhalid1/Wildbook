package org.ecocean.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SecurityInfo {
    private final User user;
    private final Map<String, Set<String>> contextRoles = new HashMap<>();

    public SecurityInfo(final User user) {
        this.user = user;
    }

    public Set<String> getContextRoleKeys() {
        return contextRoles.keySet();
    }

    public Set<String> getContextRoles(final String context) {
        return contextRoles.get(context);
    }

    public void setContextRoles(final String context, final Set<String> roles) {
        contextRoles.put(context, roles);
    }

    public User getUser() {
        return user;
    }
}

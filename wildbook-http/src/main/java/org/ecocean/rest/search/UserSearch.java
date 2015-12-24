package org.ecocean.rest.search;

import org.apache.commons.lang3.StringUtils;

public class UserSearch {
    public String name;

    public boolean hasData() {
        return !StringUtils.isBlank(name);
    }
}

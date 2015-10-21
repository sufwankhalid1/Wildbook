package org.ecocean.html;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class HtmlConfig {
    public HtmlNavBar navbar;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

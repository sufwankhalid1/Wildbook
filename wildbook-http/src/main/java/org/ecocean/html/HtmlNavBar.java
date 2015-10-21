package org.ecocean.html;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class HtmlNavBar {
    public List<HtmlMenu> menus;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

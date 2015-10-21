package org.ecocean.html;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class HtmlMenu {
    public String name;
    public String url;
    public String target;
    public String role;
    public boolean login;
    public String type;
    public List<HtmlMenu> submenus;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
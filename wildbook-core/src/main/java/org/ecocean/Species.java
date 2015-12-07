package org.ecocean;

public class Species {
    private String code;
    private String name;
    private String icon;

    public Species() {
        // deserialization
    }

    public Species(final String code, final String name, final String icon) {
        this.code = code;
        this.name = name;

        if (icon != null) {
            this.icon = icon;
        } else {
            this.icon = code;
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }
}

package org.ecocean.location;

public class Region {

    public String code;
    public String name;

    public Region()
    {
        // deserialization
    }

    public Region(final String code, final String name)
    {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}

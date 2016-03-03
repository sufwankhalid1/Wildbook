package org.ecocean.location;

public class Country {

    public String code;
    public String name;

    public Country()
    {
        // deserialization
    }

    public Country(final String code, final String name)
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

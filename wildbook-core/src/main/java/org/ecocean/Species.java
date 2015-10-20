package org.ecocean;

public class Species {
    private String code;
    private String name;

    public Species() {
        // deserialization
    }

    public Species(final String code, final String name) {
        this.setCode(code);
        this.setName(name);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

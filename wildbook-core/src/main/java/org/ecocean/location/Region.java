package org.ecocean.location;

public class Region {
    private GeoLoc loc;
    private String code;
    private String name;

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

    public GeoLoc getLoc() {
        return loc;
    }

    public void setLoc(GeoLoc loc) {
        this.loc = loc;
    }
}

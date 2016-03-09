package org.ecocean.location;

public class SubRegion {
    private GeoLoc loc;
    private String name;

    public SubRegion()
    {
        // deserialization
    }

    public SubRegion(final GeoLoc loc, final String name)
    {
        this.setLoc(loc);
        this.name = name;
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

package org.ecocean;

public class VesselType {
    private Integer vesseltypeid;
    public String vesseltype;

    public VesselType() {
        // deserialization
    }

    public VesselType(final Integer vesseltypeid,
                  final String vesseltype)
    {
        this.vesseltypeid = vesseltypeid;
        this.vesseltype = vesseltype;
    }

    public Integer getVesselTypeId() {
        return vesseltypeid;
    }

    public void setVesselTypeId(final Integer vesseltypeid) {
        this.vesseltypeid = vesseltypeid;
    }

    public String getVesselType() {
        return vesseltype;
    }

    public void setVesselType(final String vesseltype) {
        this.vesseltype = vesseltype;
    }

}


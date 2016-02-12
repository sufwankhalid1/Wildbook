package org.ecocean;

public class Vessel {
    private Integer vesselid;
    private String name;
    private Integer orgid;
    private Integer vesseltypeid;

    public Vessel() {
        // deserialization
    }

    public Vessel(final Integer vesselId,
                  final Integer orgid,
                  final Integer vesseltypeid,
                  final String name)
    {
        this.vesselid = vesselId;
        this.orgid = orgid;
        this.vesseltypeid = vesseltypeid;
        this.name = name;
    }

    public Integer getVesselId() {
        return vesselid;
    }

    public void setVesselId(final Integer vesselId) {
        this.vesselid = vesselId;
    }

    public Integer getOrgId() {
        return orgid;
    }

    public void setOrgId(final Integer orgid) {
        this.orgid = orgid;
    }

    public Integer getVesselTypeId() {
        return vesseltypeid;
    }

    public void setVesselTypeId(final Integer vesseltypeid) {
        this.vesseltypeid = vesseltypeid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

//    public String getDisplayName() {
//        StringBuilder builder = new StringBuilder();
//        if (typeid != null) {
//            builder.append(typeid).append(": ");
//        }
//
//        builder.append(name);
//        return builder.toString();
//    }
}

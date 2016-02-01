package org.ecocean.survey;

public class Vessel {
    private Integer vesselid;
    private String name;
    private Integer orgid;
    private Integer typeid;
    private String test;

    public Vessel() {
        // deserialization
    }

    public Vessel(final Integer vesselId,
                  final Integer orgid,
                  final Integer typeid,
                  final String name)
    {
        this.vesselid = vesselId;
        this.orgid = orgid;
        this.typeid = typeid;
        this.name = name;
    }

    public String getTest() {
        return test;
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

    public Integer getTypeId() {
        return typeid;
    }

    public void setTypeId(final Integer typeid) {
        this.typeid = typeid;
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

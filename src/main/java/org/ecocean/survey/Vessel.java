package org.ecocean.survey;

public class Vessel {
    private Integer vesselId;
    private int orgId;
    private String type;
    private String name;

    public Vessel() {
        // deserialization
    }

    public Vessel(final Integer vesselId,
                  final int orgId,
                  final String type,
                  final String name)
    {
        this.vesselId = vesselId;
        this.orgId = orgId;
        this.type = type;
        this.name = name;
    }

    public Integer getVesselId() {
        return vesselId;
    }

    public void setVesselId(final Integer vesselId) {
        this.vesselId = vesselId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(final int orgId) {
        this.orgId = orgId;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDisplayName() {
        StringBuilder builder = new StringBuilder();
        if (type != null) {
            builder.append(type).append(": ");
        }

        builder.append(name);
        return builder.toString();
    }
}

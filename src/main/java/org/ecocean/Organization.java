package org.ecocean;

public class Organization {
    private Integer orgId;
    private String name;

    public Organization(final Integer orgId,
                        final String name)
    {
        this.orgId = orgId;
        this.name = name;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(final Integer orgId) {
        this.orgId = orgId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}

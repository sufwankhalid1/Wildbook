package org.ecocean;

public class CrewRole {
    private Integer crewroleid;
    private String role;

    public CrewRole() {
        // deserialization
    }

    public CrewRole(final Integer crewroleid,
                final String role) {
        this.crewroleid = crewroleid;
        this.role = role;
    }

    public Integer getCrewRoleId() {
        return crewroleid;
    }

    public void setCrewRoleId(final Integer crewroleid) {
        this.crewroleid = crewroleid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}


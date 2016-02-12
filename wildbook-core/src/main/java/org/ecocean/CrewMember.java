package org.ecocean;

import org.ecocean.rest.SimpleUser;

public class CrewMember {
    private SimpleUser user;
    private CrewRole crewrole;
    private Integer surveypartid;

    public CrewMember() {
        // deserialization
    }

    public CrewMember(final SimpleUser user,
                      final CrewRole crewrole,
                      final Integer surveypartid) {
        this.user = user;
        this.crewrole = crewrole;
        this.surveypartid = surveypartid;
    }

    public SimpleUser getUser() {
        return user;
    }

    public void setUser(final SimpleUser user) {
        this.user = user;
    }

    public CrewRole getCrewrole() {
        return crewrole;
    }

    public void setCrewrole(final CrewRole crewrole) {
        this.crewrole = crewrole;
    }


    public Integer getSurveypartid() {
        return surveypartid;
    }

    public void setSurveypartid(final Integer surveypartid) {
        this.surveypartid = surveypartid;
    }
}

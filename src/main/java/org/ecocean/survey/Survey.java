package org.ecocean.survey;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.Organization;

public class Survey {
    private Integer surveyId;
    private Organization organization;
    private String surveyNumber;

    public Survey(final Integer surveyId,
                  final Organization organization,
                  final String surveyNumber) {
        this.surveyId = surveyId;
        this.organization = organization;
        this.surveyNumber = surveyNumber;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public Integer getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(final Integer surveyId) {
        this.surveyId = surveyId;
    }

    public String getSurveyNumber() {
        return surveyNumber;
    }

    public void setSurveyNumber(final String surveyNumber) {
        this.surveyNumber = surveyNumber;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}

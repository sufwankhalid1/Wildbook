package org.ecocean.survey;

import java.time.LocalDate;
import java.time.LocalTime;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.ecocean.Location;
import org.ecocean.util.DateUtils;

public class SurveyPart {
    //
    // This can be a primitive since I think it will exist.
    //
    private int surveyId;
    private Integer surveyPartId;

    private Vessel vessel;

    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDate partDate;
    private LocalTime starttime;
    private LocalTime endtime;

    private String code;
    private String comments;
    private Location location;

    public Vessel getVessel() {
        return vessel;
    }

    public void setVessel(final Vessel vessel) {
        this.vessel = vessel;
    }

    public Integer getSurveyPartId() {
        return surveyPartId;
    }

    public void setSurveyPartId(final Integer surveyPartId) {
        this.surveyPartId = surveyPartId;
    }

    public int getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(final int surveyId) {
        this.surveyId = surveyId;
    }

    public LocalDate getPartDate() {
        return partDate;
    }

    public void setPartDate(final LocalDate partDate) {
        this.partDate = partDate;
    }

    public LocalTime getStarttime() {
        return starttime;
    }

    public void setStarttime(final LocalTime starttime) {
        this.starttime = starttime;
    }

    public LocalTime getEndtime() {
        return endtime;
    }

    public void setEndtime(final LocalTime endtime) {
        this.endtime = endtime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public String getFormattedTime() {
        return DateUtils.format(partDate, starttime, endtime);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

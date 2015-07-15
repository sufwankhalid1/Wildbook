package org.ecocean.media;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.ShepherdPMF;
import org.ecocean.Shepherd;
import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class MediaSubmission {
    private Long id;

    //
    // Either username is not null and name/email are null
    // or name/email are not null and username is null. i.e.
    // either this is a user we know about or someone from the general public.
    //
    private String username;
    private String name;
    private String email;
    private String verbatimLocation; //description of location
    private Double latitude;
    private Double longitude;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime startTime;
    private Long startTime;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime endTime;
    private Long endTime;
    private String description;
    private List<SinglePhotoVideo> media;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    private DateTime timeSubmitted = new DateTime();
    private Long timeSubmitted = new Date().getTime();
    private String status;

    //
    // To store the intended thing that this was submitted for, like the SurveyId.
    // If they get it wrong then we at least still have their intention and thus can
    // later hook it up to the right survey, or whatever the submission was for.
    //
    private String submissionid;

    public MediaSubmission() {
    }

    public MediaSubmission(RecordSet rs) throws DatabaseException {
        this.setDescription(rs.getString("description"));
        this.setEmail(rs.getString("email"));
        this.setEndTime(rs.getLongObj("endtime"));
        this.setId(rs.getLong("id"));
        this.setLatitude(rs.getDoubleObj("latitude"));
        this.setLongitude(rs.getDoubleObj("longitude"));
        this.setName(rs.getString("name"));
        this.setStartTime(rs.getLongObj("starttime"));
        this.setSubmissionid(rs.getString("submissionid"));
        this.setTimeSubmitted(rs.getLongObj("timesubmitted"));
        this.setUsername(rs.getString("username"));
        this.setVerbatimLocation(rs.getString("verbatimlocation"));
        this.setStatus(rs.getString("status"));
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerbatimLocation() {
        return verbatimLocation;
    }

    public void setVerbatimLocation(String verbatimLocation) {
        this.verbatimLocation = verbatimLocation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SinglePhotoVideo> getMedia() {
        return media;
    }

    public void setMedia(List<SinglePhotoVideo> media) {
        this.media = media;
    }

    public Long getTimeSubmitted() {
        return timeSubmitted;
    }

    public void setTimeSubmitted(Long timeSubmitted) {
        this.timeSubmitted = timeSubmitted;
    }

    public String getSubmissionid() {
        return submissionid;
    }

    public void setSubmissionid(String submissionid) {
        this.submissionid = submissionid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    } 

    //note: this does *not* set .media but will grab them all via the mediasubmission_media table
    public List<SinglePhotoVideo> fetchMedia(String context) throws DatabaseException {
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
        Database db = new Database(ci);
        if ((this.getId() == null) || this.getId().equals("")) return null;
        String sql = "SELECT * FROM mediasubmission_media WHERE mediasubmissionid=" + this.getId();
        RecordSet rs = db.getRecordSet(sql);
        Shepherd myShepherd = new Shepherd(context);
        List<SinglePhotoVideo> media = new ArrayList<SinglePhotoVideo>();
        while (rs.next()) {
            SinglePhotoVideo spv = myShepherd.getSinglePhotoVideo(rs.getString("mediaid"));
            if (spv != null) media.add(spv);
        }
        db.release();
        return media;
    }

    public static List<MediaSubmission> findMediaSources(List<SinglePhotoVideo> media, String context) {
        if ((media == null) || (media.size() < 1)) return null;
        List<MediaSubmission> msList = new ArrayList<MediaSubmission>();
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
        Database db = new Database(ci);
        String mids = media.get(0).getDataCollectionEventID();
        for (int i = 1 ; i < media.size() ; i++) {
            mids += "', '" + media.get(i).getDataCollectionEventID();
        }
        String sql = "SELECT DISTINCT id,description,email,endtime,latitude,longitude,name,starttime,submissionid,timesubmitted,username,verbatimlocation,status FROM mediasubmission INNER JOIN mediasubmission_media ON (mediasubmission_media.mediasubmissionid = mediasubmission.id) WHERE mediasubmission_media.mediaid IN ('" + mids + "') ORDER BY id";
System.out.println("sql = " + sql);
        try {
            RecordSet rs = db.getRecordSet(sql);
            while (rs.next()) {
                MediaSubmission ms = new MediaSubmission(rs);
                ms.setMedia(ms.fetchMedia(context));
                msList.add(ms);
            }
        } catch (DatabaseException dbe) {
            System.out.println(dbe.toString());
        } finally {
            db.release();
        }
        return msList;
    }

}

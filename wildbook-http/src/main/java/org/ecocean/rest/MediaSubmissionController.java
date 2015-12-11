package org.ecocean.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.Global;
import org.ecocean.email.EmailUtils;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.encounter.EncounterObj;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.media.MediaAssetType;
import org.ecocean.media.MediaSubmission;
import org.ecocean.media.MediaSubmissionFactory;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.ecocean.security.UserService;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.survey.SurveyFactory;
import org.ecocean.survey.SurveyPartObj;
import org.ecocean.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.GroupedSqlCondition;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlTable;
import com.samsix.database.Table;
import com.samsix.util.string.StringUtilities;

import de.neuland.jade4j.exceptions.JadeException;

@RestController
@RequestMapping(value = "/obj/mediasubmission")
public class MediaSubmissionController
{
    private static Logger logger = LoggerFactory.getLogger(MediaSubmissionController.class);


    @RequestMapping(value = "/photos/{submissionid}", method = RequestMethod.GET)
    public static List<SimplePhoto> getPhotos(final HttpServletRequest request,
                                              @PathVariable("submissionid")
                                              final String submissionid) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            String sql = "SELECT ma.* FROM mediasubmission_media msm"
                    + " INNER JOIN mediaasset ma ON ma.id = msm.mediaid"
                    + " WHERE msm.mediasubmissionid = " + submissionid
                    + " ORDER BY ma.metatimestamp, ma.id";
            return db.selectList(sql, (rs) -> {
                return MediaAssetFactory.readPhoto(rs);
            });
        }
    }

    @RequestMapping(value = "/encounters/{submissionid}", method = RequestMethod.GET)
    public static SubmissionEncounters getEncounters(final HttpServletRequest request,
                                                     @PathVariable("submissionid")
                                                     final String submissionid) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            //
            // Get the encounters from the encounter_media and any survey parts they
            // are part of.
            //
            SqlStatement sql = EncounterFactory.getEncounterStatement(true);
            sql.addInnerJoin(EncounterFactory.ALIAS_ENCOUNTERS,
                             EncounterFactory.PK_ENCOUNTERS,
                             EncounterFactory.TABLENAME_ENCOUNTER_MEDIA,
                             EncounterFactory.ALIAS_ENCOUNTER_MEDIA,
                             EncounterFactory.PK_ENCOUNTERS);
            sql.addInnerJoin(EncounterFactory.ALIAS_ENCOUNTER_MEDIA,
                             "mediaid",
                             MediaSubmissionFactory.TABLENAME_MEDIASUB_MEDIA,
                             MediaSubmissionFactory.ALIAS_MEDIASUB_MEDIA,
                             "mediaid");
            sql.addLeftOuterJoin(EncounterFactory.ALIAS_ENCOUNTERS,
                                 EncounterFactory.PK_ENCOUNTERS,
                                 "surveypart_encounters",
                                 "spe",
                                 EncounterFactory.PK_ENCOUNTERS);
            sql.appendSelectString("spe." + SurveyFactory.PK_SURVEYPART);
            sql.addCondition(MediaSubmissionFactory.ALIAS_MEDIASUB_MEDIA,
                             "mediasubmissionid",
                             SqlRelationType.EQUAL,
                             submissionid);


            SubmissionEncounters subEncs = new SubmissionEncounters();

            SqlStatement sql2;
            sql2 = SurveyFactory.getSurveyStatement(true);
            sql2.addInnerJoin(SurveyFactory.ALIAS_SURVEYPART,
                              SurveyFactory.PK_SURVEYPART,
                              "surveypart_encounters",
                              "spe",
                              SurveyFactory.PK_SURVEYPART);
            sql2.addCondition(SurveyFactory.ALIAS_SURVEYPART, SurveyFactory.PK_SURVEYPART, SqlRelationType.EQUAL, "?");

            SqlStatement sql3;
            sql3 = EncounterFactory.getEncounterStatement();
            sql3.addInnerJoin(EncounterFactory.ALIAS_ENCOUNTERS,
                              EncounterFactory.PK_ENCOUNTERS,
                              "surveypart_encounters",
                              "spe",
                              EncounterFactory.PK_ENCOUNTERS);
            sql3.addCondition("spe", SurveyFactory.PK_SURVEYPART, SqlRelationType.EQUAL, "?");

            db.select(sql, (rs) -> {
                Encounter encounter = EncounterFactory.readEncounter(rs);

                Integer spi = rs.getInteger(SurveyFactory.PK_SURVEYPART);

                if (spi == null) {
                    subEncs.encs.add(EncounterFactory.getEncounterObj(db, encounter));
                } else {
                    //
                    // If we were part of a survey then go ahead and read that survey info in.
                    //
                    SurveyEncounters ses = new SurveyEncounters();
                    ses.surveypart = db.selectFirst(sql2, (rs2) -> {
                        return SurveyFactory.readSurveyPartObj(rs2);
                    }, spi);

                    //
                    // Something went horribly wrong, this should not happen, but just in case...
                    //
                    if (ses.surveypart == null) {
                        subEncs.encs.add(EncounterFactory.getEncounterObj(db, encounter));
                    }

                    //
                    // Now read in the encounters that were attached to this survey part.
                    //
                    List<Encounter> ecs = db.selectList(sql3, (rs3) -> {
                        return EncounterFactory.readEncounter(rs3);
                    }, spi);

                    for (Encounter ec : ecs) {
                        ses.encs.add(EncounterFactory.getEncounterObj(db, ec));
                    }

                    subEncs.surveyEncounters.add(ses);
                }
            });

            return subEncs;
        }
    }


//    @RequestMapping(value = "/get/id/{mediaid}", method = RequestMethod.GET)
//    public MediaSubmission get(final HttpServletRequest request,
//                               @PathVariable("mediaid")
//                               final long mediaid)
//        throws DatabaseException
//    {
//        return getMediaSubmission(mediaid);
//    }

    private List<MediaSubmission> getMediaSubmissions(final Database db,
                                                      final SqlStatement sql) throws DatabaseException {
        sql.setOrderBy("timesubmitted desc");

        List<MediaSubmission> mss = new ArrayList<MediaSubmission>();

        db.select(sql, (rs) -> {
            mss.add(MediaSubmissionFactory.readMediaSubmission(rs));
        });

        return mss;

    }

    @RequestMapping(value = "/get/uncompleted", method = RequestMethod.GET)
    public List<MediaSubmission> getStatus(final HttpServletRequest request)
        throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            SqlStatement sql = MediaSubmissionFactory.getStatement();

            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            SqlTable table = sql.findTable(MediaSubmissionFactory.ALIAS_MEDIASUBMISSION);
            cond.addCondition(table, "status", SqlRelationType.NOT_EQUAL, "completed");
            cond.addCondition(table, "status", SqlRelationType.EQUAL, null);
            sql.addCondition(cond);

            return getMediaSubmissions(db, sql);
        }
    }


    @RequestMapping(value = "/get/status/{status}", method = RequestMethod.GET)
    public List<MediaSubmission> getStatus(final HttpServletRequest request,
                                           @PathVariable("status")
                                           final String status)
        throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            //
            // * will mean get all, so we just have an empty where formatter
            // we want all other values, included null, to pass to the append method
            //
            SqlStatement sql = MediaSubmissionFactory.getStatement();

            //
            // Use relationType null to mean don't search on anything, not even null.
            // Smelly, but that's why I put this comment here.
            //
            if (! "*".equals(status)) {
                sql.addCondition(MediaSubmissionFactory.ALIAS_MEDIASUBMISSION, "status", SqlRelationType.EQUAL, status);
            }

            return getMediaSubmissions(db, sql);
        }
    }

//    @RequestMapping(value = "/get/sources/{id}", method = RequestMethod.GET)
//    public List<MediaSubmission> getSources(final HttpServletRequest request,
//                                            @PathVariable("id") final int id) throws DatabaseException {
//        if (id < 1) return null;
//
//        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
//            String sql = "SELECT \"DATACOLLECTIONEVENTID_EID\" AS mid FROM \"SURVEYTRACK_MEDIA\" WHERE \"ID_OID\"=" + id;
//            RecordSet rs = db.getRecordSet(sql);
//            List<SinglePhotoVideo> media = new ArrayList<SinglePhotoVideo>();
//            while (rs.next()) {
//                SinglePhotoVideo spv = new SinglePhotoVideo();
//                spv.setDataCollectionEventID(rs.getString("mid"));
//                media.add(spv);
//            }
//
//            return findMediaSources(media, ServletUtilities.getContext(request));
//        }
//    }
//
//    @RequestMapping(value = "/get/sources", method = RequestMethod.POST)
//    public List<MediaSubmission> getSources(final HttpServletRequest request,
//                                            @RequestBody final List<SinglePhotoVideo> media) throws DatabaseException {
//        return findMediaSources(media, ServletUtilities.getContext(request));
//    }

    public static void deleteMedia(final Database db,
                                   final int submissionid,
                                   final int mediaid) throws DatabaseException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting mediaid [" + mediaid + "] from submission [" + submissionid + "]");
        }

        MediaAsset ma = MediaAssetFactory.load(db, mediaid);

        if (ma == null) {
            throw new IllegalArgumentException("No media with id [" + mediaid + "] found.");
        }

        Table table = db.getTable("mediasubmission_media");
        String where = "mediasubmissionid = "
                + submissionid
                + " AND mediaid = "
                + mediaid;
        table.deleteRows(where);

        MediaAssetFactory.delete(db, ma.getID());
        MediaAssetFactory.deleteFromStore(ma);
    }

    @RequestMapping(value = "/deletemedia", method = RequestMethod.POST)
    public void deleteMedia(final HttpServletRequest request,
                            @RequestBody final MSMEntry msm)
        throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (int mediaid : msm.mediaids) {
                    deleteMedia(db, msm.submissionid, mediaid);
                }
            });
        }
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public void deleteSubmission(final HttpServletRequest request,
                                 @RequestBody final MSMEntry msm)
        throws DatabaseException, IOException
    {
        try (Database db = ServletUtils.getDb(request)) {
            String mWhere = "mediasubmissionid = " + msm.submissionid;

            String sql = "SELECT ma.* FROM mediaasset ma"
                    + " INNER JOIN mediasubmission_media msm ON msm.mediaid = ma.id"
                    + " WHERE " + mWhere;
            List<MediaAsset> media;
            media = db.selectList(sql, (rs) -> {
                return MediaAssetFactory.valueOf(rs);
            });

            db.performTransaction(() -> {
                //
                // Delete all the joins
                //
                Table table = db.getTable("mediasubmission_media");
                table.deleteRows(mWhere);

                //
                // Delete the media submission itself.
                //
                table = db.getTable("mediasubmission");
                table.deleteRows("id = " + msm.submissionid);

                //
                // Delete all the files.
                //
                for (MediaAsset aMedia : media) {
                    MediaAssetFactory.delete(db, aMedia.getID());
                }
            });

            //
            // Now delete the physical files.
            //
            for (MediaAsset aMedia : media) {
                MediaAssetFactory.deleteFromStore(aMedia);
            }
        }
    }


    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public void complete(final HttpServletRequest request,
                         @RequestBody final MediaSubmission media)
        throws DatabaseException
    {
        UserService service = Global.INST.getUserService();

        User user;
        if (media.getUser() != null) {
            user = service.getUserById(media.getUser().getId().toString());
        } else {
            user = service.getUserByEmail(media.getEmail());
            //
            // The user was added to the database, let's make sure the
            // media submission has this info so that when we save it
            // it will be with the user.
            //
            if (user != null) {
                media.setUser(user.toSimple());
            }
        }

        try (Database db = ServletUtils.getDb(request)) {
            MediaSubmissionFactory.save(db, media);

            //
            // Email notify admin of new mediasubmission in WIldbook
            //
            Map<String, Object> model = EmailUtils.createModel();
            model.put(EmailUtils.TAG_SUBMISSION, media);
            if (user != null) {
                model.put(EmailUtils.TAG_USER, user.toSimple());
                model.put("userverified", user.isVerified());

                //
                // Create and send new reset token and add it
                // to the model so that the user can verify their account
                // directly from the email.
                //
                if (! user.isVerified()) {
                    try {
                        model.put(EmailUtils.TAG_TOKEN, UserFactory.createPWResetToken(db, user.getUserId()));
                    } catch (DatabaseException ex) {
                        logger.error("Can't create password reset token to send to user for verification.", ex);
                    }
                }
            }

            try {
                List<MediaAsset> mas = MediaSubmissionFactory.getMedia(db, media.getId());
                for (MediaAsset ma : mas) {
                    if (MediaAssetType.IMAGE.equals(ma.getType())) {
                        model.put("subinfo.photo", ma.thumbWebPathString());
                        break;
                    }
                }

                model.put("subinfo.number", String.valueOf(mas.size()));
                model.put("subinfo.date", DateUtils.epochMilliSecToString(media.getTimeSubmitted()));
            } catch (Throwable ex) {
                //
                // Catch everything so that we don't bail simply because something went wrong
                // here.
                //
                logger.error("Problem filling out the email model", ex);
            }

            //
            // Send email to admin to let them know that there has been a new submission.
            //
            try {
                EmailUtils.sendJadeTemplate(EmailUtils.getAdminSender(),
                                            EmailUtils.getAdminRecipients(),
                                            "admin/newSubmission",
                                            model);
            } catch (JadeException | IOException | MessagingException ex) {
                logger.error("Trouble sending admin email", ex);
            }

            //
            // Send email to user to thank them for their submission.
            //
            if (user != null) {
                Table table = db.getTable(MediaSubmissionFactory.TABLENAME_MEDIASUBMISSION);
                long count = table.getCount("id != " + media.getId() + " AND userid = " + user.getUserId());

                String template;
                if (count == 0) {
                    template = "media/firstSubmission";
                } else {
                    template = "media/anotherSubmission";
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("sending thankyou email to:" + user.getEmail());
                }
                try {
                    EmailUtils.sendJadeTemplate(EmailUtils.getAdminSender(),
                                                user.getEmail(),
                                                template,
                                                model);
                } catch (JadeException | IOException | MessagingException ex) {
                    logger.error("Trouble sending thank you email ["
                            + template
                            + "] to ["
                            + user.getEmail()
                            + "]", ex);
                }
            }

            //
            // Now finally remove the files from the users session object so that
            // they can submit again with a fresh set.
            //
            MediaUploadServlet.clearFileSet(request.getSession(), media.getSubmissionid());
        }
    }


    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Integer save(final HttpServletRequest request,
                        @RequestBody final MediaSubmission media)
        throws DatabaseException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling MediaSubmission save for media [" + media + "]");
        }

        try (Database db = ServletUtils.getDb(request)){
            //
            // Save media submission
            //
//            boolean isNew = (media.getId() == null);
            MediaSubmissionFactory.save(db, media);

            //
            // TODO: This code works as is EXCEPT due to the stupid IDX ordering column
            // that DataNucleus put on our SURVEY_MEDIA table along with making SURVEY_ID_OID/IDX being
            // the primary key (Why?!!!) we can't just add 0 for the IDX column. Sheesh.
            // Recreate the SURVEY_MEDIA table the way you want it and fix this later.
            //
//            if (isNew) {
//                //
//                // Check if the media submissionId matches a survey and if so
//                // insert into SURVEY_MEDIA table.
//                // TODO: Add a parameter to the save method to indicate that this
//                // media submission was intended for a survey so that we know if
//                // we should be doing this or something else with the submissionId.
//                //
//                RecordSet rs;
//                SqlWhereFormatter where = new SqlWhereFormatter();
//                where.append(SurveyFactory.PK_SURVEY), media.getSubmissionid());
//
//                rs = db.getTable("SURVEY").getRecordSet(where.getWhereClause());
//                if (rs.next()) {
//                    SqlInsertFormatter formatter;
//                    formatter = new SqlInsertFormatter();
//                    formatter.append("SURVEY_ID_OID"), rs.getInteger("SURVEY_ID"));
//                    formatter.append("ID_EID"), media.getId());
//                    formatter.append("IDX"), 0);
//                    db.getTable("SURVEY_MEDIA").insertRow(formatter);
//                }
//            }

            if (logger.isDebugEnabled()) {
                logger.debug("Returning media submission id [" + media.getId() + "]");
            }

            return media.getId();
        }
    }


    @RequestMapping(value = "reassign", method = RequestMethod.POST)
    public void addEncounter(final HttpServletRequest request,
                             @RequestBody @Valid final MSReassign reassign) throws DatabaseException {
        //
        // update all of the photos to point to this new user and then update the mediasubmission itself.
        //
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                SqlStatement subquery = new SqlStatement(MediaAssetFactory.TABLENAME_MEDIAASSET,
                                                         MediaAssetFactory.ALIAS_MEDIAASSET);
                subquery.setSelectString(MediaAssetFactory.PK_MEDIAASSET);
                subquery.addInnerJoin(MediaAssetFactory.ALIAS_MEDIAASSET,
                                      MediaAssetFactory.PK_MEDIAASSET,
                                      "mediasubmission_media",
                                      "msm",
                                      "mediaid");
                subquery.addCondition("msm", "mediasubmissionid", SqlRelationType.EQUAL, reassign.msid);

                String sql = "update " + MediaAssetFactory.TABLENAME_MEDIAASSET
                        + " set submitterid = " + reassign.userid
                        + " where id in (" + subquery.toString() + ")";
                db.executeUpdate(sql);

                sql = "update " + MediaSubmissionFactory.TABLENAME_MEDIASUBMISSION
                        + " set userid = " + reassign.userid
                        + " where id = " + reassign.msid;
                db.executeUpdate(sql);
            });
        }
    }

    @RequestMapping(value = "/setstatus/{msid}", method = RequestMethod.POST)
    public void setStatus(final HttpServletRequest request,
                        @PathVariable("msid")
                        final int msid,
                        @RequestBody final String status) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            String saveStatus;

            if ("null".equals(status)) {
                saveStatus = null;
            } else {
                saveStatus = status;
            }

            String sql = "update " + MediaSubmissionFactory.TABLENAME_MEDIASUBMISSION
                    + " set status = " + StringUtilities.wrapQuotes(saveStatus)
                    + " where id = " + msid;
            db.executeUpdate(sql);
        };
    }

    @RequestMapping(value = "/delfile/{msid}", method = RequestMethod.POST)
    public void delFile(final HttpServletRequest request,
                        @PathVariable("msid")
                        final int msid,
                        @RequestBody final String filename) throws DatabaseException
    {
        MediaUploadServlet.deleteFileFromSet(request, msid, filename);
    }


    @RequestMapping(value = "/getexif/{msid}", method = RequestMethod.GET)
    public ExifData getExif(final HttpServletRequest request,
                            @PathVariable("msid")
                            final long msid)
        throws DatabaseException
    {
        List<MediaAsset> media;
        try (Database db = ServletUtils.getDb(request)) {
            media = MediaSubmissionFactory.getMedia(db, msid);
        }

        ExifData data = new ExifData();
        ExifAvg avg = data.avg;

        double latSum = 0;
        int latCount = 0;
        double longSum = 0;
        int longCount = 0;

        for (MediaAsset ma : media) {
            ExifItem item = new ExifItem();
            item.mediaid = ma.getID();
            data.items.add(item);

            if (ma.getMetaTimestamp() != null) {
                item.time = DateUtils.ldtToMillis(ma.getMetaTimestamp());

                if (avg.minTime == null) {
                    avg.minTime = item.time;
                } else {
                    if (item.time < avg.minTime) {
                        avg.minTime = item.time;
                    }
                }
                if (avg.maxTime == null) {
                    avg.maxTime = item.time;
                } else {
                    if (item.time > avg.maxTime) {
                        avg.maxTime = item.time;
                    }
                }
            }

            item.latitude = ma.getMetaLatitude();
            item.longitude = ma.getMetaLongitude();

            if (item.latitude != null) {
                latSum += item.latitude;
                latCount += 1;
                longSum += item.longitude;
                longCount += 1;
            }
        }

        if (latCount > 0) {
            avg.latitude = latSum / latCount;
            avg.longitude = longSum / longCount;
        }

        return data;
    }


    public static class MSMEntry
    {
        public int submissionid;
        public List<Integer> mediaids;
    }

    public static class ExifItem
    {
        public Long time;
        public Double latitude;
        public Double longitude;
        public int mediaid;
    }

    public static class ExifAvg
    {
        public Long minTime;
        public Long maxTime;
        public Double latitude;
        public Double longitude;
    }

    public static class ExifData
    {
        public List<ExifItem> items = new ArrayList<ExifItem>();
        public ExifAvg avg = new ExifAvg();
    }

    public static class SubmissionEncounters
    {
        public List<EncounterObj> encs = new ArrayList<>();
        public List<SurveyEncounters> surveyEncounters = new ArrayList<>();
    }

    public static class SurveyEncounters {
        public SurveyPartObj surveypart;
        public List<EncounterObj> encs = new ArrayList<>();
    }

    static class MSReassign {
        public int msid;
        public int userid;
    }
}

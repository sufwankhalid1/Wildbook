package org.ecocean.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.ecocean.Point;
import org.ecocean.ShepherdPMF;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.media.MediaAssetType;
import org.ecocean.survey.SurveyTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SpecialSqlCondition;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

public class SimpleFactory {
    private final static Logger logger = LoggerFactory.getLogger(SimpleFactory.class);
    private final static int MIN_PHOTOS = 8;

    private SimpleFactory() {
        // prevent instantiation
    }


//    public static SimpleUser getUser(final String context,
//                                     final String username) {
//        Shepherd myShepherd = new Shepherd(context);
//        User user = myShepherd.getUser(username);
//
//        if (user == null) {
//            return null;
//        }
//
//        return getUser(user);
//    }


    public static List<SimpleEncounter> getIndividualEncounters(final Database db, final SimpleIndividual individual) throws DatabaseException
    {
        String sql = "SELECT * FROM encounters e WHERE individualid = " + individual.getId();

        List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();

        RecordSet rs = db.getRecordSet(sql);
        while (rs.next()) {
            encounters.add(readSimpleEncounter(individual, rs));
        }

        return encounters;
    }


    public static List<SimpleUser> readSimpleUsers(final Database db, final String sql) throws DatabaseException {
        List<SimpleUser> users = new ArrayList<>();

        RecordSet rs = db.getRecordSet(sql);
        while (rs.next()) {
            users.add(readUser(rs));
        }

        return users;
    }


    public static List<SimpleUser> getIndividualSubmitters(final Database db, final int individualid) throws DatabaseException
    {
        SqlStatement sql = getUserStatement(true);
        sql.addInnerJoin("u", "userid", "mediaasset", "ma2", "submitterid");
        sql.addInnerJoin("ma2", "id", "encounter_media", "em", "mediaid");
        sql.addInnerJoin("em", "encounterid", "encounters", "e", "encounterid");
        sql.addCondition("e", "individualid", SqlRelationType.EQUAL, individualid);

        return readSimpleUsers(db, sql.getSql());
    }

    public static List<SimplePhoto> getIndividualPhotos(final Database db, final int individualId) throws DatabaseException
    {
        //
        // Add photos
        //
        SqlStatement sql = new SqlStatement("mediaasset", "ma", "ma.*");
        sql.addInnerJoin("ma", "id", "encounter_media", "em", "mediaid");
        sql.addInnerJoin("em", "encounterid", "encounters", "e", "encounterid");
        sql.addCondition("ma", "type", SqlRelationType.EQUAL, MediaAssetType.IMAGE.getCode());
        sql.addCondition("e", "individualid", SqlRelationType.EQUAL, individualId);

        RecordSet rs;
        List<SimplePhoto> photos = new ArrayList<SimplePhoto>();

        //
        // Find the highlight images for this individual.
        //
        rs = db.getRecordSet(sql.getSql() + " AND 'highlight' = ANY (ma.tags)");
        while (rs.next()) {
            photos.add(readPhoto(rs));
        }

        //
        // If we are not at our minimum number of photos go ahead
        // and grab the rest at random. Grab the minimum number of photos
        // rather than the minimum minus the number already retrieved so
        // that we can throw out any duplicates.
        //
        if (photos.size() < MIN_PHOTOS) {
            rs = db.getRecordSet(sql.getSql() + " LIMIT " + MIN_PHOTOS);
            while (rs.next()) {
                if (photos.size() >= MIN_PHOTOS) {
                    break;
                }

                SimplePhoto photo = readPhoto(rs);

                boolean addphoto = true;
                for (SimplePhoto foto : photos) {
                    if (foto.getId() == photo.getId()) {
                        // don't add the same photo twice
                        addphoto = false;
                        break;
                    }
                }

                if (addphoto) {
                    photos.add(photo);
                }
            }
        }

        return photos;
    }


//    public static SimpleIndividual getIndividual(final String context,
//                                                 final MarkedIndividual mi) throws DatabaseException {
//
//        SimpleIndividual ind = new SimpleIndividual(mi.getIndividualID(), mi.getNickName());
//        ind.setSex(mi.getSex());
//        if (mi.getAvatar() != null) {
//            ind.setAvatar(mi.getAvatar().asUrl(context));
//        }
//
//        return ind;
//    }

    public static SqlStatement getIndividualStatement()
    {
        SqlStatement sql = new SqlStatement("individuals", "i");
        sql.addLeftOuterJoin("i", "avatarid", "mediaasset", "ma", "id");
        return sql;
    }

    public static SimpleIndividual getIndividual(final Database db, final int individualId) throws DatabaseException
    {
        RecordSet rs;
        SqlStatement sql = getIndividualStatement();
        sql.addCondition("i", "individualid", SqlRelationType.EQUAL, individualId);
        rs = db.getRecordSet(sql.getSql());
        if (rs.next()) {
            return readSimpleIndividual(rs);
        }

        return null;
    }


    public static SqlStatement getEncounterStatement()
    {
        SqlStatement sql = new SqlStatement("encounters", "e");
        sql.addLeftOuterJoin("e", "individualid", "individuals", "i", "individualid");
        sql.addLeftOuterJoin("i", "avatarid", "mediaasset", "ma", "id");
        return sql;
    }

    public static SqlStatement getUserStatement(final boolean distinct) {
        SqlStatement sql = getUserStatement();
        sql.setSelectString("u.*, ma.*");
        sql.setSelectDistinct(true);
        return sql;
    }

    public static SqlStatement getUserStatement() {
        SqlStatement sql = new SqlStatement("users", "u");
        sql.addLeftOuterJoin("u", "avatarid", "mediaasset", "ma", "id");
        return sql;
    }


    public static UserInfo getUserInfo(final int userid) throws DatabaseException
    {
        SimpleUser user = getUser(userid);

        if (user == null) {
            return null;
        }

        UserInfo userinfo;
        userinfo = new UserInfo(user);

        //
        // Add:
        // 4) Indivduals identified (unique Individuals from Encounters
        // 5) Voyages on
        //
        String sqlRoot = "SELECT ma.* FROM mediaasset ma";
        String whereRoot = " WHERE ma.submitterid = " + userid
            + " AND ma.type = " + MediaAssetType.IMAGE.getCode();

        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            String sql;
            RecordSet rs;

            //
            // 1) Highlighted Photos (including any avatar photos)
            //

            //
            // Did the user submit an avatar photo?
            //
            sql = sqlRoot
                    + " INNER JOIN individuals i ON ma.id = i.avatarid"
                    + whereRoot;
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                userinfo.addPhoto(readPhoto(rs));
            }

            //
            // Highlighted photos
            //
            sql = sqlRoot + whereRoot + " AND 'highlight' = ANY (ma.tags)";

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                userinfo.addPhoto(readPhoto(rs));
            }

            //
            // If we are not at our minimum number of photos go ahead
            // and grab the rest at random. Grab the minimum number of photos
            // rather than the minimum minus the number already retrieved so
            // that we can throw out any duplicates. That code is embedded
            // in the addPhoto method of the SimpleIndividual
            //
            if (userinfo.getPhotos().size() < MIN_PHOTOS) {
                sql = sqlRoot + whereRoot + " LIMIT " + MIN_PHOTOS;

                rs = db.getRecordSet(sql);
                while (rs.next()) {
                    if (userinfo.getPhotos().size() >= MIN_PHOTOS) {
                        break;
                    }

                    userinfo.addPhoto(readPhoto(rs));
                }
            }

            //
            // 2) Total Number of photos
            //
            sql = "SELECT count(*) AS count FROM mediaasset ma" + whereRoot;
            rs = db.getRecordSet(sql);
            if (rs.next()) {
                userinfo.setTotalPhotoCount(rs.getInt("count"));
            }

            //
            // 3) Encounters/Individuals
            // NOTE: Doing the individual stuff here on the server even though the information
            // is duplicated because javascript does not have hashmaps which makes the code to try and
            // get all the unique values of individualID into an array much messier.
            //
            SqlStatement ss = getEncounterStatement();
            ss.addCondition(new SpecialSqlCondition("exists (select * from encounter_media em"
                    + " inner join mediaasset ma on ma.id = em.mediaid"
                    + whereRoot
                    + " and em.encounterid = e.encounterid)"));

            Map<Integer, SimpleIndividual> inds = new HashMap<>();
            rs = db.getRecordSet(ss.getSql());
            while (rs.next()) {
                Integer indid = rs.getInteger("individualid");

                SimpleIndividual ind = inds.get(indid);
                if (ind == null) {
                    ind = readSimpleIndividual(rs);
                    if (ind != null) {
                        inds.put(ind.getId(), ind);
                    }
                }

                SimpleEncounter encounter = readSimpleEncounter(ind, rs);
                userinfo.addEncounter(encounter);
            }
            userinfo.setIndividuals(new ArrayList<SimpleIndividual>(inds.values()));

            //
            // TODO: Fix this so that it works with new file types. Don't think we need to convert the tiny
            // bit of data that has accumulated on the server as I think it is only John's test data.
            //
//            //
//            // Get voyages they were a part of...
//            // TODO: This is a bad design! We are getting the voyages they were on by the photos they submitted to the SurveyTrack!
//            // Bleh! We need to have a SurveyTrack_User table that just indicates the user was on that SurveyTrack. For now I'm
//            // just going to code it based on the photos taken knowing that this will change.
//            //
//            sql = "SELECT DISTINCT(\"ID_OID\") as ID FROM \"SURVEYTRACK_MEDIA\" stm"
//                    +  " INNER JOIN \"SINGLEPHOTOVIDEO\" spv ON stm.\"DATACOLLECTIONEVENTID_EID\" = spv.\"DATACOLLECTIONEVENTID\""
//                    + whereRoot;
//            rs = db.getRecordSet(sql);
//            while (rs.next()) {
//                userinfo.addVoyage(getSurveyTrack(db, rs.getLong("ID")));
//            }
            userinfo.setVoyages(Collections.<SurveyTrack>emptyList());
        }

        return userinfo;
    }


    public static SimpleIndividual readSimpleIndividual(final RecordSet rs) throws DatabaseException
    {
        Integer indid = rs.getInteger("individualid");
        if (indid == null) {
            return null;
        }

        SimpleIndividual ind = new SimpleIndividual(indid, rs.getString("nickname"));
        ind.setSex(rs.getString("sex"));
        ind.setSpecies(rs.getString("species"));
        ind.setAlternateId(rs.getString("alternateid"));

        SimplePhoto photo = readPhoto(rs);
        if (photo != null) {
            ind.setAvatar(photo.getThumbUrl());
        }

        return ind;
    }


    private static List<SurveyTrack> readSurveyTracks(final RecordSet rs) throws DatabaseException
    {
        List<SurveyTrack> tracks = new ArrayList<SurveyTrack>();

        SurveyTrack track = null;
        long trackId = -1;
        while (rs.next())
        {
            long id = rs.getLong("ID");
            if (track == null || id != trackId) {
                track = readSurveyTrack(rs);
                tracks.add(track);
            }

            track.addPoint(readPoint(rs));
        }

        return tracks;
    }


    private static SurveyTrack getSurveyTrack(final Database db, final long id) throws DatabaseException
    {
        String sql = "SELECT st.*, p.* FROM \"SURVEYTRACK\" st"
                + " INNER JOIN \"SURVEYTRACK_POINTS\" stp ON stp.\"ID_OID\" = st.\"ID\""
                + " INNER JOIN \"POINT\" p ON p.\"POINT_ID\" = stp.\"POINT_ID_EID\""
                + " WHERE st.\"ID\" = " + id
                + " ORDER BY st.\"ID\", p.\"TIMESTAMP\"";

        RecordSet rs = db.getRecordSet(sql);
        return readSurveyTrack(rs);
    }


    private static SurveyTrack readSurveyTrack(final RecordSet rs) throws DatabaseException
    {
        SurveyTrack track = null;
        while (rs.next()) {
            if (track == null) {
                track = readSurveyTrackCore(rs);
            }

            track.addPoint(readPoint(rs));
        }

        return track;
    }


    private static SurveyTrack readSurveyTrackCore(final RecordSet rs) throws DatabaseException
    {
        SurveyTrack surveyTrack = new SurveyTrack();
        surveyTrack.setId(rs.getLong("ID"));
        surveyTrack.setName(rs.getString("NAME"));
        surveyTrack.setType(rs.getString("TYPE"));
        surveyTrack.setVesselId(rs.getString("VESSELID"));

        return surveyTrack;
    }


    private static Point readPoint(final RecordSet rs) throws DatabaseException
    {
        Point point = new Point();
        point.setElevation(rs.getDouble("ELEVATION"));
        point.setLatitude(rs.getDouble("LATITUDE"));
        point.setLongitude(rs.getDouble("LONGITUDE"));
        point.setTimestamp(rs.getLongObj("TIMESTAMP"));
        return point;
    }


    public static SimpleEncounter readSimpleEncounter(final SimpleIndividual individual,
                                                      final RecordSet rs) throws DatabaseException
    {
        SimpleEncounter encounter = new SimpleEncounter(rs.getInt("encounterid"), rs.getLocalDate("encdate"));

        encounter.setStarttime(rs.getOffsetTime("starttime"));
        encounter.setEndtime(rs.getOffsetTime("endtime"));
        encounter.setLocationid(rs.getString("locationid"));
        encounter.setLatitude(rs.getDoubleObj("latitude"));
        encounter.setLongitude(rs.getDoubleObj("longitude"));
        encounter.setVerbatimLocation(rs.getString("verbatimLocation"));

        encounter.setIndividual(individual);

        return encounter;
    }

    public static SimpleEncounter readSimpleEncounter(final RecordSet rs) throws DatabaseException
    {
        return readSimpleEncounter(readSimpleIndividual(rs), rs);
    }


//    public static SimpleUser getUser(final String context, final String configDir, final String username) throws DatabaseException
//    {
//        Client client = Stormpath.getClient(configDir);
//        AccountList accounts = Stormpath.getAccounts(client, username);
//
//        if (accounts.getSize() < 1) {
//            return getUser(username);
//        }
//
//        return getStormpathUser(accounts.iterator().next());
//    }


//    public static SimpleEncounter getEncounter(final String context, final String configDir, final Encounter encounter) throws DatabaseException
//    {
//        SimpleEncounter se = new SimpleEncounter(encounter.getDWCGlobalUniqueIdentifier(),
//                                                 encounter.getDateInMilliseconds());
//        se.setLocationid(encounter.getLocationID());
//        se.setVerbatimLocation(encounter.getLocation());
//        se.setLatitude(encounter.getLatitude());
//        se.setLongitude(encounter.getLongitude());
//
//        se.setSubmitter(getUser(encounter.getSubmitterID()));
//
//        return se;
//    }

    public static String getThumbnail(final String url) {
        return getScaledImage(url, MediaUploadServlet.THUMB_DIR);
    }

    public static String getMidsizeFile(final String url) {
        return getScaledImage(url, MediaUploadServlet.MID_DIR);
    }

    private static String getScaledImage(final String url, final String subdir) {
        int index = url.lastIndexOf( File.separatorChar );
        return url.substring(0, index) + "/" + subdir + url.substring(index);
    }


    public static SimplePhoto getPhoto(final String context,
                                       final SinglePhotoVideo spv)
    {
        String url = spv.asUrl(context);

        //
        // TODO: Get rid of this actually. I don't want to have to mess with the voyage
        // page at the moment so i'm just going to make it compile.
        //
//        return new SimplePhoto(spv.getDataCollectionEventID(), url, getThumbnail(url));
        return new SimplePhoto(0, url, getThumbnail(url));
    }


    public static SimplePhoto readPhoto(final RecordSet rs) throws DatabaseException
    {
        MediaAsset ma = MediaAssetFactory.valueOf(rs);

        if (ma == null) {
            return null;
        }

        return new SimplePhoto(ma.getID(), ma.webPathString(), ma.thumbWebPathString());
    }


//    public static SimpleUser getUser(final User user)
//    {
//        SimpleUser su = new SimpleUser(user.getUsername(), user.getEmailAddress());
//
//        if (user.getUserImage() != null) {
//            su.setAvatar(user.getUserImage().webPath().getFile());
//        }
//
//        su.setAffiliation(user.getAffiliation());
//        su.setFullName(user.getFullName());
//
//        return su;
//    }


    public static SimpleUser getUser(final String username) throws DatabaseException
    {
        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            SqlStatement sql = getUserStatement();
            sql.addCondition("u", "username",SqlRelationType.EQUAL, username);
            RecordSet rs;
            rs = db.getRecordSet(sql.getSql());
            if (rs.next()) {
                return readUser(rs);
            }

            return null;
        }
    }


    public static SimpleUser getUserByIdString(final String userid)
    {
        //
        // I decided to swallow the error here because I didn't want to bother
        // catching errors in the jsp files which lead me to write this method.
        // Not critical if you want to change it.
        //
        try (Database db = ShepherdPMF.getDb()) {
            return getUser(NumberUtils.createInteger(userid));
        } catch (DatabaseException ex) {
            logger.error("Can't get user from idstring [" + userid + "]", ex);
            return null;
        }
    }


    public static SimpleUser getUser(final Integer userid) throws DatabaseException
    {
        if (userid == null) {
            return null;
        }

        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            SqlStatement sql = getUserStatement();
            sql.addCondition("u", "userid",SqlRelationType.EQUAL, userid);
            RecordSet rs;
            rs = db.getRecordSet(sql.getSql());
            if (rs.next()) {
                return readUser(rs);
            }

            return null;
        }
    }


    public static SimpleUser readUser(final RecordSet rs) throws DatabaseException
    {
        Integer id = rs.getInteger("userid");
        if (id == null) {
            return null;
        }

        SimpleUser user = new SimpleUser(id, rs.getString("username"), rs.getString("fullname"));

        MediaAsset ma = MediaAssetFactory.valueOf(rs);

        if (ma != null) {
            user.setAvatar(ma.webPathString(), rs.getString("email"));
        }
        user.setStatement(rs.getString("statement"));

//        user.setAffiliation(rs.getString("AFFILIATION"));

        return user;
    }

    public static SimpleUser getProfiledUser() {

        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            //
            // Weird way to get random row but seems to work. Probably won't scale super well but we
            // can deal with that later.
            //
            SqlStatement sql = getUserStatement();
            sql.addCondition(new SpecialSqlCondition("u.statement IS NOT NULL"));
            sql.setOrderBy("random()");
            sql.setLimit(1);

            RecordSet rs;
            try {
                rs = db.getRecordSet(sql.getSql());
                if (rs.next()) {
                    return readUser(rs);
                }
            } catch (DatabaseException ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }
}

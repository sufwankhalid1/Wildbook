package org.ecocean.rest;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Individual;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.encounter.SimpleEncounter;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.media.MediaAssetType;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.survey.SurveyPart;
import org.ecocean.util.LogBuilder;
import org.ecocean.util.WildbookUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SpecialSqlCondition;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.util.string.StringUtilities;

@RestController
@RequestMapping(value = "/data")
public class MainController
{
    private static Logger logger = LoggerFactory.getLogger(MainController.class);
    private final static int MIN_PHOTOS = 8;


    public static List<Contributor> getTopContributors(final HttpServletRequest request,
                                                       final int pastNumDays,
                                                       final int number)
            throws DatabaseException
    {
        LocalDate since = LocalDate.now().minusDays(pastNumDays);

        String table = "(select ma2.submitterid, count(*) as numEncs from mediaasset ma2"
                + " inner join encounter_media em on ma2.id = em.mediaid"
                + " inner join encounters e on em.encounterid = e.encounterid"
                + " where e.encdate > "
                + StringUtilities.wrapQuotes(since.toString())
                + " and submitterid is not null"
                + " group by ma2.submitterid"
                + " order by numEncs desc limit "
                + number
                + ")";

        SqlStatement sql = UserFactory.getUserStatement();
        sql.addInnerJoin(UserFactory.AlIAS_USERS, UserFactory.PK_USERS, table, "c", "submitterid");
        try (Database db = ServletUtils.getDb(request)) {
            return db.selectList(sql, (rs) -> {
                Contributor contrib = new Contributor();
                contrib.user = UserFactory.readSimpleUser(rs);
                contrib.numEncs = rs.getInt("numEncs");
                return contrib;
            });
        }
    }

    @RequestMapping(value = "/individual/get/{id}", method = RequestMethod.GET)
    public IndividualInfo getIndividual(final HttpServletRequest request,
                                        @PathVariable("id")
                                        final int id) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            IndividualInfo indinfo = new IndividualInfo();

            Individual ind = EncounterFactory.getIndividual(db, id);
            if (ind == null) {
                return null;
            }

            List<Encounter> encounters = EncounterFactory.getIndividualEncounters(db, ind);

            indinfo.individual = ind.toSimple();
            indinfo.photos = MainController.getIndividualPhotos(db, id);
            indinfo.encounters = WildbookUtils.convertList(encounters, encounter -> encounter.toSimple());
            indinfo.submitters = EncounterFactory.getIndividualSubmitters(db, id);

            return indinfo;
        }
    }


    @RequestMapping(value = "/encounter/sighters/{id}", method = RequestMethod.GET)
    public List<SimpleUser> getUser(final HttpServletRequest request,
                                    @PathVariable("id")
                                    final int encounterid) throws DatabaseException
    {
        SqlStatement sql = UserFactory.getUserStatement(true);
        sql.addInnerJoin(UserFactory.AlIAS_USERS,
                         UserFactory.PK_USERS,
                         MediaAssetFactory.TABLENAME_MEDIAASSET,
                         "ma2",
                         "submitterid");
        sql.addInnerJoin("ma2",
                         MediaAssetFactory.PK_MEDIAASSET,
                         "encounter_media",
                         "em",
                         "mediaid");
        sql.addCondition("em", "encounterid", SqlRelationType.EQUAL, encounterid);

        try (Database db = ServletUtils.getDb(request)) {
            List<SimpleUser> sighters = new ArrayList<>();

            db.select(sql, (rs) -> {
                sighters.add(UserFactory.readSimpleUser(rs));
            });

            return sighters;
        }
    }


    @RequestMapping(value = "/user/get/{username}", method = RequestMethod.GET)
    public SimpleBeing getUser(final HttpServletRequest request,
                               @PathVariable("username")
                               final String username) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            return UserFactory.getSimpleUser(db, username);
        }
    }


    @RequestMapping(value = "/userinfo/get/{username}", method = RequestMethod.GET)
    public UserInfo getUserInfo(final HttpServletRequest request,
                                @PathVariable("username")
                                final int userid) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            return MainController.getUserInfo(db, userid);
        }
    }


    @RequestMapping(value = "/config/value/{var}", method = RequestMethod.GET)
    public String getConfigValue(final HttpServletRequest request,
            @PathVariable("var")
            final String var)
    {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.get()
                    .appendVar("var", var)
                    .appendVar("value", request.getServletContext().getInitParameter(var)).toString());
        }
        return request.getServletContext().getInitParameter(var);
    }

    public static UserInfo getUserInfo(final Database db, final int userid) throws DatabaseException
        {
            SimpleUser user = UserFactory.getUser(db, userid);

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

            String sql;
            //
            // 1) Highlighted Photos (including any avatar photos)
            //

            //
            // Did the user submit an avatar photo?
            //
            sql = sqlRoot
                    + " INNER JOIN individuals i ON ma.id = i.avatarid"
                    + whereRoot;
            db.select(sql, (rs) -> {
                userinfo.addPhoto(MediaAssetFactory.readPhoto(rs));
            });

            //
            // Highlighted photos
            //
            sql = sqlRoot + whereRoot + " AND 'highlight' = ANY (ma.tags)";

            db.select(sql, (rs) -> {
                userinfo.addPhoto(MediaAssetFactory.readPhoto(rs));
            });

            //
            // If we are not at our minimum number of photos go ahead
            // and grab the rest at random. Grab the minimum number of photos
            // rather than the minimum minus the number already retrieved so
            // that we can throw out any duplicates. That code is embedded
            // in the addPhoto method of the SimpleIndividual
            //
            if (userinfo.getPhotos().size() < MIN_PHOTOS) {
                sql = sqlRoot + whereRoot + " LIMIT " + MIN_PHOTOS;

                db.select(sql, (rs) -> {
                    if (userinfo.getPhotos().size() < MIN_PHOTOS) {
                        userinfo.addPhoto(MediaAssetFactory.readPhoto(rs));
                    }
                });
            }

            //
            // 2) Total Number of photos
            //
            String criteria = "submitterid = " + userid
                    + " AND type = " + MediaAssetType.IMAGE.getCode();
            userinfo.setTotalPhotoCount(db.getTable(MediaAssetFactory.TABLENAME_MEDIAASSET).getCount(criteria));

            //
            // 3) Encounters/Individuals
            // NOTE: Doing the individual stuff here on the server even though the information
            // is duplicated because javascript does not have hashmaps which makes the code to try and
            // get all the unique values of individualID into an array much messier.
            //
            SqlStatement ss = EncounterFactory.getEncounterStatement();
            ss.addCondition(new SpecialSqlCondition("exists (select * from encounter_media em"
                    + " inner join mediaasset ma on ma.id = em.mediaid"
                    + whereRoot
                    + " and em.encounterid = e.encounterid)"));

            Map<Integer, Individual> inds = new HashMap<>();
            db.select(ss, (rs) -> {
                Integer indid = rs.getInteger("individualid");

                Individual ind = inds.get(indid);
                if (ind == null) {
                    ind = EncounterFactory.readIndividual(rs);
                    if (ind != null) {
                        inds.put(ind.getId(), ind);
                    }
                }

                Encounter encounter = EncounterFactory.readEncounter(ind, rs);
                userinfo.addEncounter(encounter.toSimple());
            });
            userinfo.setIndividuals(WildbookUtils.convertList(new ArrayList<Individual>(inds.values()), (ind) -> ind.toSimple()));

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
    //                userinfo.addVoyage(SurveyFactory.getSurveyTrack(db, rs.getLong("ID")));
    //            }
            userinfo.setVoyages(Collections.<SurveyPart>emptyList());

            return userinfo;
        }


    public static List<SimplePhoto> getIndividualPhotos(final Database db, final int individualId) throws DatabaseException
    {
        //
        // Add photos
        //
        SqlStatement sql = new SqlStatement(MediaAssetFactory.TABLENAME_MEDIAASSET,
                                            MediaAssetFactory.ALIAS_MEDIAASSET,
                                            MediaAssetFactory.ALIAS_MEDIAASSET + ".*");
        sql.addInnerJoin(MediaAssetFactory.ALIAS_MEDIAASSET, MediaAssetFactory.PK_MEDIAASSET, "encounter_media", "em", "mediaid");
        sql.addInnerJoin("em", "encounterid", "encounters", "e", "encounterid");
        sql.addCondition(MediaAssetFactory.ALIAS_MEDIAASSET,
                         "type",
                         SqlRelationType.EQUAL,
                         MediaAssetType.IMAGE.getCode());
        sql.addCondition("e", "individualid", SqlRelationType.EQUAL, individualId);

        RecordSet rs;
        List<SimplePhoto> photos = new ArrayList<>();

        //
        // Find the highlight images for this individual.
        //
        rs = db.getRecordSet(sql.getSql() + " AND 'highlight' = ANY (" + MediaAssetFactory.ALIAS_MEDIAASSET + ".tags)");
        while (rs.next()) {
            photos.add(MediaAssetFactory.readPhoto(rs));
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

                SimplePhoto photo = MediaAssetFactory.readPhoto(rs);

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

    static class IndividualInfo
    {
        public SimpleIndividual individual;
        public List<SimpleEncounter> encounters;
        public List<SimplePhoto> photos;
        public List<SimpleUser> submitters;
    }

    public static class Contributor {
        public SimpleUser user;
        public int numEncs;
    }
}

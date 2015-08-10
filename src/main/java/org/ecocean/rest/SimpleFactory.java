package org.ecocean.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ecocean.ShepherdPMF;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.media.MediaAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.util.string.StringUtilities;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.directory.CustomData;

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


    public static List<SimpleEncounter> getIndividualEncounters(final String context, final SimpleIndividual individual) throws DatabaseException
    {
        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            String sql = "SELECT * FROM \"ENCOUNTER\" e"
                    + " LEFT OUTER JOIN \"USERS\" u ON u.\"USERNAME\" = e.\"SUBMITTERID\""
                    + " LEFT OUTER JOIN mediaasset ma ON ma.id = u.\"USERIMAGEID\""
                    + " WHERE \"INDIVIDUALID\" = " + StringUtilities.wrapQuotes(individual.getId());

            List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();

            RecordSet rs;
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                encounters.add(readSimpleEncounter(individual, rs));
            }

            return encounters;
        }
    }

    public static List<SimplePhoto> getIndividualPhotos(final String context, final String individualId) throws DatabaseException
    {
        //
        // Add photos
        //
       try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            String sqlRoot = "SELECT spv.* FROM \"SINGLEPHOTOVIDEO\" spv"
                    + " INNER JOIN \"ENCOUNTER_IMAGES\" ei ON spv.\"DATACOLLECTIONEVENTID\" = ei.\"DATACOLLECTIONEVENTID_EID\""
                    + " INNER JOIN \"MARKEDINDIVIDUAL_ENCOUNTERS\" mie ON mie.\"CATALOGNUMBER_EID\" = ei.\"CATALOGNUMBER_OID\"";
            String mtmJoin = " INNER JOIN \"MEDIATAG_MEDIA\" mtm ON spv.\"DATACOLLECTIONEVENTID\" = mtm.\"DATACOLLECTIONEVENTID_EID\"";
            String whereRoot = " WHERE mie.\"INDIVIDUALID_OID\" = " + StringUtilities.wrapQuotes(individualId);

            String sql;
            RecordSet rs;
            List<SimplePhoto> photos = new ArrayList<SimplePhoto>();

            //
            // Find the highlight images for this individual.
            //
            sql = sqlRoot + mtmJoin + whereRoot
                    + " AND mtm.\"NAME_OID\" = 'highlight'";

            rs = db.getRecordSet(sql);
            while (rs.next()) {
//                photos.add(getPhoto(context, readPhoto(rs)));
                photos.add(getPhoto(context, rs));
            }

            //
            // If we are not at our minimum number of photos go ahead
            // and grab the rest at random. Grab the minimum number of photos
            // rather than the minimum minus the number already retrieved so
            // that we can throw out any duplicates.
            //
            if (photos.size() < MIN_PHOTOS) {
                sql = sqlRoot + whereRoot + " LIMIT " + MIN_PHOTOS;

                rs = db.getRecordSet(sql);
                while (rs.next()) {
                    if (photos.size() >= MIN_PHOTOS) {
                        break;
                    }

                    SimplePhoto photo = getPhoto(context, rs);

                    boolean addphoto = true;
                    for (SimplePhoto foto : photos) {
                        if (foto.getId().equals(photo.getId())) {
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


    public static SimpleIndividual getIndividual(final String context, final String individualId) throws DatabaseException
    {
        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            String sql;
            RecordSet rs;
            sql = "select * from \"MARKEDINDIVIDUAL\" mi"
                    + " left outer join \"SINGLEPHOTOVIDEO\" spv on mi.\"AVATAR_DATACOLLECTIONEVENTID_OID\" = spv.\"DATACOLLECTIONEVENTID\""
                    + " where \"INDIVIDUALID\" = " + StringUtilities.wrapQuotes(individualId);
            rs = db.getRecordSet(sql);
            if (rs.next()) {
                return readSimpleIndividual(context, rs);
            }

            return null;
        }
    }


    public static UserInfo getUserInfo(final String context,
                                       final String username) throws DatabaseException
    {
        UserInfo userinfo;
        userinfo = new UserInfo(getUser(username));

        //
        // Add:
        // 4) Indivduals identified (unique Individuals from Encounters
        // 5) Voyages on
        //
        String sqlRoot = "SELECT spv.* FROM \"SINGLEPHOTOVIDEO\" spv";
        String whereRoot = " WHERE spv.\"SUBMITTER\" = " + StringUtilities.wrapQuotes(username);

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
                    + " INNER JOIN \"MARKEDINDIVIDUAL\" mi ON spv.\"DATACOLLECTIONEVENTID\" = mi.\"AVATAR_DATACOLLECTIONEVENTID_OID\""
                    + whereRoot;
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                userinfo.addPhoto(getPhoto(context, rs));
            }

            //
            // Highlighted photos
            //
            sql = sqlRoot
                    + " INNER JOIN \"MEDIATAG_MEDIA\" mtm ON spv.\"DATACOLLECTIONEVENTID\" = mtm.\"DATACOLLECTIONEVENTID_EID\""
                    + whereRoot
                    + " AND mtm.\"NAME_OID\" = 'highlight'";

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                userinfo.addPhoto(getPhoto(context, rs));
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

                    userinfo.addPhoto(getPhoto(context, rs));
                }
            }

            //
            // 2) Total Number of photos
            //
            sql = "SELECT count(*) AS count FROM \"SINGLEPHOTOVIDEO\" spv" + whereRoot;
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
            sql = "select e.*, mi.*, spva.*, u.*, ma.* from \"ENCOUNTER\" e"
                    + " inner join \"ENCOUNTER_IMAGES\" ei on e.\"CATALOGNUMBER\" = ei.\"CATALOGNUMBER_OID\""
                    + " inner join \"SINGLEPHOTOVIDEO\" spv on ei.\"DATACOLLECTIONEVENTID_EID\" = spv.\"DATACOLLECTIONEVENTID\""
                    + " left outer join \"MARKEDINDIVIDUAL\" mi on e.\"INDIVIDUALID\" = mi.\"INDIVIDUALID\""
                    + " left outer join \"SINGLEPHOTOVIDEO\" spva on mi.\"AVATAR_DATACOLLECTIONEVENTID_OID\" = spva.\"DATACOLLECTIONEVENTID\""
                    + " LEFT OUTER JOIN \"USERS\" u ON u.\"USERNAME\" = e.\"SUBMITTERID\""
                    + " LEFT OUTER JOIN mediaasset ma ON ma.id = u.\"USERIMAGEID\""
                    + whereRoot;
            Map<String, SimpleIndividual> inds = new HashMap<String, SimpleIndividual>();
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                SimpleEncounter encounter = readSimpleEncounter(context, rs);
                SimpleIndividual ind = encounter.getIndividual();
                if (ind != null) {
                    inds.put(ind.getId(), ind);
                }
                userinfo.addEncounter(encounter);
            }
            userinfo.setIndividuals(new ArrayList<SimpleIndividual>(inds.values()));
        }

        return userinfo;
    }


    private static SimpleIndividual readSimpleIndividual(final String context,
                                                         final RecordSet rs) throws DatabaseException
    {
        String id = rs.getString("INDIVIDUALID");

        if (id == null) {
            return null;
        }

        SimpleIndividual ind = new SimpleIndividual(id, rs.getString("NICKNAME"));
        ind.setSex(rs.getString("SEX"));

//        SinglePhotoVideo spv = readPhoto(rs);
//        if (spv != null) {
//            ind.setAvatar(spv.asUrl(context));
//        }
        SimplePhoto photo = getPhoto(context, rs);
        if (photo != null) {
            ind.setAvatar(photo.getUrl());
        }

        return ind;
    }


//    private static Encounter readEncounter(final RecordSet rs) throws DatabaseException
//    {
//        Encounter encounter = new Encounter();
//
//        encounter.setEncounterNumber(rs.getString("CATALOGNUMBER"));
//        encounter.setDWCGlobalUniqueIdentifier(rs.getString("GUID"));
//        encounter.setDateInMilliseconds(rs.getLong("DATEINMILLISECONDS"));
//        encounter.setLocationID(rs.getString("LOCATIONID"));
//        encounter.setLatitude(rs.getDoubleObj("DECIMALLATITUDE"));
//        encounter.setLongitude(rs.getDoubleObj("DECIMALLONGITUDE"));
//        encounter.setSubmitterName(rs.getString("SUBMITTERID"));
//        encounter.setIndividualID(rs.getString("INDIVIDUALID"));
//
//        return encounter;
//    }


    private static SimpleEncounter readSimpleEncounter(final SimpleIndividual individual,
                                                       final RecordSet rs) throws DatabaseException
    {
        String number = rs.getString("CATALOGNUMBER");
        if (number == null) {
            return null;
        }

        SimpleEncounter encounter = new SimpleEncounter(number, rs.getLong("DATEINMILLISECONDS"));

        encounter.setLocationid(rs.getString("LOCATIONID"));
        encounter.setLatitude(rs.getDoubleObj("DECIMALLATITUDE"));
        encounter.setLongitude(rs.getDoubleObj("DECIMALLONGITUDE"));
        encounter.setVerbatimLocation(rs.getString("VERBATIMLOCALITY"));

        encounter.setSubmitter(getUser(rs));
        encounter.setIndividual(individual);

        return encounter;
    }

    private static SimpleEncounter readSimpleEncounter(final String context,
                                                       final RecordSet rs) throws DatabaseException
    {
        return readSimpleEncounter(readSimpleIndividual(context, rs), rs);
    }


//    private static SinglePhotoVideo readPhoto(final RecordSet rs) throws DatabaseException
//    {
//        //
//        // TODO: Add Keywords
//        //
//        SinglePhotoVideo spv = new SinglePhotoVideo();
//        spv.setDataCollectionEventID(rs.getString("DATACOLLECTIONEVENTID"));
//        spv.setCopyrightOwner(rs.getString("COPYRIGHTOWNER"));
//        spv.setCopyrightStatement(rs.getString("COPYRIGHTSTATEMENT"));
//        spv.setFilename(rs.getString("FILENAME"));
//        spv.setFullFileSystemPath(rs.getString("FULLFILESYSTEMPATH"));
//        spv.setSubmitter(rs.getString("SUBMITTER"));
//
//        return spv;
//    }


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


    public static SimplePhoto getPhoto(final String context,
                                       final SinglePhotoVideo spv)
    {
        return new SimplePhoto(spv.getDataCollectionEventID(), spv.asUrl(context));
    }


    public static SimplePhoto getPhoto(final String context, final RecordSet rs) throws DatabaseException
    {
        String id = rs.getString("DATACOLLECTIONEVENTID");
        if (id == null) {
            return null;
        }

        String url = SinglePhotoVideo.getUrl(context, rs.getString("FULLFILESYSTEMPATH"), rs.getString("FILENAME"));
        return new SimplePhoto(id, url);

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
            String sql;
            sql = "select * from \"USERS\" u"
                    + " LEFT OUTER JOIN mediaasset ma ON ma.id = u.\"USERIMAGEID\""
                    + " WHERE u.\"USERNAME\" = " + StringUtilities.wrapQuotes(username);

            RecordSet rs;
            rs = db.getRecordSet(sql);
            if (rs.next()) {
                return getUser(rs);
            }

            return null;
        }
    }


    private static SimpleUser getUser(final RecordSet rs) throws DatabaseException
    {
        String username = rs.getString("USERNAME");
        if (username == null) {
            return null;
        }

        SimpleUser su = new SimpleUser(rs.getString("USERNAME"), rs.getString("EMAILADDRESS"));

        MediaAsset ma = MediaAsset.valueOf(rs);

        if (ma != null) {
            su.setAvatar(ma.webPath().getFile());
        }

        su.setAffiliation(rs.getString("AFFILIATION"));
        su.setFullName(rs.getString("FULLNAME"));

        return su;
    }


    public static SimpleBeing getStormpathUser(final Account user)
    {
        SimpleUser su = new SimpleUser(user.getUsername(), user.getEmail());

        CustomData data = user.getCustomData();

        su.setAffiliation((String)data.get("affiliation"));
        su.setFullName(user.getFullName());

        return su;
    }
}

package org.ecocean.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdPMF;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.security.Stormpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.util.string.StringUtilities;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.directory.CustomData;

public class SimpleFactory {
    private final static Logger logger = LoggerFactory.getLogger(SimpleFactory.class);
    private final static int MIN_PHOTOS = 8;

    private SimpleFactory() {
        // prevent instantiation
    }


    public static SimpleUser getUser(final String context,
                                     final String username) {
        Shepherd myShepherd = new Shepherd(context);
        User user = myShepherd.getUser(username);

        if (user == null) {
            return null;
        }

        return getUser(user);
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
                photos.add(getPhoto(context, readPhoto(rs)));
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

                    SimplePhoto photo = getPhoto(context, readPhoto(rs));

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


    public static SimpleIndividual getIndividual(final String context,
                                                 final MarkedIndividual mi) throws DatabaseException {

        SimpleIndividual ind = new SimpleIndividual(mi.getIndividualID(), mi.getNickName());
        ind.setSex(mi.getSex());
        ind.setAvatar(mi.getAvatar().asUrl(context));

        return ind;
    }


    public static UserInfo getUserInfo(final String context,
                                       final String configDir,
                                       final String username) throws DatabaseException
    {
        UserInfo userinfo;
        userinfo = new UserInfo(SimpleFactory.getUser(context, username));

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
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                userinfo.addPhoto(getPhoto(context, readPhoto(rs)));
            }

            //
            // Highlighted photos
            //
            sql = sqlRoot
                    + " INNER JOIN \"MEDIATAG_MEDIA\" mtm ON spv.\"DATACOLLECTIONEVENTID\" = mtm.\"DATACOLLECTIONEVENTID_EID\""
                    + whereRoot
                    + " AND mtm.\"NAME_OID\" = 'highlight'";

            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                userinfo.addPhoto(getPhoto(context, readPhoto(rs)));
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

                if (logger.isDebugEnabled()) {
                    logger.debug(sql);
                }
                rs = db.getRecordSet(sql);
                while (rs.next()) {
                    if (userinfo.getPhotos().size() >= MIN_PHOTOS) {
                        break;
                    }

                    userinfo.addPhoto(getPhoto(context, readPhoto(rs)));
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
            // 3) Encounters
            //
            sql = "select e.*, mi.*, spva.* from \"ENCOUNTER\" e"
                    + " inner join \"ENCOUNTER_IMAGES\" ei on e.CATALOGNUMBER = ei.CATALOGNUMBER_OID"
                    + " inner join \"SINGLE_PHOTO_VIDEO\" spv on ei.\"DATACOLLECTIONEVENTID_EID\" = spv.\"DATACOLLECTIONEVENTID\""
                    + " left outer join \"MARKEDINDIVIDUAL\" mi on e.\"INDIVIDUALID\" = mi.\"INDIVIDUALID\""
                    + " left outer join \"SINGLE_PHOTO_VIDEO\" spva on mi.\"AVATAR_DATACOLLECTIONEVENTID_OID\" = spva.\"DATACOLLECTIONEVENTID\""
                    + whereRoot;

            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                SimpleEncounter encounter = getEncounter(context, configDir, readEncounter(rs));
                SimpleIndividual individual = readSimpleIndividual(context, rs);

                encounter.setIndividual(individual);
                userinfo.addEncounter(encounter);
            }

        }

        return userinfo;
    }


    private static SimpleIndividual readSimpleIndividual(final String context,
                                                         final RecordSet rs) throws DatabaseException
    {
        SimpleIndividual ind = new SimpleIndividual(rs.getString("INDIVIDUALID"),
                                                    rs.getString("NICKNAME"));
        ind.setSex(rs.getString("SEX"));

        SinglePhotoVideo spv = readPhoto(rs);
        if (spv != null) {
            ind.setAvatar(spv.asUrl(context));
        }

        return ind;
    }


    private static Encounter readEncounter(final RecordSet rs) throws DatabaseException
    {
        Encounter encounter = new Encounter();

        encounter.setDWCGlobalUniqueIdentifier("GUID");
        encounter.setDateInMilliseconds(rs.getLong("DATEINMILLISECONDS"));
        encounter.setLocationID(rs.getString("LOCATIONID"));
        encounter.setLatitude(rs.getDoubleObj("DECIMALLATITUDE"));
        encounter.setLongitude(rs.getDoubleObj("DECIMALLONGITUDE"));
        encounter.setSubmitterName(rs.getString("SUBMITTERID"));
        encounter.setIndividualID(rs.getString("INDIVIDUALID"));

        return encounter;
    }

    private static SinglePhotoVideo readPhoto(final RecordSet rs) throws DatabaseException
    {
        //
        // TODO: Add Keywords
        //
        SinglePhotoVideo spv = new SinglePhotoVideo();
        spv.setDataCollectionEventID(rs.getString("DATACOLLECTIONEVENTID"));
        spv.setCopyrightOwner(rs.getString("COPYRIGHTOWNER"));
        spv.setCopyrightStatement(rs.getString("COPYRIGHTSTATEMENT"));
        spv.setFilename(rs.getString("FILENAME"));
        spv.setFullFileSystemPath(rs.getString("FULLFILESYSTEMPATH"));
        spv.setSubmitter(rs.getString("SUBMITTER"));

        return spv;
    }


    public static SimpleEncounter getEncounter(final String context, final String configDir, final Encounter encounter)
    {
        SimpleEncounter se = new SimpleEncounter(encounter.getDWCGlobalUniqueIdentifier(),
                                                 encounter.getDateInMilliseconds());

        se.setLocationid(encounter.getLocationID());
        se.setVerbatimLocation(encounter.getLocation());
        se.setLatitude(encounter.getLatitude());
        se.setLongitude(encounter.getLongitude());

        Client client = Stormpath.getClient(configDir);
        AccountList accounts = Stormpath.getAccounts(client, encounter.getSubmitterID());

        if (accounts.getSize() < 1) {
            Shepherd myShepherd = new Shepherd(context);
            User user = myShepherd.getUser(encounter.getSubmitterID());

            if (user != null) {
                se.setSubmitter(getUser(user));
            }
        } else {
            Account account = accounts.iterator().next();
            se.setSubmitter(getStormpathUser(account));
        }

        return se;
    }


    public static SimplePhoto getPhoto(final String context,
                                       final SinglePhotoVideo spv)
    {
        SimplePhoto sp = new SimplePhoto(spv.getDataCollectionEventID(),
                                         spv.asUrl(context));
        return sp;
    }


    public static SimpleUser getUser(final User user)
    {
        SimpleUser su = new SimpleUser(user.getUsername(), user.getEmailAddress());

        if (user.getUserImage() != null) {
            su.setAvatar(user.getUserImage().webPath().getFile());
        }

        su.setAffiliation(user.getAffiliation());
        su.setFullName(user.getFullName());

        return su;
    }


    public static SimpleUser getStormpathUser(final Account user)
    {
        SimpleUser su = new SimpleUser(user.getUsername(), user.getEmail());

        CustomData data = user.getCustomData();

        su.setAffiliation((String)data.get("affiliation"));
        su.setFullName(user.getFullName());

        return su;
    }

    /*
        TODO currently kind of experimental.  the idea is to pass in either ONE or BOTH of a username/email pair and get *some*
        kind of user out, including (optionally) a sort of place holder "unknown" user (which will just return null if returnUnknownUser is false
    */
    //note: this *favors* Stormpath ... is this cool?
    public static SimpleUser getAnyUser(final String context, final String configDir,
                                        final String username, final String email, final boolean returnUnknownUser) {
        SimpleUser user = null;
        if (returnUnknownUser) {
            user = new SimpleUser(null, null);
            user.setAffiliation("Unknown"); //???
            user.setFullName("Unknown");
            //user.setAvatar(SOME GENERIC THING HERE);  //TODO
        }
        if (Util.isEmpty(username) && Util.isEmpty(email)) return user;

        Client client = Stormpath.getClient(configDir);
        if (client != null) {
            HashMap<String, Object> q = new HashMap<String, Object>();
            if (!Util.isEmpty(username)) {
                q.put("username", username);
            } else {
                q.put("email", email);
            }
            AccountList accounts = Stormpath.getAccounts(client, q);
            if (accounts.getSize() > 0) {
                Account account = accounts.iterator().next();
                return getStormpathUser(account);
            }
        }

        Shepherd myShepherd = new Shepherd(context);
        User wu = null;
        if (!Util.isEmpty(username)) {
            wu = myShepherd.getUser(username);
        } else {
            wu = myShepherd.getUserByEmailAddress(email);
        }
        if (wu != null) return getUser(wu);

        //if we fall thru here, we have no user; but may be able to give a *little* info on the UnknownUser
        if (!returnUnknownUser) return null;
        String name = username;
        if (Util.isEmpty(username)) name = email;
        if (!Util.isEmpty(name)) {
            //censor any email address (or email-as-username)
            if ((name.length() > 6) && (name.indexOf("@") > -1)) name = name.substring(0,3) + ".." + name.substring(name.length() - 2);
            user.setFullName(name);
        }
        return user;
    }

}

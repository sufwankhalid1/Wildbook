package org.ecocean.rest;

import java.util.HashMap;

import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;
import org.ecocean.ShepherdPMF;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.User;
import org.ecocean.Util;
import org.ecocean.security.Stormpath;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.util.string.StringUtilities;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.directory.CustomData;

public class SimpleFactory {
    private final static int MIN_PHOTOS = 8;

    private SimpleFactory() {
        // prevent instantiation
    }


    public static SimpleIndividual getIndividual(final String context,
                                                 final String configDir,
                                                 final String id) throws DatabaseException {
        Shepherd myShepherd = new Shepherd(context);
        MarkedIndividual mi = myShepherd.getMarkedIndividual(id);

        if (mi == null) {
            return null;
        }

        return getIndividual(context, configDir, mi);
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


    public static SimpleIndividual getIndividual(final String context,
                                                 final String configDir,
                                                 final MarkedIndividual mi) throws DatabaseException {

        SimpleIndividual ind = new SimpleIndividual(mi.getIndividualID(), mi.getNickName());
        ind.setSex(mi.getSex());

        java.util.Iterator<Encounter> it = mi.getEncounters().iterator();
        while (it.hasNext()) {
            ind.addEncounter(getEncounter(context, configDir, it.next()));
        }

        //
        // Add photos
        //
       try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            String sqlRoot = "SELECT spv.* FROM \"SINGLEPHOTOVIDEO\" spv"
                    + " INNER JOIN \"ENCOUNTER_IMAGES\" ei ON spv.\"DATACOLLECTIONEVENTID\" = ei.\"DATACOLLECTIONEVENTID_EID\""
                    + " INNER JOIN \"MARKEDINDIVIDUAL_ENCOUNTERS\" mie ON mie.\"CATALOGNUMBER_EID\" = ei.\"CATALOGNUMBER_OID\"";
            String mtmJoin = " INNER JOIN \"MEDIATAG_MEDIA\" mtm ON spv.\"DATACOLLECTIONEVENTID\" = mtm.\"DATACOLLECTIONEVENTID_EID\"";
            String whereRoot = " WHERE mie.\"INDIVIDUALID_OID\" = " + StringUtilities.wrapQuotes(ind.getId());

            //
            // Find the profile image
            //
            String sql;
            sql = sqlRoot + mtmJoin + whereRoot
                    + " AND mtm.\"NAME_OID\" = 'profile' LIMIT 1";

            RecordSet rs = db.getRecordSet(sql);
            if (rs.next()) {
                ind.setAvatar(readPhoto(rs).asUrl(context));
            }

            //
            // Find the highlight images for this individual.
            //
            sql = sqlRoot + mtmJoin + whereRoot
                    + " AND mtm.\"NAME_OID\" = 'highlight'";

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                ind.addPhoto(getPhoto(context, readPhoto(rs)));
            }

            //
            // If we are not at our minimum number of photos go ahead
            // and grab the rest at random. Grab the minimum number of photos
            // rather than the minimum minus the number already retrieved so
            // that we can throw out any duplicates. That code is embedded
            // in the addPhoto method of the SimpleIndividual
            //
            if (ind.getPhotos().size() < MIN_PHOTOS) {
                sql = sqlRoot + whereRoot + " LIMIT " + MIN_PHOTOS;

                rs = db.getRecordSet(sql);
                while (rs.next()) {
                    if (ind.getPhotos().size() >= MIN_PHOTOS) {
                        break;
                    }

                    ind.addPhoto(getPhoto(context, readPhoto(rs)));
                }
            }
        }

        return ind;
    }


    public static UserInfo getUserInfo(final String context,
                                       final String username) throws DatabaseException
    {
        UserInfo userinfo;
        userinfo = new UserInfo(SimpleFactory.getUser(context, username));

        //
        // Add:
        // 1) Highlighted photos. (4 random photos if no highlighted one's?)
        // 2) Total photo submission count
        // 3) Encounters
        // 4) Indivduals identified (unique Individuals from Encounters
        // 5) Voyages on
        //
        String sqlRoot = "SELECT spv.* FROM \"SINGLEPHOTOVIDEO\" spv";
        String whereRoot = " WHERE spv.\"SUBMITTER\" = " + StringUtilities.wrapQuotes(username);

        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            String sql;

            //
            // 1) Highlighted Photos
            //
            sql = sqlRoot
                    + " INNER JOIN \"MEDIATAG_MEDIA\" mtm ON spv.\"DATACOLLECTIONEVENTID\" = mtm.\"DATACOLLECTIONEVENTID_EID\""
                    + whereRoot
                    + " AND mtm.\"NAME_OID\" = 'highlight' OR mtm.\"NAME_OID\" = 'profile'";

            RecordSet rs = db.getRecordSet(sql);
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


        }

        return userinfo;
    }

    private static SinglePhotoVideo readPhoto(final RecordSet rs) throws DatabaseException
    {
        //
        // TODO: Add Keywords
        //
        SinglePhotoVideo spv = new SinglePhotoVideo();
        spv.setDataCollectionEventID("DATACOLLECTIONEVENTID");
        spv.setCopyrightOwner(rs.getString("COPYRIGHTOWNER"));
        spv.setCopyrightStatement(rs.getString("COPYRIGHTSTATEMENT"));
        spv.setFilename(rs.getString("FILENAME"));
        spv.setFullFileSystemPath(rs.getString("FULLFILESYSTEMPATH"));
        spv.setSubmitter(rs.getString("SUBMITTER"));

        return spv;
    }


    public static SimpleEncounter getEncounter(final String context, String configDir, final Encounter encounter)
    {
        SimpleEncounter se = new SimpleEncounter(encounter.getDWCGlobalUniqueIdentifier(),
                                                 encounter.getDateInMilliseconds());

        se.setLocationid(encounter.getLocationID());
        se.setVerbatimLocation(encounter.getLocation());
        se.setLatitude(encounter.getLatitude());
        se.setLongitude(encounter.getLongitude());

        encounter.getSubmitterName();
        for (SinglePhotoVideo photo : encounter.getSinglePhotoVideo())
        {
            se.addPhoto(getPhoto(context, photo));
        }

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

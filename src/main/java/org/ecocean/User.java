package org.ecocean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.servlet.ServletUtilities;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

/**
 * <code>User</code> stores information about a contact/user.
 * Examples: photographer, submitter
 * @author Ed Stastny
 */
public class User implements Serializable {
    private static Logger log = LoggerFactory.getLogger(User.class);

    private static final long serialVersionUID = -1261710718629763048L;
    // The user's full name
    private String fullName;
    //Primary email address
    private String emailAddress;
    // User's snail-mail address/location
    private String physicalAddress;
    //Primary phone number
    private String phoneNumber;
    //Organization or project affiliation
    private String affiliation;

    private String userProject;
    private String userStatement;
    private String userURL;
    private Integer userImageID;
    private MediaAsset userImage; // not persisted

    //Misc. information about this user
    private String notes;
    //Date of last update of this record, in ms
    private long dateInMilliseconds;
    private long userID;

    private long lastLogin=-1;

    private String username;
    private String password;
    private String salt;
    private HashMap<String,String> social;
    //String currentContext;


    private boolean acceptedUserAgreement=false;

    private boolean receiveEmails=true;

    //JDOQL required empty instantiator
    public User(){}

    public User(final String fullName, final String emailAddress, final String physicalAddress, final String phoneNumber, final String affiliation, final String notes) {
        setFullName(fullName);
        setEmailAddress(emailAddress);
        setPhysicalAddress(physicalAddress);
        setPhoneNumber(phoneNumber);
        setAffiliation(affiliation);
        setNotes(notes);
        RefreshDate();
        this.lastLogin=-1;
    }

    public User(final String username, final String password){
        setUsername(username);

        salt = ServletUtilities.getSalt().toHex();
        this.password = ServletUtilities.hashAndSaltPassword(password, salt);

        setReceiveEmails(true);
        RefreshDate();
        this.lastLogin=-1;
    }

    public void RefreshDate()
    {
        this.dateInMilliseconds = new Date().getTime();
    }

    public String getFullName()
    {
        return this.fullName;
    }
    public void setFullName (final String fullName)
    {
        this.fullName = fullName;
        RefreshDate();
    }

    public String getEmailAddress()
    {
        return this.emailAddress;
    }
    public void setEmailAddress (final String emailAddress)
    {
        this.emailAddress = emailAddress;
        RefreshDate();
    }

    public String getPhysicalAddress ()
    {
        return this.physicalAddress;
    }
    public void setPhysicalAddress (final String physicalAddress)
    {
        this.physicalAddress = physicalAddress;
        RefreshDate();
    }

    public String getPhoneNumber ()
    {
        return this.phoneNumber;
    }
    public void setPhoneNumber (final String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
        RefreshDate();
    }

    public String getAffiliation ()
    {
        return this.affiliation;
    }
    public void setAffiliation (final String affiliation)
    {
        this.affiliation = affiliation;
        RefreshDate();
    }

    public String getNotes ()
    {
        return this.notes;
    }
    public void setNotes (final String notes)
    {
        this.notes = notes;
        RefreshDate();
    }

    public long getDateInMilliseconds ()
    {
        return this.dateInMilliseconds;
    }

    public long getUserID() {
        return userID;
    }
    public void setUserID(final long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(final String password) {
        this.password = password;
    }

    public void setSalt(final String salt){this.salt=salt;}
    public String getSalt(){return salt;}

    public void setUserProject(final String newProj) {
        userProject = newProj;
    }
    public String getUserProject(){return userProject;}

    public void setUserStatement(final String newState) {
        userStatement = newState;
    }
    public String getUserStatement(){return userStatement;}

    public Integer getUserImageID() {return userImageID;}

    public MediaAsset getUserImage() {return userImage;}

    public synchronized void cacheUserImage(final Database db) {
        if (db == null)
            throw new IllegalArgumentException("null database");

        if (userImageID == null ||
            userImageID == MediaAssetFactory.NOT_SAVED)
            return;

        try {
            userImage = MediaAssetFactory.load(db, userImageID);
        } catch (DatabaseException e) {
            log.error("Error while loading user image " + userImageID + " from db " + db, e);
        }
    }

    public synchronized void setUserImage(final MediaAsset image) {
        if (image == null) {
            userImageID = null;
        } else if (image.getID() == MediaAssetFactory.NOT_SAVED) {
            throw new IllegalArgumentException("Image must be saved prior to set");
        } else {
            userImageID = image.getID();
        }

        userImage = image;
    }

    public void setUserURL(final String newURL) {
        userURL = newURL;
    }
    public String getUserURL(){return userURL;}

    public long getLastLogin(){
        return lastLogin;
    }

    public String getLastLoginAsDateString(){
        if(lastLogin==-1) return null;
        return (new DateTime(this.lastLogin)).toString();
    }

    public void setLastLogin(final long lastie){this.lastLogin=lastie;}

    public boolean getReceiveEmails(){return receiveEmails;}
    public void setReceiveEmails(final boolean receive){this.receiveEmails=receive;}

    public boolean getAcceptedUserAgreement(){return acceptedUserAgreement;}

    public void setAcceptedUserAgreement(final boolean accept){this.acceptedUserAgreement=accept;}


    public String getSocial(final String type) {
        if (social == null) return null;
            return social.get(type);
    }

    public void setSocial(final String type, final String s) {
        if ((s == null) || s.equals("")) {
            unsetSocial(type);
            return;
        }
        if (social == null) social = new HashMap<String,String>();
            social.put(type, s);
    }

    public void setSocial(final String type) {
        unsetSocial(type);
    }

    public void unsetSocial(final String type) {
        if (social == null) return;
        social.remove(type);
    }


    //TODO this needs to be dealt with better.  see: rant about saving usernames from forms
    public static boolean isUsernameAnonymous(final String uname) {
        return ((uname == null) || uname.equals("") || uname.equals("N/A"));
    }

    //public String getCurrentContext(){return currentContext;}
    //public void setCurrentContext(String newContext){currentContext=newContext;}
}

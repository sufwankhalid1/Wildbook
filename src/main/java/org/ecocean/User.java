package org.ecocean;

import java.util.Date;
import java.io.Serializable;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.media.*;

import com.samsix.database.*;

import org.joda.time.DateTime;

import com.stormpath.sdk.account.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Long userImageID;
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

    //String currentContext;


    private boolean acceptedUserAgreement=false;

    private boolean receiveEmails=true;

    //JDOQL required empty instantiator
    public User(){}

    public User(String fullName, String emailAddress, String physicalAddress, String phoneNumber, String affiliation, String notes) {
        setFullName(fullName);
        setEmailAddress(emailAddress);
        setPhysicalAddress(physicalAddress);
        setPhoneNumber(phoneNumber);
        setAffiliation(affiliation);
        setNotes(notes);
        RefreshDate();
        this.lastLogin=-1;
    }

    public User(String username,String password, String salt){
        setUsername(username);
        setPassword(password);
        setSalt(salt);
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
    public void setFullName (String fullName)
    {
        this.fullName = fullName;
        RefreshDate();
    }

    public String getEmailAddress()
    {
        return this.emailAddress;
    }
    public void setEmailAddress (String emailAddress)
    {
        this.emailAddress = emailAddress;
        RefreshDate();
    }

    public String getPhysicalAddress ()
    {
        return this.physicalAddress;
    }
    public void setPhysicalAddress (String physicalAddress)
    {
        this.physicalAddress = physicalAddress;
        RefreshDate();
    }

    public String getPhoneNumber ()
    {
        return this.phoneNumber;
    }
    public void setPhoneNumber (String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
        RefreshDate();
    }

    public String getAffiliation ()
    {
        return this.affiliation;
    }
    public void setAffiliation (String affiliation)
    {
        this.affiliation = affiliation;
        RefreshDate();
    }

    public String getNotes ()
    {
        return this.notes;
    }
    public void setNotes (String notes)
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
    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(String salt){this.salt=salt;}
    public String getSalt(){return salt;}
>>>>>>> User.java: whitespace and slight logic cleanup


    public void setUserProject(String newProj) {
        userProject = newProj;
    }
    public String getUserProject(){return userProject;}

    public void setUserStatement(String newState) {
        userStatement = newState;
    }
    public String getUserStatement(){return userStatement;}

    public Long getUserImageID() {return userImageID;}

    public MediaAsset getUserImage() {return userImage;}

    public synchronized void cacheUserImage(Database db) {
        if (db == null)
            throw new IllegalArgumentException("null database");

        if (userImageID == null ||
            userImageID == MediaAsset.NOT_SAVED)
            return;

        try {
            userImage = MediaAsset.load(db, userImageID);
        } catch (DatabaseException e) {
            log.error("Error while loading user image " + userImageID + " from db " + db, e);
        }
    }

    public synchronized void setUserImage(MediaAsset image) {
        if (image == null) {
            userImageID = null;
        } else if (image.id == MediaAsset.NOT_SAVED) {
            throw new IllegalArgumentException("Image must be saved prior to set");
        } else {
            userImageID = image.id;
        }

        userImage = image;
    }

    public void setUserURL(String newURL) {
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

    public void setLastLogin(long lastie){this.lastLogin=lastie;}

    public boolean getReceiveEmails(){return receiveEmails;}
    public void setReceiveEmails(boolean receive){this.receiveEmails=receive;}

    public boolean getAcceptedUserAgreement(){return acceptedUserAgreement;}

    public void setAcceptedUserAgreement(boolean accept){this.acceptedUserAgreement=accept;}

    //TODO this needs to be dealt with better.  see: rant about saving usernames from forms
    public static boolean isUsernameAnonymous(String uname) {
        return ((uname == null) || uname.equals("") || uname.equals("N/A"));
    }

    //public String getCurrentContext(){return currentContext;}
    //public void setCurrentContext(String newContext){currentContext=newContext;}
}

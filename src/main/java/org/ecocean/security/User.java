package org.ecocean.security;

import java.time.LocalDate;

import org.ecocean.Organization;
import org.ecocean.rest.SimpleUser;
import org.ecocean.servlet.ServletUtilities;
import org.joda.time.DateTime;

public class User {
    private Integer id;
    private String username;
    private String fullName;

    private Organization organization;

    //
    // Not persisted. Only for generating SimpleUsers.
    //
    private String avatar;

    private String email;
    private String physicalAddress;
    private String phoneNumber;
    private LocalDate creationDate;

    private long lastLogin;

    private String hashedPass;
    private String salt;
    private Integer avatarid;
    private String statement;

    private boolean verified = false;
    private boolean acceptedUserAgreement = false;

    public User() {
        // blank constructor for when creating a new user.
    }

    public User(final Integer id,
                final String username,
                final String fullName,
                final String email)
    {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }

    public Integer getUserId() {
        return id;
    }


    public void setUserId(final Integer userid) {
        this.id = userid;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhysicalAddress()
    {
        return this.physicalAddress;
    }

    public void setPhysicalAddress (final String physicalAddress)
    {
        this.physicalAddress = physicalAddress;
    }

    public String getPhoneNumber ()
    {
        return this.phoneNumber;
    }

    public void setPhoneNumber (final String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getHashedPass() {
        return hashedPass;
    }

    public void setSaltAndHashedPass(final String salt, final String hashedPass) {
        this.salt = salt;
        this.hashedPass = hashedPass;
    }

    public void initPassword(final String password) {
        salt = ServletUtilities.getSalt().toHex();
        resetPassword(password);
    }

    public void resetPassword(final String password) {
        this.hashedPass = ServletUtilities.hashAndSaltPassword(password, salt);
    }

    public String getSalt() {
        return salt;
    }


//    public synchronized void cacheUserImage(final Database db) {
//        if (db == null)
//            throw new IllegalArgumentException("null database");
//
//        if (userImageID == null ||
//            userImageID == MediaAssetFactory.NOT_SAVED)
//            return;
//
//        try {
//            userImage = MediaAssetFactory.load(db, userImageID);
//        } catch (DatabaseException e) {
//            log.error("Error while loading user image " + userImageID + " from db " + db, e);
//        }
//    }
//
//    public synchronized void setUserImage(final MediaAsset image) {
//        if (image == null) {
//            userImageID = null;
//        } else if (image.getID() == MediaAssetFactory.NOT_SAVED) {
//            throw new IllegalArgumentException("Image must be saved prior to set");
//        } else {
//            userImageID = image.getID();
//        }
//
//        userImage = image;
//    }


    public long getLastLogin(){
        return lastLogin;
    }

    public String getLastLoginAsDateString(){
        if (lastLogin == 0) {
            return null;
        }

        return (new DateTime(this.lastLogin)).toString();
    }

    public void setLastLogin(final long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean getAcceptedUserAgreement() {
        return acceptedUserAgreement;
    }

    public void setAcceptedUserAgreement(final boolean accept) {
        this.acceptedUserAgreement=accept;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public SimpleUser toSimple()
    {
        SimpleUser user = new SimpleUser(id, username, fullName);
        if (organization != null) {
            user.setAffiliation(organization.getName());
        }
        user.setAvatar(avatar);
        return user;
    }

    public Integer getAvatarid() {
        return avatarid;
    }

    public void setAvatarid(final Integer avatarid) {
        this.avatarid = avatarid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(final String statement) {
        this.statement = statement;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }
}

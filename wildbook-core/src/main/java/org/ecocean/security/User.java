package org.ecocean.security;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Organization;
import org.ecocean.location.GeoLocation;
import org.ecocean.mmutil.StringUtilities;
import org.ecocean.rest.SimpleUser;
import org.ecocean.util.DateUtils;
import org.ecocean.util.WildbookUtils;

public class User {
    private Integer id;
    private String username;
    private String fullName;

    private Organization organization;

    private GeoLocation geoLocation;

    private String email;
    private String physicalAddress;
    private String phoneNumber;
    private LocalDate creationDate;

    private long lastLogin;

    private String hashedPass;
    private String salt;
    private String statement;

    private boolean verified = false;
    private boolean acceptedUserAgreement = false;
    private String avatarPath;

    private String prtoken;
    private LocalDateTime prtimestamp;

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

    public static User create(final String username,
                              final String fullName,
                              final String email) {
        return create(username, fullName, email, randomPass());
    }

    public static User create(final String username,
                              final String fullName,
                              final String email,
                              final String password) {
        User user = new User(null, username, fullName, email);
        user.initPassword(password);
        return user;
    }

    private static String randomPass() {
        return UUID.randomUUID().toString();
    }

    public Integer getId() {
        return id;
    }


    public void setId(final Integer id) {
        this.id = id;
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

    public void initPassword() {
        initPassword(randomPass());
    }

    private void initPassword(final String password) {
        salt = WildbookUtils.getSalt().toHex();
        resetPassword(password);
    }

    public void resetPassword(final String password) {
        this.hashedPass = WildbookUtils.hashAndSaltPassword(password, salt);
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
        return DateUtils.epochMilliSecToString(this.lastLogin);
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

        if (geoLocation != null) {
            user.setGeoLocation(getGeoLocation());
        }

        user.setAvatar(getAvatar());
        user.setStatement(getStatement());
        return user;
    }

    public String getAvatar() {
        //
        // TODO:
        // This will probably be a relative path. So the client will have to know if it starts
        // with "http" to just make the request or else to tack on the static avatar path. Or
        // should the server tack that on? Maybe the server should do it? I guess that's how
        // we are doing mediaasset with the assetstore and all.
        //
        if (avatarPath != null) {
            return avatarPath;
        }

        String emails;

        if (! StringUtils.isBlank(email)) {
            //
            // Return 80x80 sized gvatar. They default to 80x80 but can be requested up to 2048x2048.
            // Though most users will have used a small image.
            // Feel free to change if you want it bigger as all the code on the browser side should
            // be sized to fit it's use anyway.
            // NOTE: d=identicon makes default (when not set by user) be those crazy (unique) geometric shapes, rather than the gravatar logo
            //         - https://en.wikipedia.org/wiki/Identicon
            //
            emails = email.trim().toLowerCase();
        } else {
            emails = "thisuserneedsanemail";
        }

        return "https://www.gravatar.com/avatar/"
        + StringUtilities.getHashOf(emails)
        + "?s=80&d=identicon";
    }

    public void setAvatarPath(final String avatar) {
        this.avatarPath = avatar;
    }

    public String getAvatarPath() {
        return avatarPath;
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

    public String getPrtoken() {
        return prtoken;
    }

    public void setPrtoken(final String prtoken) {
        this.prtoken = prtoken;
    }

    public LocalDateTime getPrtimestamp() {
        return prtimestamp;
    }

    public void setPrtimestamp(final LocalDateTime prtimestamp) {
        this.prtimestamp = prtimestamp;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}

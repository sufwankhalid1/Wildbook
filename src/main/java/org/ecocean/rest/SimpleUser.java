package org.ecocean.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ecocean.mmutil.StringUtilities;

public class SimpleUser {
    private final String username;
    private String fullName;
    private String affiliation;
    private String avatar;

    //
    // NOTE: Do not create a getter for email because
    // we don't want to expose that to the web. Used here for
    // ID purposes only as that is what stormpath uses.
    //
    private final String email;

    public SimpleUser(final String username,
                      final String email)
    {
        this.username = username;
        this.email = email;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof SimpleUser)) {
            return false;
        }

        SimpleUser other = (SimpleUser) obj;

        return new EqualsBuilder()
            .append(username, other.username)
            .append(email, other.email)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
            .append(username)
            .append(email)
            .toHashCode();
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDisplayName() {
        String display;
        if (StringUtils.isBlank(fullName)) {
            //
            // Check for email and strip out the end part to secure from seeing the email address.
            // This way we only show the stuff before the @. If this is on the client side, then the
            // username will have the full email address but only astute people will be able to find it.
            //
            int index = username.indexOf("@");
            if (index >= 0) {
                display = username.substring(0, index);
            }

            display = username;
        } else {
            display = fullName;
        }


        if (StringUtils.isBlank(affiliation)) {
            return display;
        }

        return display + " / " + affiliation;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getAvatar() {
        if (avatar != null) {
            return avatar;
        }

        if (email == null) {
            return null;
        }

        //
        // Return 48x48 sized gravatar. They default to 80x80 and can be requested up to 2048x2048.
        // Though most users will have used a small image.
        // Feel free to change if you want it bigger as all the code on the browser side should
        // be sized to fit it's use anyway.
        //
        return "http://www.gravatar.com/avatar/"
            + StringUtilities.getHashOf(email.trim().toLowerCase())
            + "?s=48";
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

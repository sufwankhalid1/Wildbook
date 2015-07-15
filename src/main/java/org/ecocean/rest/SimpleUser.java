package org.ecocean.rest;

import org.apache.commons.lang3.StringUtils;

public class SimpleUser {
    public String username;
    public String fullName;
    public String affiliation;
    public String avatar;

    public SimpleUser(final String username)
    {
        this.username = username;
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
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

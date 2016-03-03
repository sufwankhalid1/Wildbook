package org.ecocean.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ecocean.Species;
import org.ecocean.location.UserLocation;

public class SimpleUser implements SimpleBeing {
    private static final Species homo_sapien = new Species("homo_sapien", "Homo sapiens");

    private Integer id;
    private String username;
    private String fullName;
    private String affiliation;
    private String statement;
    private String avatar;

    private UserLocation userlocation;

    public SimpleUser() {
        // For deserialization.
    }

    public SimpleUser(final Integer id,
                      final String username,
                      final String fullName)
    {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(final String affiliation) {
        this.affiliation = affiliation;
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

    public UserLocation getUserlocation() {
        return userlocation;
    }

    public void setUserlocation(final UserLocation userlocation) {
        this.userlocation = userlocation;
    }

    //===========================
    //  SimpleBeing interface
    //===========================

    /* (non-Javadoc)
     * @see org.ecocean.rest.SimpleBeing#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        String displayName;
        if (StringUtils.isBlank(fullName)) {
            displayName = username;
        } else {
            displayName = fullName;
        }

        if (affiliation != null) {
            return displayName + " (" + affiliation + ")";
        }

        return displayName;
    }

    /* (non-Javadoc)
     * @see org.ecocean.rest.SimpleBeing#getAvatar()
     */
    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public Species getSpecies() {
        return homo_sapien;
    }


    //===========================
    //  Object interface
    //===========================

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof SimpleUser)) {
            return false;
        }

        SimpleUser other = (SimpleUser) obj;

        return new EqualsBuilder()
            .append(id, other.id)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
            .append(id)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return getUsername() + ": " + getDisplayName();
    }
}

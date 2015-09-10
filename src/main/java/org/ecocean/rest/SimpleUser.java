package org.ecocean.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ecocean.mmutil.StringUtilities;

public class SimpleUser implements SimpleBeing {
    private final Integer id;
    private final String username;
    private final String fullName;
    private String affiliation;
    private String statement;
    private String avatar;

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

    //
    // TODO: I think this should this be stored as a separate link to
    // to an organization table. That way we can have all kinds of info
    // about the organization. The user may be affiliated with more than
    // one org. In addition, the orgs should probably also be who can
    // create surveys.
    //
    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(final String affiliation) {
        this.affiliation = affiliation;
    }

    public void setAvatar(final String avatar, final String email) {
        if (StringUtils.isBlank(avatar) && ! StringUtils.isBlank(email)) {
            //
            // Return 80x80 sized gravatar. They default to 80x80 but can be requested up to 2048x2048.
            // Though most users will have used a small image.
            // Feel free to change if you want it bigger as all the code on the browser side should
            // be sized to fit it's use anyway.
            // NOTE: d=identicon makes default (when not set by user) be those crazy (unique) geometric shapes, rather than the gravatar logo
            //         - https://en.wikipedia.org/wiki/Identicon
            //
            this.avatar = "http://www.gravatar.com/avatar/"
                    + StringUtilities.getHashOf(email.trim().toLowerCase())
                    + "?s=80&d=identicon";
        } else {
            this.avatar = avatar;
        }
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(final String statement) {
        this.statement = statement;
    }


    //===========================
    //  SimpleBeing interface
    //===========================

    /* (non-Javadoc)
     * @see org.ecocean.rest.SimpleBeing#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        if (StringUtils.isBlank(fullName)) {
            return username;
        }

        return fullName;
    }

    /* (non-Javadoc)
     * @see org.ecocean.rest.SimpleBeing#getAvatar()
     */
    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public String getSpecies() {
        return "human";
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

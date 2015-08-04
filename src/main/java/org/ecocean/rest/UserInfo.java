package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    private final SimpleUser user;
    private final List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();
    private final List<SimplePhoto> photos = new ArrayList<SimplePhoto>();

    public UserInfo(final SimpleUser user)
    {
        this.user = user;
    }

    public SimpleUser getUser()
    {
        return user;
    }

    public List<SimpleEncounter> getEncounters() {
        return encounters;
    }

    public List<SimplePhoto> getPhotos() {
        return photos;
    }
}

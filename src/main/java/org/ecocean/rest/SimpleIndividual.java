package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;


class SimpleIndividual
{
    private String id;
    private String nickname;
    private String sex;
    private final List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();
    private final List<SimplePhoto> photos = new ArrayList<SimplePhoto>();

    //
    // TODO: Representative thumbnail needs to be better defined?
    //
//    private String thumbnailUrl;

    public SimpleIndividual()
    {
        // for deserialization
    }

    public SimpleIndividual(final String id,
                            final String nickname)
    {
        this.setId(id);
        this.setNickname(nickname);
    }


    public void setSex(final String sex)
    {
        this.sex = sex;
    }


    public String getSex() {
        return sex;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void addPhoto(final SimplePhoto photo) {
        photos.add(photo);
    }

    public List<SimplePhoto> getPhotos() {
        return photos;
    }

    public void addEncounter(final SimpleEncounter encounter) {
        encounters.add(encounter);
    }

    public List<SimpleEncounter> getEncounters() {
        return encounters;
    }


//    public void setThumbnail(final String thumbnailUrl)
//    {
//        this.thumbnailUrl = thumbnailUrl;
//    }
}
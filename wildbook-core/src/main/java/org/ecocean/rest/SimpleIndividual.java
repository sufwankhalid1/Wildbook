package org.ecocean.rest;

import org.ecocean.Individual;
import org.ecocean.Species;


public class SimpleIndividual implements SimpleBeing
{
    private Integer id;
    private String alternateId;
    private boolean identified = false;
    private Species species;
    private String nickname;
    private String sex;
    private String bio;
    private SimplePhoto avatarFull;


    public SimpleIndividual()
    {
        // for deserialization
    }

    public SimpleIndividual(final Integer id,
                            final String nickname)
    {
        this.id = id;
        this.setNickname(nickname);
    }


    public void setSex(final String sex)
    {
        this.sex = sex;
    }


    public String getSex() {
        return sex;
    }


    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }


    public void setSpecies(final Species species) {
        this.species = species;
    }


    @Override
    public String getDisplayName() {
        return Individual.getDisplayName(nickname, alternateId);
    }


    @Override
    public String getAvatar() {
        if (avatarFull != null) {
            return avatarFull.getThumbUrl();
        }
        return null;
    }

    @Override
    public Species getSpecies() {
        return species;
    }
//
//    public void setAvatar(final String avatar) {
//        // ignore. simply so jackson doesn't barf when tryng to deserialize
//    }

    public String getAlternateId() {
        return alternateId;
    }

    public void setAlternateId(final String alternateId) {
        this.alternateId = alternateId;
    }

    public boolean isIdentified() {
        return identified;
    }

    public void setIdentified(boolean identified) {
        this.identified = identified;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public SimplePhoto getAvatarFull() {
        return avatarFull;
    }

    public void setAvatarFull(final SimplePhoto photo) {
        avatarFull = photo;
    }
}
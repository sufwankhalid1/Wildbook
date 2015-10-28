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
    private String avatar;


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
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }

    @Override
    public Species getSpecies() {
        return species;
    }

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
}
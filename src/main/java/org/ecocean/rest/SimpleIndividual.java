package org.ecocean.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.ecocean.Global;


public class SimpleIndividual implements SimpleBeing
{
    private Integer id;
    private String alternateId;
    private String species;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }


    public void setSpecies(final String species) {
        this.species = species;
    }


    @Override
    public String getDisplayName() {
        String name;
        if (StringUtils.isBlank(nickname)) {
            name = Global.INST.getAppResources().getString("individuals.unnamed.nickname", "[Unnamed]");
        } else {
            name = nickname;
        }

        if (StringUtils.isBlank(alternateId)) {
            return name;
        }

        return name + " (" + alternateId + ")";
    }


    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String getSpecies() {
        return species;
    }

    public String getSpeciesDisplayName() {
        if (StringUtils.isBlank(species)) {
            return "Individual";
        }

        return WordUtils.capitalize(species.replace("_", " "));
    }

    public String getAlternateId() {
        return alternateId;
    }

    public void setAlternateId(final String alternateId) {
        this.alternateId = alternateId;
    }
}
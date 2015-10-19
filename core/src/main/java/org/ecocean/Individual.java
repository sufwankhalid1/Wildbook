package org.ecocean;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.rest.SimpleIndividual;


public class Individual {
    private Integer id;
    private String alternateId;
    private Species species;
    private String nickname;
    private String sex;
    private Integer avatarid;
    //
    // Not persisted. Only for generating SimpleUsers.
    //
    private String avatar;


    public Individual()
    {
        // for deserialization
    }

    public Individual(final Integer id,
                      final Species species,
                      final String nickname)
    {
        this.id = id;
        this.species = species;
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

    public String getDisplayName() {
        return getDisplayName(nickname, alternateId);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }

    public Species getSpecies() {
        return species;
    }

    public String getAlternateId() {
        return alternateId;
    }

    public void setAlternateId(final String alternateId) {
        this.alternateId = alternateId;
    }

    public Integer getAvatarid() {
        return avatarid;
    }

    public void setAvatarid(final Integer avatarid) {
        this.avatarid = avatarid;
    }

    public static String getDisplayName(final String nickname, final String alternateId) {
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

    public SimpleIndividual toSimple()
    {
        SimpleIndividual simple = new SimpleIndividual(id, nickname);
        simple.setAlternateId(alternateId);
        simple.setAvatar(avatar);
        simple.setSex(sex);
        simple.setSpecies(species);
        return simple;
    }
}
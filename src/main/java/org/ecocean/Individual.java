package org.ecocean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.ecocean.rest.SimpleIndividual;


public class Individual {
    private Integer id;
    private String alternateId;
    private String species;
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

    public void setSpecies(final String species) {
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
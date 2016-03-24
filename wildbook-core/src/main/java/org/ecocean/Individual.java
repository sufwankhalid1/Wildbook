package org.ecocean;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimplePhoto;

public class Individual {
    private Integer id;
    private String alternateId;
    private boolean identified = false;
    private Species species;
    private String nickname;
    private String sex;
    private SimplePhoto avatarFull;
    private String bio;


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

    public String getLongDisplayName() {
        if (species != null) {
            return getDisplayName() + " - " + species.getName();
        }
        return getDisplayName();
    }

    //
    // Need this to be like SimpleIndividual so that we can
    // pass the values back and forth.
    //
    public String getAvatar() {
        if (avatarFull != null) {
            return avatarFull.getThumbUrl();
        }
        return null;
    }

    public SimplePhoto getAvatarFull() {
        return avatarFull;
    }

    public void setAvatarFull(final SimplePhoto avatar) {
        this.avatarFull = avatar;
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
        simple.setAvatarFull(avatarFull);
        simple.setSex(sex);
        simple.setSpecies(species);
        simple.setBio(bio);
        simple.setIdentified(identified);
        return simple;
    }

    public boolean isIdentified() {
        return identified;
    }

    public void setIdentified(final boolean identified) {
        this.identified = identified;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(final String bio) {
        this.bio = bio;
    }
}
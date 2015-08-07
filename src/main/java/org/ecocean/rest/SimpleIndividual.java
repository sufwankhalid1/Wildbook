package org.ecocean.rest;

import org.apache.commons.lang3.StringUtils;


class SimpleIndividual implements SimpleBeing
{
    private String id;
    private String nickname;
    private String sex;
    private String avatar;


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

    public void setId(final String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }


    @Override
    public String getDisplayName() {
        if (! StringUtils.isBlank(nickname)) {
            return nickname + " (" + id + ")";
        }

        return id;
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
        //
        // TODO: Put this in the db. For now I'm hard-coding everything
        // to humpbacks.
        //
        return "humpback_whale";
    }
}
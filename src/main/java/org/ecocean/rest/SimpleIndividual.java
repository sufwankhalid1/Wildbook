package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

class SimpleIndividual
{
    private String id;
    private String nickname;
    private String sex;
    private String avatar;
    private final List<SimplePhoto> photos = new ArrayList<SimplePhoto>();


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
        for (SimplePhoto foto : photos) {
            if (foto.getId().equals(photo.getId())) {
                // don't add the same photo twice
                return;
            }
        }

        photos.add(photo);
    }

    public List<SimplePhoto> getPhotos() {
        return photos;
    }

    public String getAvatar() {
        if (avatar != null) {
            return avatar;
        }

        if (photos.size() > 0) {
            return photos.get(0).getUrl();
        }

        return null;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
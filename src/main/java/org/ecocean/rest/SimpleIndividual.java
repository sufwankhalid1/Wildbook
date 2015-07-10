package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;


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


    public static SimpleIndividual fromMarkedIndividual(final MarkedIndividual mi, final String context)
    {
        SimpleIndividual ind = new SimpleIndividual(mi.getIndividualID(), mi.getNickName());
        ind.setSex(mi.getSex());
        //
        // TODO: expose and fix thumbnail path if needed.
        //
//        ind.setThumbnail(mi.getThumbnail());

        java.util.Iterator<Encounter> it = mi.getEncounters().iterator();
        while (it.hasNext()) {
            Encounter enc = it.next();
            ind.encounters.add(SimpleEncounter.fromEncounter(enc, context));
        }

        //
        // TODO: Have photos for individual be chosen from the "selected" or "highlighted"
        // photos via a tag or whatever method we want the users to specify which are the best
        // images. For now I'm just going to grab 8 or so images from all the encounters, grabbing
        // the top images from each.
        //
        int count = 0;
        int index = 0;
        final int MAX_PHOTOS = 8;
        for (SimpleEncounter encounter : ind.encounters) {
            int lastCount = Integer.MAX_VALUE;
            while (count < MAX_PHOTOS && count != lastCount) {
                lastCount = count;
                if (encounter.getPhotos().size() > index) {
                    ind.photos.add(encounter.getPhotos().get(index));
                    count++;
                }
            }
        }

        return ind;
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

    public List<SimplePhoto> getPhotos() {
        return photos;
    }

    public List<SimpleEncounter> getEncounters() {
        return encounters;
    }


//    public void setThumbnail(final String thumbnailUrl)
//    {
//        this.thumbnailUrl = thumbnailUrl;
//    }
}
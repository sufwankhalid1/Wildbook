package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.ShepherdPMF;
import org.ecocean.Shepherd;
import org.ecocean.Util;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.Point;
import org.ecocean.Encounter;
import org.ecocean.MarkedIndividual;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.survey.SurveyTrack;
import org.ecocean.security.Stormpath;
import org.ecocean.media.MediaSubmission;
import org.ecocean.media.MediaTag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import javax.jdo.*;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.client.Client;

@RestController
@RequestMapping(value = "/obj/surveytrack")
public class SurveyTrackController
{

    public PersistenceManager getPM(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
                return myShepherd.getPM();
    }

/*  NOT sure if we ever need to save a track on its own?  see Survey appendTracks REST call
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<SurveyTrack> save(final HttpServletRequest request,
                     @RequestBody @Valid final Survey survey) {
        System.out.println(survey);
                getPM(request).makePersistent(survey);
        return new ResponseEntity<Survey>(survey, HttpStatus.OK);
    }
*/


    @RequestMapping(value = "/appendPoints/{surveyTrackId}", method = RequestMethod.POST)
    public ResponseEntity<SurveyTrack> appendPoints(final HttpServletRequest request,
                                                    @RequestBody List<Point> points,
                                                    @PathVariable("surveyTrackId") final int surveyTrackId) {

        PersistenceManager pm = getPM(request);
        SurveyTrack track = null;
        try {
          track = (SurveyTrack) pm.getObjectById(SurveyTrack.class, surveyTrackId);
        } catch (Exception ex) {
        }

        if (track == null) {
            return new ResponseEntity<SurveyTrack>(track, HttpStatus.NOT_FOUND);
        } else {
            List<Point> prevPoints = track.getPoints();
            /// TODO do we want also a SET (reset, not append) points?
            if (prevPoints == null) prevPoints = new ArrayList<Point>();
            prevPoints.addAll(points);
            track.setPoints(prevPoints);
            pm.makePersistent(track);
            return new ResponseEntity<SurveyTrack>(track, HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/appendMedia/{surveyTrackId}", method = RequestMethod.POST)
    public ResponseEntity<SurveyTrack> appendMedia(final HttpServletRequest request,
                                                    @RequestBody List<SinglePhotoVideo> media,
                                                    @PathVariable("surveyTrackId") final int surveyTrackId) {

        PersistenceManager pm = getPM(request);
        SurveyTrack track = null;
        try {
          track = (SurveyTrack) pm.getObjectById(SurveyTrack.class, surveyTrackId);
        } catch (Exception ex) {
        }

        if (track == null) {
            return new ResponseEntity<SurveyTrack>(track, HttpStatus.NOT_FOUND);
        } else {
            List<SinglePhotoVideo> prevMedia = track.getMedia();
            if (prevMedia == null) prevMedia = new ArrayList<SinglePhotoVideo>();

            //for some reason, media does not get populated such that when tag is persisted, it creates all new SPVs.. grrr wtf.. explicitely loading them fixes this
            /////////prevMedia.addAll(media);
            for (SinglePhotoVideo s : media) {
                SinglePhotoVideo obj = ((SinglePhotoVideo) (pm.getObjectById(pm.newObjectIdInstance(SinglePhotoVideo.class, s.getDataCollectionEventID()), true)));
                if (obj != null) prevMedia.add(obj);
            }

            track.setMedia(prevMedia);
            pm.makePersistent(track);
            return new ResponseEntity<SurveyTrack>(track, HttpStatus.OK);
        }
    }


    //a "voyage" is based loosely upon a single SurveyTrack -- just with lots of other junk added.  for the voyage page specifically
    @RequestMapping(value = "/get/voyage/{id}", method = RequestMethod.GET)
    public HashMap<String,Object> getVoyage(final HttpServletRequest request,
                                            @PathVariable("id") final int id) throws DatabaseException {
        return getVoyageObj(request, id);
    }

    //this version allows info tailored to a specific match
    @RequestMapping(value = "/get/voyage/{id}/{matchMediaId}/{matchEncounterId}/{encounterMediaOffset}", method = RequestMethod.GET)
    public HashMap<String,Object> getVoyage(final HttpServletRequest request,
                                            @PathVariable("id") final int id,
                                            @PathVariable("matchMediaId") final String matchMediaId,
                                            @PathVariable("matchEncounterId") final String matchEncounterId,
                                            @PathVariable("encounterMediaOffset") final int encounterMediaOffset
                                           ) throws DatabaseException {
        PersistenceManager pm = getPM(request);
        String context = ServletUtilities.getContext(request);
        HashMap<String,Object> obj = getVoyageObj(request, id);
        //List<String> err = new ArrayList<String>();

        SinglePhotoVideo testSpv = null;
        try {
            testSpv = (SinglePhotoVideo) pm.getObjectById(SinglePhotoVideo.class, matchMediaId);
        } catch (Exception ex) {
            obj.put("error", "could not find match image with id " + matchMediaId);
            return obj;
        }

        Encounter enc = null;
        try {
            enc = (Encounter) pm.getObjectById(Encounter.class, matchEncounterId);
        } catch (Exception ex) {
            obj.put("error", "could not find match encounter with id " + matchEncounterId);
            return obj;
        }

        List<SinglePhotoVideo> spvs = enc.getImages();
        if ((spvs == null) || (spvs.size() < 1)) {
            obj.put("error", "no images on encounter " + matchEncounterId);
            return obj;
        }
        int offset = encounterMediaOffset;
        if (offset >= spvs.size()) offset = 0;

        MarkedIndividual ind = null;
        try {
            ind = (MarkedIndividual) pm.getObjectById(MarkedIndividual.class, enc.getIndividualID());
        } catch (Exception ex) {
            //we dont care (or do we?) if we dont have an individual -- would we ever even match against an unknown encounter? i guess.
        }

        //now we should have everything we need
////TODO i18n
        SurveyTrack track = (SurveyTrack)obj.get("surveyTrack");
        Long trackTime = track.startTime();
        Client client = Stormpath.getClient(ServletUtilities.getConfigDir(request));
        HashMap<String,Object> match = new HashMap<String,Object>();
        if (ind != null) match.put("individualID", ind.getIndividualID());
        match.put("testImage", SimpleFactory.getPhoto(context, testSpv));
        if (trackTime == null) {
            match.put("testCaption", "Your photo taken from your " + track.getName() + " voyage.");
        } else {
            match.put("testCaption", "Your photo taken from your " + track.getName() + " voyage on " + trackTime + ".");
        }
        match.put("matchImage", SimpleFactory.getPhoto(context, spvs.get(offset)));

        String encUsername = enc.getSubmitterID();
        if ("N/A".equals(encUsername)) encUsername = null; //grrrr
        SimpleUser photoUser = SimpleFactory.getAnyUser(context, ServletUtilities.getConfigDir(request), encUsername, enc.getSubmitterEmail(), true);
        String mcaption = "Photo taken by " + photoUser.getFullName() + " in " + enc.getYear();
        if (!Util.isEmpty(enc.getLocationCode())) mcaption += " in " + enc.getLocationCode();
        match.put("matchCaption", mcaption + ".");
        if (ind == null) {
            match.put("link", "unknown individual");
        } else {
            String name = ind.getIndividualID();
            if (!ind.getNickName().equals("Unassigned")) name = ind.getNickName() + " (" + name + ")";
            match.put("link", "<a href=\"/individual/" + ind.getIndividualID() + "\">" + name + "</a>");
        }
        
        obj.put("match", match);

        return obj;
    }

    private HashMap<String,Object> getVoyageObj(HttpServletRequest request, int id) {
        if (id < 1) return null;
        String context = "context0";
        context = ServletUtilities.getContext(request);
        PersistenceManager pm = getPM(request);
        SurveyTrack track = null;
        try {
            track = (SurveyTrack) pm.getObjectById(SurveyTrack.class, id);
        } catch (Exception ex) {
        }
        HashMap<String,Object> obj = new HashMap<String,Object>();
        obj.put("surveyTrack", track);
        if (track != null) {
            HashMap<SinglePhotoVideo,List<String>> tags = MediaTag.getTags(track.getMedia());
            List<HashMap> media = new ArrayList<HashMap>();  //will be sorted based on sources
            List<MediaSubmission> sources = MediaSubmission.findMediaSources(track.getMedia(), context);
            if (sources.size() > 0) {
                //obj.put("sources", sources);  //do we ever need this in rest response???
                List<SimpleUser> contrib = new ArrayList<SimpleUser>();
                for (MediaSubmission m : sources) {
                    //note: (in theory) there should be a stormpath user for every mediasubmission; so we should get it via username or email
                    //TODO prevent duplicates
                    contrib.add(SimpleFactory.getAnyUser(context, ServletUtilities.getConfigDir(request), m.getUsername(), m.getEmail(), true));
                    for (SinglePhotoVideo spv : m.getMedia()) {
                        if ((track.getMedia() != null) && track.getMedia().contains(spv)) {
                            HashMap h = new HashMap();
                            System.out.println(SimpleFactory.getPhoto(context, spv));
                            h.put("image", SimpleFactory.getPhoto(context, spv));
                            h.put("mediaSubmissionSource", m.getId());
                            if (tags.get(spv) != null) h.put("tags", tags.get(spv));
                            media.add(h);
                        }
                    }
                }
                obj.put("contributors", contrib);
            }
            obj.put("media", media);
        }

        return obj;
    }

/*
    @RequestMapping(value = "/get/sources", method = RequestMethod.POST)
    public List<MediaSubmission> getSources(final HttpServletRequest request,
                                            @RequestBody List<SinglePhotoVideo> media) throws DatabaseException {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        return MediaSubmission.findMediaSources(media, context);
    }
*/



}

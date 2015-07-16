package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.ShepherdPMF;
import org.ecocean.Shepherd;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.Point;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.survey.SurveyTrack;
import org.ecocean.media.MediaSubmission;
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
    public HashMap<String,Object> getSources(final HttpServletRequest request,
                                            @PathVariable("id") final int id) throws DatabaseException {
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
/*
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo();
        Database db = new Database(ci);
        String sql = "SELECT \"DATACOLLECTIONEVENTID_EID\" AS mid FROM \"SURVEYTRACK_MEDIA\" WHERE \"ID_OID\"=" + id;
System.out.println(sql);
        RecordSet rs = db.getRecordSet(sql);
        List<SinglePhotoVideo> media = new ArrayList<SinglePhotoVideo>();
        while (rs.next()) {
            SinglePhotoVideo spv = new SinglePhotoVideo();
            spv.setDataCollectionEventID(rs.getString("mid"));
            media.add(spv);
        }
        db.release();
        return MediaSubmission.findMediaSources(media, context);
*/
        if (track != null) obj.put("sources", MediaSubmission.findMediaSources(track.getMedia(), context));
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

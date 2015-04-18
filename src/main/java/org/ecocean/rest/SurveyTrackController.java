package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.ShepherdPMF;
import org.ecocean.Shepherd;
import org.ecocean.SinglePhotoVideo;
import org.ecocean.Point;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.survey.SurveyTrack;
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
					prevMedia.addAll(media);
					track.setMedia(prevMedia);
					pm.makePersistent(track);
        	return new ResponseEntity<SurveyTrack>(track, HttpStatus.OK);
				}
    }

}

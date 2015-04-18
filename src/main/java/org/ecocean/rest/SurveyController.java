package org.ecocean.rest;

import javax.validation.Valid;

import org.ecocean.survey.Survey;
import org.ecocean.survey.SurveyTrack;
import org.ecocean.Shepherd;
import org.ecocean.servlet.ServletUtilities;
//import org.ecocean.SinglePhotoVideo;
//import org.ecocean.Point;

import javax.jdo.*;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping(value = "/obj/survey")
public class SurveyController {

//    private final UserService userService;
//
//    @Inject
//    public UserController(final UserService userService) {
//        this.userService = userService;
//    }

		public PersistenceManager getPM(final HttpServletRequest request) {
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
				return myShepherd.getPM();
		}

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ResponseEntity<List<Survey>> save(final HttpServletRequest request) {
				PersistenceManager pm = getPM(request);
				Extent ext = pm.getExtent(Survey.class);
				Query q = pm.newQuery(ext);
        ArrayList all = new ArrayList((Collection) q.execute());
/*
    Extent encClass = pm.getExtent(Encounter.class, true);
    Query acceptedEncounters = pm.newQuery(encClass);
    try {
      c = (Collection) (acceptedEncounters.execute());
      //ArrayList list = new ArrayList(c);
      Iterator it = c.iterator();
      return it;
*/
        return new ResponseEntity<List<Survey>>(all, HttpStatus.OK);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<Survey> save(final HttpServletRequest request,
                     @RequestBody @Valid final Survey survey) {
				getPM(request).makePersistent(survey);
        return new ResponseEntity<Survey>(survey, HttpStatus.OK);
    }


    @RequestMapping(value = "/appendTrack/{surveyId}", method = RequestMethod.POST)
    public ResponseEntity<Survey> appendTrack(final HttpServletRequest request,
                            @RequestBody @Valid final SurveyTrack track,
                            @PathVariable("surveyId") final int surveyId) {

				PersistenceManager pm = getPM(request);
        Survey survey = null;
        try {
          survey = (Survey) pm.getObjectById(Survey.class, surveyId);
        } catch (Exception ex) {
				}

				if (survey == null) {
        	return new ResponseEntity<Survey>(survey, HttpStatus.NOT_FOUND);
				} else {
					List<SurveyTrack> tracks = survey.getTracks();
					if (tracks == null) tracks = new ArrayList<SurveyTrack>();
					tracks.add(track);
					survey.setTracks(tracks);
					pm.makePersistent(survey);
        	return new ResponseEntity<Survey>(survey, HttpStatus.OK);
				}
    }
}


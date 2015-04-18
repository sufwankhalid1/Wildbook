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

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping(value = "/survey")
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

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public void save(final HttpServletRequest request,
                     @RequestBody @Valid final Survey survey) {
/*
        String context = "context0";
        context = ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
				PersistenceManager pm = myShepherd.getPM();
*/
        System.out.println(survey);
				getPM(request).makePersistent(survey);
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

				if (survey != null) {
					List<SurveyTrack> tracks = survey.getTracks();
					if (tracks == null) tracks = new ArrayList<SurveyTrack>();
					tracks.add(track);
					survey.setTracks(tracks);
					pm.makePersistent(survey);
				}
        //System.out.println(track);
        return new ResponseEntity<Survey>(survey, HttpStatus.OK);
    }
}


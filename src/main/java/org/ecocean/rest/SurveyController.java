package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.survey.Survey;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/obj/survey")
public class SurveyController {
    @RequestMapping(value = "/getpart/{id}", method = RequestMethod.GET)
    public List<Survey> get(final HttpServletRequest request,
                            @PathVariable("id")
                            final int surveypartid) {
        //
        // TODO: Read survey from database.
        //
        return null;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public void save(final HttpServletRequest request,
                     @RequestBody @Valid final Survey survey) {
        //
        // TODO: save survey
        //
    }
}


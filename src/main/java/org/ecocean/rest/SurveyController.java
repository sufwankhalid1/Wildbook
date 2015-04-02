package org.ecocean.rest;

import javax.validation.Valid;

import org.ecocean.survey.Survey;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/survey")
public class SurveyController {

//    private final UserService userService;
//
//    @Inject
//    public UserController(final UserService userService) {
//        this.userService = userService;
//    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public void save(@RequestBody @Valid final Survey survey) {
        System.out.println(survey);
    }
}

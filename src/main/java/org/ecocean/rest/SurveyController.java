package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.servlet.ServletUtilities;
import org.ecocean.survey.SurveyFactory;
import org.ecocean.survey.SurveyObj;
import org.ecocean.survey.SurveyPart;
import org.ecocean.survey.SurveyPartObj;
import org.ecocean.survey.Vessel;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

@RestController
@RequestMapping(value = "/obj/survey")
public class SurveyController {
    private Logger logger = LoggerFactory.getLogger(SurveyController.class);

    @RequestMapping(value = "/part/get/{id}", method = RequestMethod.GET)
    public SurveyPartObj getPart(final HttpServletRequest request,
                                 @PathVariable("id")
                                 final int surveypartid) throws DatabaseException {
        try (Database db = ServletUtilities.getDb(request)) {
            SqlStatement sql = SurveyFactory.getSurveyStatement();

            sql.addCondition(SurveyFactory.ALIAS_SURVEYPART,
                             SurveyFactory.PK_SURVEYPART,
                             SqlRelationType.EQUAL,
                             surveypartid);

            return db.selectFirst(sql, (rs) -> {
                return SurveyFactory.readSurveyPartObj(rs);
            });
        }
    }

    @RequestMapping(value = "/vessels/get", method = RequestMethod.GET)
    public List<Vessel> getVessels(final HttpServletRequest request,
                                   @RequestParam
                                   final int orgid) throws DatabaseException {
        try (Database db = ServletUtilities.getDb(request)) {
            return SurveyFactory.getVesselsByOrg(db, orgid);
        }
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public void savePart(final HttpServletRequest request,
                         @RequestBody @Valid final SurveyObj survey) throws DatabaseException {
        if (survey == null) {
            return;
        }

        try (Database db = ServletUtilities.getDb(request)) {
            db.performTransaction(() -> {
              SurveyFactory.saveSurvey(db, survey);
              if (survey.tracks != null) {
                  for (SurveyPart track : survey.tracks) {
                      if (logger.isDebugEnabled()) {
                          LogBuilder.debug(logger, "track", track);
                      }
                      track.setSurveyId(survey.getSurveyId());
                      SurveyFactory.saveSurveyPart(db, track);
                  }
              }
            });
        }
    }
}


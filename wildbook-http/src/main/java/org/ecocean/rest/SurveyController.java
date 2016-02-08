package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.survey.Survey;
import org.ecocean.survey.SurveyFactory;
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
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.Table;

@RestController
@RequestMapping(value = "/obj/survey")
public class SurveyController {
    private final Logger logger = LoggerFactory.getLogger(SurveyController.class);

    @RequestMapping(value = "/part/get/{id}", method = RequestMethod.GET)
    public SurveyPartObj getPart(final HttpServletRequest request,
                                 @PathVariable("id")
                                 final int surveypartid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
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
        try (Database db = ServletUtils.getDb(request)) {
            return SurveyFactory.getVesselsByOrg(db, orgid);
        }
    }

    @RequestMapping(value = "savetrack", method = RequestMethod.POST)
    public SurveyPartObj savePart(final HttpServletRequest request,
                                  @RequestBody @Valid
                                  final SurveyPartObj sp) throws DatabaseException {
        if (sp == null) {
            return null;
        }

        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
              SurveyFactory.saveSurvey(db, sp.survey);
              if (logger.isDebugEnabled()) {
                  LogBuilder.debug(logger, "part", sp.part);
              }
              sp.part.setSurveyId(sp.survey.getSurveyId());
              SurveyFactory.saveSurveyPart(db, sp.part);
            });

            return sp;
        }
    }

    @RequestMapping(value = "/encounters/{partid}", method = RequestMethod.GET)
    public List<Encounter> getPartEncounters(final HttpServletRequest request,
                                             @PathVariable("partid")
                                             final int surveypartid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            SqlStatement sql = EncounterFactory.getEncounterStatement();
            sql.addInnerJoin(EncounterFactory.ALIAS_ENCOUNTERS,
                             EncounterFactory.PK_ENCOUNTERS,
                             "surveypart_encounters",
                             "spe",
                             EncounterFactory.PK_ENCOUNTERS);
            sql.addCondition("spe",
                             SurveyFactory.PK_SURVEYPART,
                             SqlRelationType.EQUAL,
                             surveypartid);

            return db.selectList(sql, (rs) -> {
                return EncounterFactory.readEncounter(rs);
            });
        }
    }

    @RequestMapping(value = "addencounter", method = RequestMethod.POST)
    public void addEncounter(final HttpServletRequest request,
                             @RequestBody @Valid final EncounterPart encounterPart) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            formatter.append(SurveyFactory.PK_SURVEYPART, encounterPart.surveypartid);
            formatter.append(EncounterFactory.PK_ENCOUNTERS, encounterPart.encounterid);

            Table table = db.getTable("surveypart_encounters");
            table.insertRow(formatter);
        }
    }

    @RequestMapping(value = "save", method = RequestMethod.POST)
    public Integer saveSurvey(final HttpServletRequest request,
                                             @RequestBody @Valid final Survey survey) throws DatabaseException {
        if (survey == null) {
            return null;
        }

        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                SurveyFactory.saveSurvey(db, survey);
            });
        }

        return survey.getSurveyId();
    }

//    @RequestMapping(value = "addcrewmember/{surveyid}", method = RequestMethod.POST)
//    public Crew getCrew(final HttpServletRequest request,
//                        @PathVariable("surveyid") final int surveyid) throws DatabaseException {
//        try (Database db = ServletUtils.getDb(request)) {
//        }
//    }

    private static class EncounterPart {
        public int surveypartid;
        public int encounterid;
    }
}

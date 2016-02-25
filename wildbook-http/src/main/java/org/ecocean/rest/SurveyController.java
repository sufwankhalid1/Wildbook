package org.ecocean.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.CrewMember;
import org.ecocean.Vessel;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.survey.SurveyFactory;
import org.ecocean.survey.SurveyPartObj;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

@RestController
@RequestMapping(value = "/api/survey")
public class SurveyController {
    //private final Logger logger = LoggerFactory.getLogger(SurveyController.class);

    @RequestMapping(value = "/part/get/{id}", method = RequestMethod.GET)
    public SurveyPartObj getPart(final HttpServletRequest request,
                                 @PathVariable("id")
                                 final int surveypartid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return SurveyFactory.getSurveyPart(db, surveypartid);
        }
    }

    @RequestMapping(value = "/part/getcrew/{id}", method = RequestMethod.GET)
    public List<CrewMember> getCrew(final HttpServletRequest request,
                                    @PathVariable("id")
                                    final int surveypartid) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            return SurveyFactory.getSurveyPartCrew(db, surveypartid);
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
}

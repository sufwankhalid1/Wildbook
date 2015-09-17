package org.ecocean.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.servlet.ServletUtilities;
import org.ecocean.survey.SurveyFactory;
import org.ecocean.survey.SurveyPartObj;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

@RestController
@RequestMapping(value = "/obj/survey")
public class SurveyController {
    @RequestMapping(value = "/part/get/{id}", method = RequestMethod.GET)
    public SurveyPartObj getPart(final HttpServletRequest request,
                                 @PathVariable("id")
                                 final int surveypartid) throws DatabaseException {
        try (Database db = ServletUtilities.getDb(request)) {
            SqlStatement sql = SurveyFactory.getSqlStatement();

            sql.addCondition(SurveyFactory.ALIAS_SURVEYPART,
                             SurveyFactory.PK_SURVEYPART,
                             SqlRelationType.EQUAL,
                             surveypartid);

            RecordSet rs = db.getRecordSet(sql);
            if (rs.next()) {
                return SurveyFactory.readSurveyPartObj(rs);
            }

            return null;
        }
    }

    @RequestMapping(value = "/part/save", method = RequestMethod.POST)
    public void savePart(final HttpServletRequest request,
                         @RequestBody @Valid final SurveyPartObj spo) throws DatabaseException {
        try (Database db = ServletUtilities.getDb(request)) {
            SurveyFactory.saveSurvey(db, spo.survey);
            SurveyFactory.saveSurveyPart(db, spo.part);
        }
    }
}


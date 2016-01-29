package org.ecocean.rest.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.Species;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimpleUser;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtils;
import org.ecocean.survey.SurveyFactory;
import org.ecocean.survey.SurveyPartObj;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.GroupedSqlCondition;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlTable;

@RestController
@RequestMapping(value = "/search")
public class SearchController
{
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    private List<SimpleIndividual> searchIndividuals(final Database db, final String term) throws DatabaseException {
        if (StringUtils.isBlank(term)) {
            return Collections.emptyList();
        }

        SqlStatement sql = EncounterFactory.getIndividualStatement();

        SqlTable table = sql.findTable(EncounterFactory.ALIAS_INDIVIDUALS);
        GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
        cond.addContainsCondition(table, "alternateid", term);
        cond.addContainsCondition(table, "nickname", term);
        sql.addCondition(cond);

        return db.selectList(sql, (rs) -> {
            return EncounterFactory.readSimpleIndividual(rs);
        });
    }


    @RequestMapping(value = "/site", method = RequestMethod.GET)
    public List<SiteSearchResult> searchSite(final HttpServletRequest request,
                                             @RequestParam
                                             final String term) throws DatabaseException
    {
        List<SiteSearchResult> results = new ArrayList<>();

        if (StringUtils.isBlank(term)) {
            return results;
        }

        try (Database db = ServletUtils.getDb(request)) {
            for (SimpleIndividual individual : searchIndividuals(db, term)) {
                SiteSearchResult result = new SiteSearchResult();
                result.label = individual.getDisplayName();
                result.value = String.valueOf(individual.getId());
                result.type = "individual";
                result.species = individual.getSpecies();
                result.avatar = individual.getAvatar();
                results.add(result);
            }

            SqlStatement sql = UserFactory.userSearchStatement(term);

            results.addAll(db.selectList(sql, (rs) -> {
                SimpleUser user = UserFactory.readSimpleUser(rs);

                SiteSearchResult result = new SiteSearchResult();
                result.label = user.getDisplayName();
                result.value = String.valueOf(user.getId());
                result.type = "user";
                result.avatar = user.getAvatar();
                return result;
            }));

            return results;
        }
    }
//
//
//    @RequestMapping(value = "/encounter", method = RequestMethod.POST)
//    public List<SimpleEncounter> searchEncounter(final HttpServletRequest request,
//                                                 @RequestBody final EncounterSearch search) throws DatabaseException
//    {
//        List<Encounter> encounters = EncounterController.searchEncounters(request, search);
//
//        List<SimpleEncounter> simples = new ArrayList<>(encounters.size());
//        for (Encounter encounter : encounters) {
//            simples.add(encounter.toSimple());
//        }
//
//        return simples;
//    }
//
//
//    @RequestMapping(value = "/individual", method = RequestMethod.POST)
//    public List<SimpleIndividual> searchIndividual(final HttpServletRequest request,
//                                                   @RequestBody
//                                                   final IndividualSearch search) throws DatabaseException
//    {
//        List<Individual> individuals = IndividualController.searchIndividuals(request, search);
//
//        List<SimpleIndividual> simples = new ArrayList<>(individuals.size());
//        for (Individual individual : individuals) {
//            simples.add(individual.toSimple());
//        }
//
//        return simples;
//    }


    @RequestMapping(value = "/survey", method = RequestMethod.POST)
    public List<SurveyPartObj> searchSurvey(final HttpServletRequest request,
                                            @RequestBody final SurveySearch search) throws DatabaseException
    {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("search", ToStringBuilder.reflectionToString(search)));
        }

        try (Database db = ServletUtils.getDb(request)) {
            SqlStatement sql = SurveyFactory.getSurveyStatement();

            if (search.vesselid != null) {
                sql.addCondition(SurveyFactory.ALIAS_SURVEYPART, "vesselid", SqlRelationType.EQUAL, search.vesselid);
            }

            if (! StringUtils.isBlank(search.code)) {
                sql.addContainsCondition(SurveyFactory.ALIAS_SURVEYPART, "code", search.code);
            }

            if (! StringUtils.isBlank(search.surveynumber)) {
                sql.addContainsCondition(SurveyFactory.ALIAS_SURVEY, "surveynumber", search.surveynumber);
            }

            if (search.orgid != null) {
                sql.addCondition(SurveyFactory.ALIAS_SURVEY, UserFactory.PK_ORG, SqlRelationType.EQUAL, search.orgid);
            }

            if (! StringUtils.isBlank(search.date)) {
                sql.addCondition(SurveyFactory.ALIAS_SURVEYPART, "partdate", SqlRelationType.EQUAL, search.date);
            }

            List<SurveyPartObj> parts = new ArrayList<>();
            db.select(sql, (rs) -> {
                parts.add(SurveyFactory.readSurveyPartObj(rs));
            });

            return parts;
        }
    }

    static class SiteSearchResult
    {
        public String label;
        public String value;
        public String type;
        public Species species;
        public String avatar;
    }

    static class SurveySearch
    {
        private Integer vesselid;
        private String surveynumber;
        private String code;
        private Integer orgid;
        private String date;

        public Integer getVesselid() {
            return vesselid;
        }
        public void setVesselid(final Integer vesselid) {
            this.vesselid = vesselid;
        }
        public String getSurveynumber() {
            return surveynumber;
        }
        public void setSurveynumber(final String surveynumber) {
            this.surveynumber = surveynumber;
        }
        public String getCode() {
            return code;
        }
        public void setCode(final String code) {
            this.code = code;
        }
        public Integer getOrgid() {
            return orgid;
        }
        public void setOrgid(final Integer orgid) {
            this.orgid = orgid;
        }
        public String getDate() {
            return date;
        }
        public void setDate(final String date) {
            this.date = date;
        }
    }
}
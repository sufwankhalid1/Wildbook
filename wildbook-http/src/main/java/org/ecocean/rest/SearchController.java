package org.ecocean.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.ecocean.Individual;
import org.ecocean.Species;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.encounter.SimpleEncounter;
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

        IndividualController.addIndividualNameCondition(sql, term);

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

            SqlStatement sql = UserFactory.getUserStatement();

            SqlTable users = sql.findTable(UserFactory.AlIAS_USERS);
            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            cond.addContainsCondition(users, "fullname", term);
            cond.addContainsCondition(users, "username", term);
            sql.addCondition(cond);

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


    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public List<SimpleUser> searchUser(final HttpServletRequest request,
                                       @RequestBody
                                       final UserSearch search) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            SqlStatement sql = UserFactory.getUserStatement();

            SqlTable users = sql.findTable(UserFactory.AlIAS_USERS);
            if (! StringUtils.isBlank(search.name)) {
                GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
                cond.addContainsCondition(users, "fullname", search.name);
                cond.addContainsCondition(users, "username", search.name);
                sql.addCondition(cond);
            }
            sql.setOrderBy("fullname");

            return db.selectList(sql, (rs) -> {
                return UserFactory.readSimpleUser(rs);
            });
        }
    }


    @RequestMapping(value = "/encounter", method = RequestMethod.POST)
    public List<SimpleEncounter> searchEncounter(final HttpServletRequest request,
                                                 @RequestBody final EncounterSearch search) throws DatabaseException
    {
        SqlStatement sql = EncounterFactory.getEncounterStatement();

        if (search.encdate != null) {
            sql.addCondition(EncounterFactory.ALIAS_ENCOUNTERS, "encdate", SqlRelationType.EQUAL, search.encdate.toString());
        }

        if (! StringUtils.isBlank(search.locationid)) {
            sql.addContainsCondition(EncounterFactory.ALIAS_ENCOUNTERS, "locationid", search.locationid);
        }

        try (Database db = ServletUtils.getDb(request)) {
            return db.selectList(sql, (rs) -> {
                return EncounterFactory.readSimpleEncounter(rs);
            });
        }
    }


    @RequestMapping(value = "/individual", method = RequestMethod.POST)
    public List<SimpleIndividual> searchIndividual(final HttpServletRequest request,
                                                   @RequestBody
                                                   final IndividualSearch search) throws DatabaseException
    {
        List<Individual> individuals = IndividualController.searchIndividuals(request, search);

        List<SimpleIndividual> simples = new ArrayList<>(individuals.size());
        for (Individual individual : individuals) {
            simples.add(individual.toSimple());
        }

        return simples;
    }


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
                sql.addCondition(SurveyFactory.ALIAS_SURVEY, "orgid", SqlRelationType.EQUAL, search.orgid);
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

    static class EncounterSearch
    {
        private LocalDate encdate;
        private String locationid;

        public LocalDate getEncdate() {
            return encdate;
        }
        public void setEncdate(final LocalDate encdate) {
            this.encdate = encdate;
        }
        public String getLocationid() {
            return locationid;
        }
        public void setLocationid(final String locationid) {
            this.locationid = locationid;
        }
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

    static class UserSearch
    {
        public String name;
    }
}
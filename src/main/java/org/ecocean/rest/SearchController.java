package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.ShepherdPMF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.GroupedSqlCondition;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

@RestController
@RequestMapping(value = "/search")
public class SearchController
{
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    private List<SimpleIndividual> searchIndividuals(final Database db, final String term) throws DatabaseException {
        List<SimpleIndividual> individuals = new ArrayList<>();

        if (StringUtils.isBlank(term)) {
            return individuals;
        }

        String searchTerm = "%" + term.toLowerCase() + "%";

        RecordSet rs;
        SqlStatement ss = SimpleFactory.getIndividualStatement();

        GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
        cond.addCondition(ss.findTable("i"), "alternateid", SqlRelationType.LIKE, searchTerm)
            .setFunction("lower");
        cond.addCondition(ss.findTable("i"), "nickname", SqlRelationType.LIKE, searchTerm)
            .setFunction("lower");
        ss.addCondition(cond);

        rs = db.getRecordSet(ss.getSql());
        while (rs.next()) {
            individuals.add(SimpleFactory.readSimpleIndividual(rs));
        }

        return individuals;
    }


    @RequestMapping(value = "/site", method = RequestMethod.GET)
    public List<SiteSearchResult> searchSite(final HttpServletResponse response,
                                             @RequestParam
                                             final String term) throws DatabaseException
    {
        List<SiteSearchResult> results = new ArrayList<>();

        if (StringUtils.isBlank(term)) {
            return results;
        }

        try (Database db = ShepherdPMF.getDb()) {
            for (SimpleIndividual individual : searchIndividuals(db, term)) {
                SiteSearchResult result = new SiteSearchResult();
                result.label = individual.getDisplayName();
                result.value = String.valueOf(individual.getId());
                result.type = "individual";
                result.species = individual.getSpecies();
                result.speciesdisplay = individual.getSpeciesDisplayName();
                result.avatar = individual.getAvatar();
                results.add(result);
            }

            String searchTerm = "%" + term.toLowerCase() + "%";

            //
            // Query on Users
            //
            SqlStatement ss = SimpleFactory.getUserStatement();

            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            cond.addCondition(ss.findTable("u"), "\"FULLNAME\"", SqlRelationType.LIKE, searchTerm)
                .setFunction("lower");
            cond.addCondition(ss.findTable("u"), "\"USERNAME\"", SqlRelationType.LIKE, searchTerm)
                .setFunction("lower");
            ss.addCondition(cond);

            RecordSet rs = db.getRecordSet(ss.getSql());
            while (rs.next()) {
                SimpleUser user = SimpleFactory.readUser(rs);

                SiteSearchResult result = new SiteSearchResult();
                result.label = user.getDisplayName();
                result.value = user.getUsername();
                result.type = "user";
                result.avatar = user.getAvatar();
                results.add(result);
            }

            return results;
        }
    }


    @RequestMapping(value = "/encounter", method = RequestMethod.GET)
    public List<SimpleEncounter> searchEncounter(final HttpServletResponse response,
                                                 @RequestParam
                                                 final String encdate,
                                                 @RequestParam
                                                 final String individualid) throws DatabaseException
    {
        SqlStatement sql = SimpleFactory.getEncounterStatement();

        if (! StringUtils.isBlank(encdate)) {
            sql.addCondition("e", "encdate", SqlRelationType.EQUAL, encdate);
        }
        if (! StringUtils.isBlank(individualid)) {
            sql.addCondition("i", "individualid", SqlRelationType.EQUAL, individualid);
        }

        try (Database db = ShepherdPMF.getDb()) {
            List<SimpleEncounter> encounters = new ArrayList<>();
            RecordSet rs = db.getRecordSet(sql.getSql());
            while (rs.next()) {
                encounters.add(SimpleFactory.readSimpleEncounter(rs));
            }

            return encounters;
        }
    }


    @RequestMapping(value = "/individual", method = RequestMethod.GET)
    public List<SimpleIndividual> searchIndividual(final HttpServletResponse response,
                                                   @RequestParam
                                                   final String name) throws DatabaseException
    {
        try (Database db = ShepherdPMF.getDb()) {
            return searchIndividuals(db, name);
        }
    }


    static class SiteSearchResult
    {
        public String label;
        public String value;
        public String type;
        public String species;
        public String speciesdisplay;
        public String avatar;
    }
}
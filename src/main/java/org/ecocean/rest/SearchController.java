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
import com.samsix.database.RecordSet;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.util.string.StringUtilities;

@RestController
@RequestMapping(value = "/search")
public class SearchController
{
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    @RequestMapping(value = "/site", method = RequestMethod.GET)
    public List<SiteSearchResult> searchSite(final HttpServletResponse response,
                                             @RequestParam
                                             final String term) throws DatabaseException
    {
        List<SiteSearchResult> results = new ArrayList<>();

        if ((term == null) || term.equals("")) {
            return results;
        }

        try (Database db = ShepherdPMF.getDb()) {
            String search = StringUtilities.wrapQuotes("%" + term.toLowerCase() + "%");

            RecordSet rs;
            String sql;
            sql = "select * from individuals where lower(alternateid) like "
                    + search
                    + " OR lower(nickname) like "
                    + search;

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                SimpleIndividual individual = SimpleFactory.readSimpleIndividual(rs);
                SiteSearchResult result = new SiteSearchResult();
                result.label = individual.getDisplayName();
                result.value = String.valueOf(individual.getId());
                result.type = "individual";
                result.species = individual.getSpecies();
                result.speciesdisplay = individual.getSpeciesDisplayName();
                results.add(result);
            }

            //
            // Query on Users
            //
            sql = "select * from \"USERS\" where lower(\"FULLNAME\") like "
                    + search
                    + " OR lower(\"USERNAME\") like "
                    + search;

            rs = db.getRecordSet(sql);
            while (rs.next()) {
                SiteSearchResult result = new SiteSearchResult();
                String fullname = rs.getString("FULLNAME");
                String username = rs.getString("USERNAME");
                if (StringUtils.isBlank(fullname)) {
                    result.label = username;
                } else {
                    result.label = fullname + " (" + username + ")";
                }
                result.value = username;
                result.type = "user";
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


    static class SiteSearchResult
    {
        public String label;
        public String value;
        public String type;
        public String species;
        public String speciesdisplay;
    }
}
package org.ecocean.rest.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Individual;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.rest.search.EncounterSearch;
import org.ecocean.rest.search.IndividualSearch;
import org.ecocean.rest.search.SearchController;
import org.ecocean.rest.search.SearchData;
import org.ecocean.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.GroupedSqlCondition;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlTable;

@RestController
@RequestMapping(value = "/admin/search")
public class AdminSearchController {
    Logger logger = LoggerFactory.getLogger(SearchController.class);

    private static void addEncounterData(final SqlStatement sql, final EncounterSearch search) {
        if (search.getEncdate() != null) {
            sql.addCondition(EncounterFactory.ALIAS_ENCOUNTERS, "encdate", SqlRelationType.EQUAL, search.getEncdate().toString());
        }

        if (! StringUtils.isBlank(search.getLocationid())) {
            sql.addContainsCondition(EncounterFactory.ALIAS_ENCOUNTERS, "locationid", search.getLocationid());
        }
    }


    public static List<Encounter> searchEncounters(final HttpServletRequest request,
                                                   final SearchData search) throws DatabaseException {
        SqlStatement sql = EncounterFactory.getEncounterStatement();

        addEncounterData(sql, search.encounter);
        addIndividualData(sql, search.individual);

        try (Database db = ServletUtils.getDb(request)) {
            return db.selectList(sql, (rs) -> {
                return EncounterFactory.readEncounter(rs);
            });
        }
    }

    private static void addIndividualData(final SqlStatement sql, final IndividualSearch search) {
        if (search.nameid != null) {
            SqlTable table = sql.findTable(EncounterFactory.ALIAS_INDIVIDUALS);
            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            cond.addContainsCondition(table, "alternateid", search.nameid);
            cond.addContainsCondition(table, "nickname", search.nameid);
            sql.addCondition(cond);
        }

        if (search.species != null) {
            sql.addCondition(EncounterFactory.ALIAS_INDIVIDUALS, "species", SqlRelationType.EQUAL, search.species);
        }
    }

    public static List<Individual> searchIndividuals(final HttpServletRequest request,
                                                     final SearchData search)
            throws DatabaseException {
        SqlStatement sql = EncounterFactory.getIndividualStatement();

        addIndividualData(sql, search.individual);
        addEncounterData(sql, search.encounter);

        try (Database db = ServletUtils.getDb(request)) {
            return db.selectList(sql, (rs) -> {
                return EncounterFactory.readIndividual(rs);
            });
        }
    }


    @RequestMapping(value = "/encounter", method = RequestMethod.POST)
    public List<Encounter> searchEncounter(final HttpServletRequest request,
                                           @RequestBody final SearchData search) throws DatabaseException
    {
        return searchEncounters(request, search);
    }


    @RequestMapping(value = "/individual", method = RequestMethod.POST)
    public List<Individual> searchIndividual(final HttpServletRequest request,
                                             @RequestBody
                                             final SearchData search) throws DatabaseException
    {
        return searchIndividuals(request, search);
    }

}

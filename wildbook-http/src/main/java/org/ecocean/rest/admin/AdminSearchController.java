package org.ecocean.rest.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.Individual;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimpleUser;
import org.ecocean.search.SearchData;
import org.ecocean.search.SearchFactory;
import org.ecocean.search.UserSearch;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlStatement;

@RestController
@RequestMapping(value = "/admin/api/search")
public class AdminSearchController {
    Logger logger = LoggerFactory.getLogger(AdminSearchController.class);


    @RequestMapping(value = "/encounter", method = RequestMethod.POST)
    public List<Encounter> searchEncounter(final HttpServletRequest request,
                                           @RequestBody final SearchData search) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            return SearchFactory.searchEncounters(db, search);
        }
    }

    @RequestMapping(value = "/orphaned", method = RequestMethod.GET)
    public List<SimpleIndividual> orphaned(final HttpServletRequest request) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {

            SqlStatement sqls = EncounterFactory.getIndividualStatement();
            String sql = sqls.toString() + " where not exists (select * from encounters where individualid = "
                        + EncounterFactory.ALIAS_INDIVIDUALS + ".individualid)";

            return db.selectList(sql, (rs) -> {
                return EncounterFactory.readSimpleIndividual(rs);
            });
        }
    }

    @RequestMapping(value = "/individual", method = RequestMethod.POST)
    public List<Individual> searchIndividual(final HttpServletRequest request,
                                             @RequestBody
                                             final SearchData search) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            return SearchFactory.searchIndividuals(db, search);
        }
    }


    //
    // Returning SimpleUser for now as I don't see the need for a full user.
    // If we need a full user we can get them individually with a call.
    //
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public List<SimpleUser> searchUser(final HttpServletRequest request,
                                       @RequestBody
                                       final UserSearch search) throws DatabaseException
    {
        try (Database db = ServletUtils.getDb(request)) {
            SqlStatement sql = UserFactory.getUserStatement();

            SearchFactory.addUserData(sql, sql.findTable(UserFactory.ALIAS_USERS), search);

            sql.setOrderBy("fullname");

            return db.selectList(sql, (rs) -> {
                return UserFactory.readSimpleUser(rs);
            });
        }
    }
}

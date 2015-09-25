package org.ecocean.rest;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.media.MediaAssetFactory;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtilities;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.util.string.StringUtilities;

@RestController
@RequestMapping(value = "/data")
public class MainController
{
    private static Logger logger = LoggerFactory.getLogger(MainController.class);

    public static List<Contributor> getTopContributors(final HttpServletRequest request,
                                                       final int pastNumDays,
                                                       final int number)
            throws DatabaseException
    {
        LocalDate since = LocalDate.now().minusDays(pastNumDays);

        String table = "(select ma2.submitterid, count(*) as numEncs from mediaasset ma2"
                + " inner join encounter_media em on ma2.id = em.mediaid"
                + " inner join encounters e on em.encounterid = e.encounterid"
                + " where e.encdate > "
                + StringUtilities.wrapQuotes(since.toString())
                + " and submitterid is not null"
                + " group by ma2.submitterid"
                + " order by numEncs desc limit "
                + number
                + ")";

        SqlStatement sql = UserFactory.getUserStatement();
        sql.addInnerJoin(UserFactory.AlIAS_USERS, UserFactory.PK_USERS, table, "c", "submitterid");
        try (Database db = ServletUtilities.getDb(request)) {
            List<Contributor> contribs = new ArrayList<>();

            RecordSet rs = db.getRecordSet(sql);
            while (rs.next()) {
                Contributor contrib = new Contributor();
                contrib.user = UserFactory.readSimpleUser(rs);
                contrib.numEncs = rs.getInt("numEncs");
                contribs.add(contrib);
            }

            return contribs;
        }
    }


    @RequestMapping(value = "/individual/get/{id}", method = RequestMethod.GET)
    public IndividualInfo getIndividual(final HttpServletRequest request,
                                        @PathVariable("id")
                                        final int id) throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            IndividualInfo indinfo = new IndividualInfo();

            indinfo.individual = SimpleFactory.getIndividual(db, id);
            if (indinfo.individual == null) {
                return null;
            }

            indinfo.photos = SimpleFactory.getIndividualPhotos(db, id);
            indinfo.encounters = SimpleFactory.getIndividualEncounters(db, indinfo.individual);
            indinfo.submitters = SimpleFactory.getIndividualSubmitters(db, id);

            return indinfo;
        }
    }


    @RequestMapping(value = "/encounter/sighters/{id}", method = RequestMethod.GET)
    public List<SimpleUser> getUser(final HttpServletRequest request,
                                    @PathVariable("id")
                                    final int encounterid) throws DatabaseException
    {
        SqlStatement sql = UserFactory.getUserStatement(true);
        sql.addInnerJoin(UserFactory.AlIAS_USERS,
                         UserFactory.PK_USERS,
                         MediaAssetFactory.TABLENAME_MEDIAASSET,
                         "ma2",
                         "submitterid");
        sql.addInnerJoin("ma2",
                         MediaAssetFactory.PK_MEDIAASSET,
                         "encounter_media",
                         "em",
                         "mediaid");
        sql.addCondition("em", "encounterid", SqlRelationType.EQUAL, encounterid);

        try (Database db = ServletUtilities.getDb(request)) {
            List<SimpleUser> sighters = new ArrayList<>();

            db.select(sql, (rs) -> {
                sighters.add(UserFactory.readSimpleUser(rs));
            });

            return sighters;
        }
    }


    @RequestMapping(value = "/user/get/{username}", method = RequestMethod.GET)
    public SimpleBeing getUser(final HttpServletRequest request,
                               @PathVariable("username")
                               final String username) throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            return SimpleFactory.getUser(db, username);
        }
    }


    @RequestMapping(value = "/userinfo/get/{username}", method = RequestMethod.GET)
    public UserInfo getUserInfo(final HttpServletRequest request,
                                @PathVariable("username")
                                final int userid) throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            return SimpleFactory.getUserInfo(db, userid);
        }
    }


    @RequestMapping(value = "/config/value/{var}", method = RequestMethod.GET)
    public String getConfigValue(final HttpServletRequest request,
            @PathVariable("var")
            final String var)
    {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.get()
                    .appendVar("var", var)
                    .appendVar("value", request.getServletContext().getInitParameter(var)).toString());
        }
        return request.getServletContext().getInitParameter(var);
    }

    static class IndividualInfo
    {
        public SimpleIndividual individual;
        public List<SimpleEncounter> encounters;
        public List<SimplePhoto> photos;
        public List<SimpleUser> submitters;
    }

    public static class Contributor {
        public SimpleUser user;
        public int numEncs;
    }
}

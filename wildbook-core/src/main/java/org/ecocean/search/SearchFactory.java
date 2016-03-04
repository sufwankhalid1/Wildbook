package org.ecocean.search;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Individual;
import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.security.UserFactory;
import org.ecocean.util.DateUtils;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.GroupedSqlCondition;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlTable;

public class SearchFactory {
    private SearchFactory() {
        //prevent instantiation
    }

    private static void addEncounterData(final SqlStatement sql, final EncounterSearch search) {

        DateUtils.dateSearch(sql, search.datesearch, EncounterFactory.ALIAS_ENCOUNTERS, "encdate");

        if (!StringUtils.isBlank(search.location)) {
            SqlTable table = sql.findTable(EncounterFactory.ALIAS_ENCOUNTERS);
            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            cond.addContainsCondition(table, "locationid", search.location);
            cond.addContainsCondition(table, "verbatimlocation", search.location);
            sql.addCondition(cond);
        }

        if (!StringUtils.isBlank(search.comments)) {
            sql.addContainsCondition(EncounterFactory.ALIAS_ENCOUNTERS, "comments", search.comments);
        }
    }

    public static List<Encounter> searchEncounters(final Database db,
                                                   final SearchData search) throws DatabaseException {
        SqlStatement sql = EncounterFactory.getEncounterStatement(true);

        addEncounterData(sql, search.encounter);
        //
        // Individual table is already in the basic encounter statement.
        //
        if (search.individual != null) {
            addIndividualData(sql, search.individual);
        }

        if (search.contributor != null && search.contributor.hasData()) {
            addContributorData(sql, search.contributor);
        }

        sql.setOrderBy(EncounterFactory.ALIAS_ENCOUNTERS, "encdate", true);

        return db.selectList(sql, (rs) -> {
            return EncounterFactory.readEncounter(rs);
        });
    }

    private static void addIndividualData(final SqlStatement sql, final IndividualSearch search) {
        if (!StringUtils.isBlank(search.nameid)) {
            SqlTable table = sql.findTable(EncounterFactory.ALIAS_INDIVIDUALS);
            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            cond.addContainsCondition(table, "alternateid", search.nameid);
            cond.addContainsCondition(table, "nickname", search.nameid);
            sql.addCondition(cond);
        }

        if (!StringUtils.isBlank(search.species)) {
            sql.addCondition(EncounterFactory.ALIAS_INDIVIDUALS, "species", SqlRelationType.EQUAL, search.species);
        }

        if (!StringUtils.isBlank(search.bio)) {
            sql.addContainsCondition(EncounterFactory.ALIAS_INDIVIDUALS, "bio", search.bio);
        }

        if (!StringUtils.isBlank(search.sex)) {
            sql.addContainsCondition(EncounterFactory.ALIAS_INDIVIDUALS, "sex", search.sex);
        }

        if (search.identified != null) {
            sql.addCondition(EncounterFactory.ALIAS_INDIVIDUALS, "identified", search.identified);
        }
    }

    public static void addUserData(final SqlStatement sql, final SqlTable users, final UserSearch search) {
        if (! StringUtils.isBlank(search.name)) {
            GroupedSqlCondition cond = GroupedSqlCondition.orGroup();
            cond.addContainsCondition(users, "fullname", search.name);
            cond.addContainsCondition(users, "username", search.name);
            sql.addCondition(cond);
        }
    }


    private static void addContributorData(final SqlStatement sql, final UserSearch search) {

        if (search == null) {
            return;
        }

        sql.addInnerJoin(EncounterFactory.ALIAS_ENCOUNTERS,
                EncounterFactory.PK_ENCOUNTERS,
                EncounterFactory.TABLENAME_ENCOUNTER_MEDIA,
                EncounterFactory.ALIAS_ENCOUNTER_MEDIA,
                EncounterFactory.PK_ENCOUNTERS);
        sql.addInnerJoin(EncounterFactory.ALIAS_ENCOUNTER_MEDIA,
                         "mediaid",
                         MediaAssetFactory.TABLENAME_MEDIAASSET,
                         "masearch",
                         MediaAssetFactory.PK_MEDIAASSET);
        SqlTable contributors = new SqlTable(UserFactory.TABLENAME_USERS, "contrib");
        SqlTable masearch = sql.findTable("masearch");
        sql.addInnerJoin(masearch, contributors, "submitterid", UserFactory.PK_USERS);

        addUserData(sql, contributors, search);
    }


    public static List<Individual> searchIndividuals(final Database db,
                                                     final SearchData search)
            throws DatabaseException {

        SqlStatement sql = EncounterFactory.getIndividualStatement(true);

        addIndividualData(sql, search.individual);

        if (search.encounter.hasData() || search.contributor.hasData()) {
            sql.addLeftOuterJoin(EncounterFactory.ALIAS_INDIVIDUALS,
                                 EncounterFactory.PK_INDIVIDUALS,
                                 EncounterFactory.TABLENAME_ENCOUNTERS,
                                 EncounterFactory.ALIAS_ENCOUNTERS,
                                 EncounterFactory.PK_INDIVIDUALS);
            addEncounterData(sql, search.encounter);

            if (search.contributor.hasData()) {
                addContributorData(sql, search.contributor);
            }
        }

        return db.selectList(sql, (rs) -> {
            return EncounterFactory.readIndividual(rs);
        });
    }
}

package org.ecocean.encounter;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.LocationFactory;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimplePhoto;
import org.ecocean.rest.SimpleUser;
import org.ecocean.security.UserFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;

public class EncounterFactory {
    public static final String TABLENAME_ENCOUNTERS = "encounters";
    public static final String ALIAS_ENCOUNTERS = "e";
    public static final String PK_ENCOUNTERS = "encounterid";

    public static final String TABLENAME_INDIVIDUALS = "individuals";
    public static final String ALIAS_INDIVIDUALS = "i";
    public static final String PK_INDIVIDUALS = "individualid";

    public static final String TABLENAME_ENCOUNTER_MEDIA = "encounter_media";
    public static final String ALIAS_ENCOUNTER_MEDIA = "em";

    private EncounterFactory() {
        // prevent instantiation
    }

    public static SqlStatement getIndividualStatement()
    {
        SqlStatement sql = new SqlStatement(TABLENAME_INDIVIDUALS, ALIAS_INDIVIDUALS);
        sql.addLeftOuterJoin(ALIAS_INDIVIDUALS,
                             "avatarid",
                             MediaAssetFactory.TABLENAME_MEDIAASSET,
                             MediaAssetFactory.ALIAS_MEDIAASSET,
                             MediaAssetFactory.PK_MEDIAASSET);
        return sql;
    }

    public static List<SimpleEncounter> getIndividualEncounters(final Database db, final SimpleIndividual individual) throws DatabaseException
    {
        List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();

        db.getTable(TABLENAME_ENCOUNTERS).select((rs) -> {
            encounters.add(readSimpleEncounter(individual, rs));
        }, PK_INDIVIDUALS + " = " + individual.getId());

        return encounters;
    }

    public static SimpleEncounter readSimpleEncounter(final SimpleIndividual individual,
                                                      final RecordSet rs) throws DatabaseException
    {
        SimpleEncounter encounter = new SimpleEncounter(rs.getInt(PK_ENCOUNTERS), rs.getLocalDate("encdate"));

        encounter.setStarttime(rs.getOffsetTime("starttime"));
        encounter.setEndtime(rs.getOffsetTime("endtime"));
        encounter.setLocation(LocationFactory.readLocation(rs));
        encounter.setIndividual(individual);

        return encounter;
    }

    public static SimpleIndividual readSimpleIndividual(final RecordSet rs) throws DatabaseException
    {
        Integer indid = rs.getInteger(PK_INDIVIDUALS);
        if (indid == null) {
            return null;
        }

        SimpleIndividual ind = new SimpleIndividual(indid, rs.getString("nickname"));
        ind.setSex(rs.getString("sex"));
        ind.setSpecies(rs.getString("species"));
        ind.setAlternateId(rs.getString("alternateid"));

        SimplePhoto photo = MediaAssetFactory.readPhoto(rs);
        if (photo != null) {
            ind.setAvatar(photo.getThumbUrl());
        }

        return ind;
    }

    public static SimpleEncounter readSimpleEncounter(final RecordSet rs) throws DatabaseException
    {
        return readSimpleEncounter(readSimpleIndividual(rs), rs);
    }

    public static SimpleIndividual getIndividual(final Database db, final int individualId) throws DatabaseException
    {
        RecordSet rs;
        SqlStatement sql = getIndividualStatement();
        sql.addCondition(ALIAS_INDIVIDUALS, PK_INDIVIDUALS, SqlRelationType.EQUAL, individualId);
        rs = db.getRecordSet(sql);
        if (rs.next()) {
            return readSimpleIndividual(rs);
        }

        return null;
    }

    public static SqlStatement getEncounterStatement()
    {
        SqlStatement sql = new SqlStatement(TABLENAME_ENCOUNTERS, ALIAS_ENCOUNTERS);
        sql.addLeftOuterJoin(ALIAS_ENCOUNTERS, PK_INDIVIDUALS, TABLENAME_INDIVIDUALS, ALIAS_INDIVIDUALS, PK_INDIVIDUALS);
        sql.addLeftOuterJoin(ALIAS_INDIVIDUALS,
                             "avatarid",
                             MediaAssetFactory.TABLENAME_MEDIAASSET,
                             MediaAssetFactory.ALIAS_MEDIAASSET,
                             MediaAssetFactory.PK_MEDIAASSET);
        return sql;
    }

    public static SqlStatement getEncounterStatement(final boolean distinct)
    {
        SqlStatement sql = new SqlStatement(TABLENAME_ENCOUNTERS, ALIAS_ENCOUNTERS);
        sql.addLeftOuterJoin(ALIAS_ENCOUNTERS, PK_INDIVIDUALS, TABLENAME_INDIVIDUALS, ALIAS_INDIVIDUALS, PK_INDIVIDUALS);
        sql.addLeftOuterJoin(ALIAS_INDIVIDUALS,
                             "avatarid",
                             MediaAssetFactory.TABLENAME_MEDIAASSET,
                             MediaAssetFactory.ALIAS_MEDIAASSET,
                             MediaAssetFactory.PK_MEDIAASSET);
        sql.setSelectDistinct(true);
        sql.addSelectTable(ALIAS_ENCOUNTERS);
        sql.addSelectTable(ALIAS_INDIVIDUALS);
        sql.addSelectTable(MediaAssetFactory.ALIAS_MEDIAASSET);

        return sql;
    }

    public static List<SimpleUser> getIndividualSubmitters(final Database db, final int individualid) throws DatabaseException
    {
        SqlStatement sql = UserFactory.getUserStatement(true);
        sql.addInnerJoin(UserFactory.AlIAS_USERS,
                         UserFactory.PK_USERS,
                         MediaAssetFactory.TABLENAME_MEDIAASSET,
                         "ma2",
                         "submitterid");
        sql.addInnerJoin("ma2", MediaAssetFactory.PK_MEDIAASSET, TABLENAME_ENCOUNTER_MEDIA, ALIAS_ENCOUNTER_MEDIA, "mediaid");
        sql.addInnerJoin(ALIAS_ENCOUNTER_MEDIA, PK_ENCOUNTERS, TABLENAME_ENCOUNTERS, ALIAS_ENCOUNTERS, PK_ENCOUNTERS);
        sql.addCondition(ALIAS_ENCOUNTERS, PK_INDIVIDUALS, SqlRelationType.EQUAL, individualid);

        return UserFactory.readSimpleUsers(db, sql);
    }
}

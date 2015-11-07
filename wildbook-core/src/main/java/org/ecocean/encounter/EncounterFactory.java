package org.ecocean.encounter;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.Global;
import org.ecocean.Individual;
import org.ecocean.LocationFactory;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimplePhoto;
import org.ecocean.rest.SimpleUser;
import org.ecocean.security.UserFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlRelationType;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

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

    public static List<SimpleEncounter> toSimple(final List<Encounter> encounters) {
        if (encounters == null) {
            return null;
        }

        List<SimpleEncounter> simples = new ArrayList<>(encounters.size());
        for (Encounter encounter : encounters) {
            simples.add(encounter.toSimple());
        }
        return simples;
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

    public static List<Encounter> getIndividualEncounters(final Database db, final Individual individual) throws DatabaseException
    {
        List<Encounter> encounters = new ArrayList<>();

        db.getTable(EncounterFactory.TABLENAME_ENCOUNTERS).select((rs) -> {
            encounters.add(EncounterFactory.readEncounter(individual, rs));
        }, EncounterFactory.PK_INDIVIDUALS + " = " + individual.getId());

        return encounters;
    }

    public static Encounter readEncounter(final RecordSet rs) throws DatabaseException {
        return readEncounter(readIndividual(rs), rs);
    }

    public static Encounter readEncounter(final Individual individual,
                                          final RecordSet rs) throws DatabaseException
    {
        Encounter encounter = new Encounter(rs.getInt(PK_ENCOUNTERS), rs.getLocalDate("encdate"));

        encounter.setStarttime(rs.getOffsetTime("starttime"));
        encounter.setEndtime(rs.getOffsetTime("endtime"));
        encounter.setLocation(LocationFactory.readLocation(rs));
        encounter.setIndividual(individual);

        return encounter;
    }
//
//    public static SimpleIndividual readSimpleIndividual(final RecordSet rs) throws DatabaseException
//    {
//        Integer indid = rs.getInteger(PK_INDIVIDUALS);
//        if (indid == null) {
//            return null;
//        }
//
//        SimpleIndividual ind = new SimpleIndividual(indid, rs.getString("nickname"));
//        ind.setSex(rs.getString("sex"));
//        ind.setSpecies(Global.INST.getSpecies(rs.getString("species")));
//        ind.setAlternateId(rs.getString("alternateid"));
//
//        SimplePhoto photo = MediaAssetFactory.readPhoto(rs);
//        if (photo != null) {
//            ind.setAvatar(photo.getThumbUrl());
//        }
//
//        return ind;
//    }

    public static void addMedia(final Database db, final int encounterid, final int mediaid) throws DatabaseException {
        Table table = db.getTable(TABLENAME_ENCOUNTER_MEDIA);
        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append(PK_ENCOUNTERS, encounterid);
        formatter.append("mediaid", mediaid);
        table.insertRow(formatter);
    }

    public static List<SimplePhoto> getMedia(final Database db, final int encounterid) throws DatabaseException {
        SqlStatement sql = new SqlStatement(MediaAssetFactory.TABLENAME_MEDIAASSET,
                                            MediaAssetFactory.ALIAS_MEDIAASSET,
                                            MediaAssetFactory.ALIAS_MEDIAASSET + ".*");
        sql.addInnerJoin(MediaAssetFactory.ALIAS_MEDIAASSET, MediaAssetFactory.PK_MEDIAASSET, "encounter_media", "em", "mediaid");
        sql.addCondition("em", PK_ENCOUNTERS, SqlRelationType.EQUAL, encounterid);

        return db.selectList(sql, (rs) -> {
            return MediaAssetFactory.readPhoto(rs);
        });
    }

    public static SimpleIndividual readSimpleIndividual(final RecordSet rs) throws DatabaseException
    {
        Individual ind = readIndividual(rs);
        if (ind == null) {
            return null;
        }
        return ind.toSimple();
    }

    public static Individual readIndividual(final RecordSet rs) throws DatabaseException
    {
        Integer indid = rs.getInteger(PK_INDIVIDUALS);
        if (indid == null) {
            return null;
        }

        Individual ind = new Individual(indid,
                                        Global.INST.getSpecies(rs.getString("species")),
                                        rs.getString("nickname"));
        ind.setSex(rs.getString("sex"));
        ind.setAlternateId(rs.getString("alternateid"));
        ind.setIdentified(rs.getBoolean("identified"));

        MediaAsset ma = MediaAssetFactory.valueOf(rs);

        if (ma != null) {
            ind.setAvatarid(rs.getInteger("avatarid"));
            ind.setAvatar(ma.webPathString());
        }

        return ind;
    }

    public static SimpleEncounter readSimpleEncounter(final RecordSet rs) throws DatabaseException
    {
        return readEncounter(rs).toSimple();
    }

    public static Individual getIndividual(final Database db, final int individualId) throws DatabaseException
    {
        SqlStatement sql = getIndividualStatement();
        sql.addCondition(ALIAS_INDIVIDUALS, PK_INDIVIDUALS, SqlRelationType.EQUAL, individualId);
        return db.selectFirst(sql, (rs) -> {
            return readIndividual(rs);
        });
    }

    public static Individual getIndividualByAltId(final Database db, final String alternateId) throws DatabaseException
    {
        SqlStatement sql = EncounterFactory.getIndividualStatement();

        sql.addCondition(EncounterFactory.ALIAS_INDIVIDUALS,
                         "alternateid",
                         SqlRelationType.EQUAL,
                         alternateId);
        return db.selectFirst(sql, (rs) -> {
            return readIndividual(rs);
        });
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


    public static void saveEncounter(final Database db, final Encounter encounter) throws DatabaseException {
        if (encounter == null) {
            return;
        }

        saveIndividual(db, encounter.getIndividual());

        Table table = db.getTable(TABLENAME_ENCOUNTERS);

        if (encounter.getId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillEncounterFormatter(formatter, encounter);

            encounter.setId(table.insertSequencedRow(formatter, PK_ENCOUNTERS));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillEncounterFormatter(formatter, encounter);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_ENCOUNTERS, encounter.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillEncounterFormatter(final SqlFormatter formatter, final Encounter encounter) {
        if (encounter.getIndividual() != null) {
            formatter.append("individualid", encounter.getIndividual().getId());
        }

        formatter.append("encdate", encounter.getEncDate());
        formatter.append("starttime", encounter.getStarttime());
        formatter.append("endtime", encounter.getEndtime());
        LocationFactory.fillFormatterWithLoc(formatter, encounter.getLocation());
    }


    public static void saveIndividual(final Database db, final Individual individual) throws DatabaseException {
        if (individual == null) {
            return;
        }

        Table table = db.getTable(TABLENAME_INDIVIDUALS);

        if (individual.getId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillIndividualFormatter(formatter, individual);

            individual.setId(table.insertSequencedRow(formatter, PK_INDIVIDUALS));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillIndividualFormatter(formatter, individual);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_INDIVIDUALS, individual.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillIndividualFormatter(final SqlFormatter formatter, final Individual individual) {
        formatter.append("alternateId", individual.getAlternateId());
        formatter.append("identified", individual.isIdentified());
        if (individual.getSpecies() == null) {
            formatter.appendNull("species");
        } else {
            formatter.append("species", individual.getSpecies().getCode());
        }
        formatter.append("nickname", individual.getNickname());
        formatter.append("sex", individual.getSex());
        formatter.append("avatarid", individual.getAvatarid());
    }
}

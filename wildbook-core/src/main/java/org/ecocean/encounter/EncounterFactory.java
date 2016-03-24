package org.ecocean.encounter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.Global;
import org.ecocean.Individual;
import org.ecocean.LocationFactory;
import org.ecocean.Species;
import org.ecocean.event.type.IndividualSightedEvent;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.rest.SimpleIndividual;
import org.ecocean.rest.SimplePhoto;
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

    private final static IndividualStore indStore = Global.INST.getIndividualStore();
    private final static EncounterStore encStore = Global.INST.getEncounterStore();

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

    public static SqlStatement getIndividualStatement(final boolean distinct) {
        SqlStatement sql = new SqlStatement(TABLENAME_INDIVIDUALS, ALIAS_INDIVIDUALS);
        sql.addLeftOuterJoin(ALIAS_INDIVIDUALS, "avatarid", MediaAssetFactory.TABLENAME_MEDIAASSET,
                MediaAssetFactory.ALIAS_MEDIAASSET, MediaAssetFactory.PK_MEDIAASSET);
        sql.addSelectTable(ALIAS_INDIVIDUALS);
        sql.addSelectTable(MediaAssetFactory.ALIAS_MEDIAASSET);
        sql.setSelectDistinct(distinct);

        return sql;
    }

    public static Individual checkIndividualStore(final int id) {
        return indStore.get(id);
    }

    public static SqlStatement getIndividualStatement() {
        return getIndividualStatement(false);
    }

    public static List<Encounter> getIndividualEncounters(final Database db, final Individual individual)
            throws DatabaseException {
        return db.getTable(TABLENAME_ENCOUNTERS).selectList((rs) -> {
            return readEncounter(individual, rs);
        }, PK_INDIVIDUALS + " = " + individual.getId());
    }

    public static Encounter readEncounter(final RecordSet rs) throws DatabaseException {
        return readEncounter(readIndividual(rs), rs);
    }

    public static Encounter readEncounter(final Individual individual, final RecordSet rs) throws DatabaseException {
        int encounterId = rs.getInt(PK_ENCOUNTERS);

        Encounter encounter = encStore.get(encounterId);
        if (encounter != null) {
            //
            // Need to update our encounter with this individual which should have just come from
            // the individual store and thus should be fresh. It's possible that the encounter was
            // holding a non-fresh individual if changes were made to it.
            //
            encounter.setIndividual(individual);
            return encounter;
        }

        encounter = new Encounter(encounterId, rs.getLocalDate("encdate"));

        encounter.setStarttime(rs.getLocalTime("starttime"));
        encounter.setEndtime(rs.getLocalTime("endtime"));
        encounter.setLocation(LocationFactory.readLocation(rs));
        encounter.setComments(rs.getString("comments"));
        encounter.setIndividual(individual);

        encStore.put(encounter);

        return encounter;
    }
    //
    // public static SimpleIndividual readSimpleIndividual(final RecordSet rs)
    // throws DatabaseException
    // {
    // Integer indid = rs.getInteger(PK_INDIVIDUALS);
    // if (indid == null) {
    // return null;
    // }
    //
    // SimpleIndividual ind = new SimpleIndividual(indid,
    // rs.getString("nickname"));
    // ind.setSex(rs.getString("sex"));
    // ind.setSpecies(Global.INST.getSpecies(rs.getString("species")));
    // ind.setAlternateId(rs.getString("alternateid"));
    //
    // SimplePhoto photo = MediaAssetFactory.readPhoto(rs);
    // if (photo != null) {
    // ind.setAvatar(photo.getThumbUrl());
    // }
    //
    // return ind;
    // }

    public static void addMedia(final Database db, final int encounterid, final int mediaid) throws DatabaseException {
        Table table = db.getTable(TABLENAME_ENCOUNTER_MEDIA);
        SqlInsertFormatter formatter = new SqlInsertFormatter();
        formatter.append(PK_ENCOUNTERS, encounterid);
        formatter.append("mediaid", mediaid);
        table.insertRow(formatter);
    }

    public static void detachMedia(final Database db, final int encounterid, final int mediaid)
            throws DatabaseException {
        Table table = db.getTable(TABLENAME_ENCOUNTER_MEDIA);
        SqlWhereFormatter formatter = new SqlWhereFormatter();
        formatter.append(PK_ENCOUNTERS, encounterid);
        formatter.append("mediaid", mediaid);
        table.deleteRows(formatter.getWhereClause());
    }

    public static List<SimplePhoto> getMedia(final Database db, final int encounterid) throws DatabaseException {

        SqlStatement sql = getMediaStatement(encounterid);

        UserFactory.addAsLeftJoin(MediaAssetFactory.ALIAS_MEDIAASSET, "submitterid", sql);

        return db.selectList(sql, (rs) -> {
            return MediaAssetFactory.readPhoto(rs);
        });
    }

    public static SqlStatement getMediaStatement(final int encounterid) {
        SqlStatement sql = new SqlStatement(MediaAssetFactory.TABLENAME_MEDIAASSET, MediaAssetFactory.ALIAS_MEDIAASSET);
        sql.addInnerJoin(MediaAssetFactory.ALIAS_MEDIAASSET, MediaAssetFactory.PK_MEDIAASSET, "encounter_media", "em",
                "mediaid");
        sql.addCondition("em", PK_ENCOUNTERS, SqlRelationType.EQUAL, encounterid);
        return sql;
    }

    public static SimpleIndividual readSimpleIndividual(final RecordSet rs) throws DatabaseException {
        Individual ind = readIndividual(rs);
        if (ind == null) {
            return null;
        }
        return ind.toSimple();
    }

    private static Individual readIndividualContent(final RecordSet rs, final Integer indid) throws DatabaseException {
        Individual ind = new Individual(indid,
                                        Global.INST.getSpecies(rs.getString("species")),
                                        rs.getString("nickname"));
        ind.setSex(rs.getString("sex"));
        ind.setAlternateId(rs.getString("alternateid"));
        ind.setIdentified(rs.getBoolean("identified"));
        ind.setBio(rs.getString("bio"));
        ind.setAvatarFull(MediaAssetFactory.readPhoto(rs));

        indStore.put(ind);
        return ind;
    }

    public static Individual readIndividual(final RecordSet rs) throws DatabaseException {
        Integer indid = rs.getInteger(PK_INDIVIDUALS);
        if (indid == null) {
            return null;
        }

        Individual ind = indStore.get(indid);
        if (ind != null) {
            return ind;
        }

        return readIndividualContent(rs, indid);
    }

    public static SimpleEncounter readSimpleEncounter(final RecordSet rs) throws DatabaseException {
        return readEncounter(rs).toSimple();
    }

    public static Individual getIndividual(final Database db, final int individualId) throws DatabaseException {
        Individual ind = indStore.get(individualId);
        if (ind != null) {
            return ind;
        }

        SqlStatement sql = getIndividualStatement();
        sql.addCondition(ALIAS_INDIVIDUALS, PK_INDIVIDUALS, SqlRelationType.EQUAL, individualId);
        return db.selectFirst(sql, (rs) -> {
            return readIndividual(rs);
        });
    }

    public static Individual getIndividualByAltId(final Database db, final String alternateId)
            throws DatabaseException {
        SqlStatement sql = getIndividualStatement();

        sql.addCondition(ALIAS_INDIVIDUALS, "alternateid", SqlRelationType.EQUAL, alternateId);
        return db.selectFirst(sql, (rs) -> {
            return readIndividual(rs);
        });
    }

    public static Individual getIndividualByNickname(final Database db, final Species species, final String nickname)
            throws DatabaseException {
        if (StringUtils.isBlank(nickname)) {
            return null;
        }
        SqlStatement sql = getIndividualStatement();
        sql.addCondition(ALIAS_INDIVIDUALS, "nickname", SqlRelationType.EQUAL, nickname.toLowerCase())
                .setFunction("lower");
        sql.addCondition(ALIAS_INDIVIDUALS, "species", SqlRelationType.EQUAL, species.getCode());
        return db.selectFirst(sql, (rs) -> {
            return readIndividual(rs);
        });

    }

    public static SqlStatement getEncounterStatement() {
        return getEncounterStatement(false);
    }

    public static SqlStatement getEncounterStatement(final boolean distinct) {
        SqlStatement sql = new SqlStatement(TABLENAME_ENCOUNTERS, ALIAS_ENCOUNTERS);
        sql.addLeftOuterJoin(ALIAS_ENCOUNTERS, PK_INDIVIDUALS, TABLENAME_INDIVIDUALS, ALIAS_INDIVIDUALS,
                PK_INDIVIDUALS);
        sql.addLeftOuterJoin(ALIAS_INDIVIDUALS, "avatarid", MediaAssetFactory.TABLENAME_MEDIAASSET,
                MediaAssetFactory.ALIAS_MEDIAASSET, MediaAssetFactory.PK_MEDIAASSET);
        sql.setSelectDistinct(distinct);
        sql.addSelectTable(ALIAS_ENCOUNTERS);
        sql.addSelectTable(ALIAS_INDIVIDUALS);
        sql.addSelectTable(MediaAssetFactory.ALIAS_MEDIAASSET);

        return sql;
    }

    public static Encounter getEncountersByMedia(final Database db, final int mediaid) throws DatabaseException {
        SqlStatement sql = getEncountersByMediaStatement(mediaid);
        sql.addCondition(ALIAS_ENCOUNTER_MEDIA, "mediaid", SqlRelationType.EQUAL, mediaid);
        return db.selectFirst(sql, (rs) -> {
            return readEncounter(rs);
        });
    }

    public static SqlStatement getEncountersByMediaStatement(final int mediaid) {
        SqlStatement sql = new SqlStatement(TABLENAME_ENCOUNTERS, ALIAS_ENCOUNTERS);
        sql.addLeftOuterJoin(ALIAS_ENCOUNTERS, "encounterid", TABLENAME_ENCOUNTER_MEDIA, ALIAS_ENCOUNTER_MEDIA,
                "encounterid");
        sql.addLeftOuterJoin(ALIAS_ENCOUNTERS, PK_INDIVIDUALS, TABLENAME_INDIVIDUALS, ALIAS_INDIVIDUALS,
                PK_INDIVIDUALS);
        sql.setSelectDistinct(true);
        sql.addSelectTable(ALIAS_ENCOUNTERS);
        sql.addSelectTable(ALIAS_ENCOUNTER_MEDIA);
        sql.addSelectTable(ALIAS_INDIVIDUALS);

        return sql;
    }

    public static void saveEncounter(final Database db, final Encounter encounter) throws DatabaseException {
        if (encounter == null) {
            return;
        }

        //
        // Can't have this here since we have to be able to create an encounter
        // automatically from a set of images and attach those images and we might not have a
        // lat/long that we obtained from those images.
        //
        // if (encounter.getLocation() == null
        //     || encounter.getLocation().getLatitude() == null
        //     || encounter.getLocation().getLongitude() == null) {
        //     throw new DatabaseException("Latitude and Longitude are required for an encounter.");
        // }
        //

        boolean isNewInd = (encounter.getIndividual().getId() == null);

        saveIndividual(db, encounter.getIndividual());

        Table table = db.getTable(TABLENAME_ENCOUNTERS);

        if (encounter.getId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillEncounterFormatter(formatter, encounter);

            encounter.setId(table.insertSequencedRow(formatter, PK_ENCOUNTERS));

            //
            // Is there any reason to raise an event for a brand new individual?
            // No one would be aware of this individual since they don't exist and there has
            // been no activity, so no one to notify.
            //
            if (isNewInd) {
                Global.INST.getEventHandler().trigger(new IndividualSightedEvent(encounter.toSimple()));
            }
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillEncounterFormatter(formatter, encounter);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_ENCOUNTERS, encounter.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());

            //
            // Now update the cache with the new values. We could check to see if it's there
            // first BUT it almost assuredly is because we just edited it so it's probably near the top of the list
            // even. If it's not for some reason then it will just get added with this call which is fine.
            //
            encStore.put(encounter);
        }
    }

    public static void deleteEncounter(final Database db, final int id) throws DatabaseException {
        db.performTransaction(() -> {
            String whereClause = "encounterid = " + id;
            Table table;
            table = db.getTable(TABLENAME_ENCOUNTER_MEDIA);
            table.deleteRows(whereClause);
            table = db.getTable(TABLENAME_ENCOUNTERS);
            table.deleteRows(whereClause);
        });
        encStore.remove(id);
    }

    public static void deleteIndividual(final Database db, final int id) throws DatabaseException {
        db.getTable(TABLENAME_INDIVIDUALS).deleteRows("individualid = " + id);
        indStore.remove(id);
    }

    private static void fillEncounterFormatter(final SqlFormatter formatter, final Encounter encounter) {
        if (encounter.getIndividual() != null) {
            formatter.append("individualid", encounter.getIndividual().getId());
        }

        formatter.append("encdate", encounter.getEncDate());
        formatter.append("starttime", encounter.getStarttime());
        formatter.append("endtime", encounter.getEndtime());
        formatter.append("comments", encounter.getComments());
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

            //
            // Now update the cache with the new values. We could check to see if it's there
            // first BUT it almost assuredly is because we just edited it so it's probably near the top of the list
            // even. If it's not for some reason then it will just get added with this call which is fine.
            //
            indStore.put(individual);
        }
    }

    private static void fillIndividualFormatter(final SqlFormatter formatter, final Individual individual) {
        formatter.append("alternateid", individual.getAlternateId());
        formatter.append("identified", individual.isIdentified());
        if (individual.getSpecies() == null) {
            formatter.appendNull("species");
        } else {
            formatter.append("species", individual.getSpecies().getCode());
        }
        formatter.append("nickname", individual.getNickname());
        formatter.append("sex", individual.getSex());
        if (individual.getAvatarFull() != null) {
            formatter.append("avatarid", individual.getAvatarFull().getId());
        }
        formatter.append("bio", individual.getBio());
    }

    public static Encounter getEncounterById(final Database db, final int id) throws DatabaseException {
        //
        // ONLY return the encounter from the store IF we have BOTH the encounter
        // and the individual in the store because we can't trust the encounter to have the
        // most up-to-date individual on it. Otherwise, we just go off to the db to get the whole
        // thing. no biggy.
        //
        Encounter encounter = encStore.get(id);
        if (encounter != null) {
            Individual ind = indStore.get(encounter.getIndividual().getId());
            if (ind != null) {

                //
                // Need to update our encounter with this individual which should have just come from
                // the individual store and thus should be fresh. It's possible that the encounter was
                // holding a non-fresh individual if changes were made to it.
                //
                encounter.setIndividual(ind);
                return encounter;
            }
        }

        SqlStatement sql = getEncounterStatement();
        sql.addCondition(TABLENAME_ENCOUNTERS, PK_ENCOUNTERS, SqlRelationType.EQUAL, id);
        return db.selectFirst(sql, (rs) -> {
            return readEncounter(rs);
        });
    }

    public static EncounterObj getEncounterObj(final Database db, final int id) throws DatabaseException {
        Encounter encounter = getEncounterById(db, id);
        return getEncounterObj(db, encounter);
    }

    public static EncounterObj getEncounterObj(final Database db, final Encounter encounter) throws DatabaseException {
        EncounterObj result = new EncounterObj();
        result.encounter = encounter;
        result.photos = getMedia(db, encounter.getId());
        return result;
    }
}

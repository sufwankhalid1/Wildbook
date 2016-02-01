package org.ecocean.survey;

import java.util.List;

import org.ecocean.LocationFactory;
import org.ecocean.Point;
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

public class SurveyFactory {
    private static String TABLENAME_SURVEY = "survey";
    private static String TABLENAME_SURVEYPART = "surveypart";
    private static String TABLENAME_VESSEL = "vessel";

    public static String ALIAS_SURVEY = "s";
    public static String ALIAS_SURVEYPART = "sp";
    public static String ALIAS_VESSEL = "v";

    public static String PK_SURVEY = "surveyid";
    public static String PK_SURVEYPART = "surveypartid";
    public static String PK_VESSEL = "vesselid";

    private SurveyFactory() {
        // prevent instantiation
    }

    public static SqlStatement getSurveyStatement()
    {
        return getSurveyStatement(false);
    }

    public static SqlStatement getSurveyStatement(final boolean distinct)
    {
        SqlStatement sql = new SqlStatement(TABLENAME_SURVEY, ALIAS_SURVEY);
        sql.addInnerJoin(ALIAS_SURVEY, PK_SURVEY, TABLENAME_SURVEYPART, ALIAS_SURVEYPART, PK_SURVEY);
        sql.addLeftOuterJoin(ALIAS_SURVEYPART, PK_VESSEL, TABLENAME_VESSEL, ALIAS_VESSEL, PK_VESSEL);
        sql.addLeftOuterJoin(ALIAS_SURVEY, UserFactory.PK_ORG, UserFactory.TABLENAME_ORG, UserFactory.ALIAS_ORG, UserFactory.PK_ORG);

        if (distinct) {
            sql.setSelectDistinct(true);
            sql.addSelectTable(ALIAS_SURVEY);
            sql.addSelectTable(ALIAS_SURVEYPART);
            sql.addSelectTable(ALIAS_VESSEL);
            sql.addSelectTable(UserFactory.ALIAS_ORG);
        }
        return sql;
    }
//
//    public static SqlStatement getSurveyPartStatement() {
//        SqlStatement sql = new SqlStatement(TABLENAME_SURVEYPART, ALIAS_SURVEYPART);
//        sql.addInnerJoin(ALIAS_SURVEYPART, PK_SURVEYPART, TABLENAME_SURVEY, ALIAS_SURVEY, PK_SURVEYPART);
//        sql.addLeftOuterJoin(ALIAS_SURVEYPART, PK_VESSEL, TABLENAME_VESSEL, ALIAS_VESSEL, PK_VESSEL);
//        sql.addLeftOuterJoin(ALIAS_SURVEY, UserFactory.PK_ORG, UserFactory.TABLENAME_ORG, UserFactory.ALIAS_ORG, UserFactory.PK_ORG);
//
//        return sql;
//    }

    public static SqlStatement getVesselStatement() {
        SqlStatement sql = new SqlStatement(TABLENAME_VESSEL, ALIAS_VESSEL);
        return sql;
    }


    private static Survey readSurvey(final RecordSet rs) throws DatabaseException {
        return new Survey(rs.getInteger(PK_SURVEY),
                          UserFactory.readOrganization(rs),
                          rs.getString("surveynumber"));
    }


    private static SurveyPart readSurveyPart(final RecordSet rs) throws DatabaseException
    {
        SurveyPart surveyTrack = new SurveyPart();
        surveyTrack.setSurveyPartId(rs.getInteger("surveypartid"));
        surveyTrack.setSurveyId(rs.getInt(PK_SURVEY));
        surveyTrack.setVessel(readVessel(rs));
        surveyTrack.setPartDate(rs.getLocalDate("partdate"));
        surveyTrack.setStarttime(rs.getLocalTime("starttime"));
        surveyTrack.setEndtime(rs.getLocalTime("endtime"));

        surveyTrack.setCode(rs.getString("code"));
        surveyTrack.setComments(rs.getString("comments"));
        surveyTrack.setLocation(LocationFactory.readLocation(rs));

        return surveyTrack;
    }


    public static Point readPoint(final RecordSet rs) throws DatabaseException
    {
        return new Point(rs.getDouble("latitude"),
                         rs.getDouble("longitude"),
                         rs.getLongObj("pointtime"),
                         rs.getDoubleObj("elevation"));
    }

    public static SurveyPartObj readSurveyPartObj(final RecordSet rs) throws DatabaseException {
        SurveyPartObj surveypart = new SurveyPartObj();

        surveypart.survey = readSurvey(rs);
        surveypart.part = readSurveyPart(rs);

        return surveypart;
    }


    public static void saveSurvey(final Database db, final Survey survey) throws DatabaseException {
        if (survey == null) {
            return;
        }

        Table table = db.getTable(TABLENAME_SURVEY);

        if (survey.getSurveyId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillSurveyFormatter(formatter, survey);

            survey.setSurveyId(table.insertSequencedRow(formatter, PK_SURVEY));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillSurveyFormatter(formatter, survey);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_SURVEY, survey.getSurveyId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillSurveyFormatter(final SqlFormatter formatter, final Survey survey) {
        if (survey.getOrganization() == null) {
            formatter.append(UserFactory.PK_ORG, (Integer) null);
        } else {
            formatter.append(UserFactory.PK_ORG, survey.getOrganization().getOrgId());
        }
        formatter.append("surveynumber", survey.getSurveyNumber());
    }


    public static void saveSurveyPart(final Database db, final SurveyPart spart) throws DatabaseException {
        Table table = db.getTable(TABLENAME_SURVEYPART);

        if (spart.getSurveyPartId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillSurveyPartFormatter(formatter, spart);

            spart.setSurveyPartId(table.insertSequencedRow(formatter, PK_SURVEYPART));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillSurveyPartFormatter(formatter, spart);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_SURVEYPART, spart.getSurveyPartId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillSurveyPartFormatter(final SqlFormatter formatter, final SurveyPart spart) {
        formatter.append(PK_SURVEY, spart.getSurveyId());
        if (spart.getVessel() == null) {
            formatter.append(PK_VESSEL, (Integer) null);
        } else {
            formatter.append(PK_VESSEL, spart.getVessel().getVesselId());
        }
        formatter.append("code", spart.getCode());
        formatter.append("comments", spart.getComments());
        formatter.append("partdate", spart.getPartDate());
        formatter.append("starttime", spart.getStarttime());
        formatter.append("endtime", spart.getEndtime());
        LocationFactory.fillFormatterWithLoc(formatter, spart.getLocation());
    }

    //===================================
    // Vessel stuff
    //===================================

    public static Vessel readVessel(final RecordSet rs) throws DatabaseException {
        Integer vesselId = rs.getInteger(PK_VESSEL);

        if (vesselId == null) {
            return null;
        }

        return new Vessel(vesselId, rs.getInt(UserFactory.PK_ORG), rs.getInt("vesseltypeid"), rs.getString("vesselname"));
    }

    public static List<Vessel> getVesselsByOrg(final Database db, final int orgid) throws DatabaseException {
        SqlStatement sql = SurveyFactory.getVesselStatement();
        sql.addCondition(ALIAS_VESSEL, UserFactory.PK_ORG, SqlRelationType.EQUAL, orgid);

        return db.selectList(sql, (rs) -> {
            return readVessel(rs);
        });
    }

    public static void saveVessel(final Database db, final Vessel vessel) throws DatabaseException {
        Table table = db.getTable(TABLENAME_VESSEL);

        if (vessel.getVesselId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillVesselFormatter(formatter, vessel);

            vessel.setVesselId(table.insertSequencedRow(formatter, PK_VESSEL));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillVesselFormatter(formatter, vessel);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_VESSEL, vessel.getVesselId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillVesselFormatter(final SqlFormatter formatter, final Vessel vessel) {
        formatter.append(UserFactory.PK_ORG, vessel.getOrgId());
        formatter.append("vesseltype", vessel.getTypeId());
        formatter.append("vesselname", vessel.getName());
    }
}

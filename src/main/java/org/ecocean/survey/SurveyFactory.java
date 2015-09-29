package org.ecocean.survey;

import org.ecocean.LocationFactory;
import org.ecocean.Point;
import org.ecocean.security.UserFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
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

    private SurveyFactory() {
        // prevent instantiation
    }

    public static SqlStatement getSurveyStatement()
    {
        SqlStatement sql = new SqlStatement(TABLENAME_SURVEY,
                                            ALIAS_SURVEY,
                                            ALIAS_SURVEY + ".*, " + ALIAS_SURVEYPART + ".*, " + ALIAS_VESSEL + ".*");
        sql.addInnerJoin(ALIAS_SURVEY, PK_SURVEY, TABLENAME_SURVEYPART, ALIAS_SURVEYPART, PK_SURVEY);
        sql.addLeftOuterJoin(ALIAS_SURVEYPART, "vesselid", TABLENAME_VESSEL, ALIAS_VESSEL, "vesselid");
        sql.addLeftOuterJoin(ALIAS_SURVEY, "orgid", UserFactory.TABLENAME_ORG, UserFactory.ALIAS_ORG, "orgid");
        return sql;
    }

    public static SqlStatement getSurveyStatement(final boolean distinct)
    {
        SqlStatement sql = getSurveyStatement();
        sql.setSelectDistinct(true);
        sql.addSelectTable(ALIAS_SURVEY);
        sql.addSelectTable(ALIAS_SURVEYPART);
        sql.addSelectTable(UserFactory.ALIAS_ORG);
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
        surveyTrack.setStarttime(rs.getOffsetTime("starttime"));
        surveyTrack.setEndtime(rs.getOffsetTime("endtime"));

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
        SurveyPartObj part = new SurveyPartObj();

        part.survey = readSurvey(rs);
        part.part = readSurveyPart(rs);

        return part;
    }


    public static void saveSurvey(final Database db, final Survey survey) throws DatabaseException {
        Table table = db.getTable(TABLENAME_VESSEL);

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
            formatter.append("orgid", (Integer) null);
        } else {
            formatter.append("orgid", survey.getOrganization().getOrgId());
        }
        formatter.append("surveynumber", survey.getSurveyNumber());
    }


    public static void saveSurveyPart(final Database db, final SurveyPart spart) throws DatabaseException {
        Table table = db.getTable(TABLENAME_VESSEL);

        if (spart.getSurveyPartId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillSurveyPartFormatter(formatter, spart);

            spart.setSurveyId(table.insertSequencedRow(formatter, PK_SURVEYPART));
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
            formatter.append("vesselid", (Integer) null);
        } else {
            formatter.append("vesselid", spart.getVessel().getVesselId());
        }
        formatter.append("code", spart.getCode());
        formatter.append("comments", spart.getComments());
        formatter.append("starttime", spart.getStarttime().toString());
        formatter.append("endtime", spart.getEndtime().toString());
        LocationFactory.fillFormatterWithLoc(formatter, spart.getLocation());
    }

    //===================================
    // Vessel stuff
    //===================================

    public static Vessel readVessel(final RecordSet rs) throws DatabaseException {
        Integer vesselId = rs.getInteger("vesselid");

        if (vesselId == null) {
            return null;
        }

        return new Vessel(vesselId,
                          rs.getInt("orgid"),
                          rs.getString("type"),
                          rs.getString("name"));
    }


    public static void saveVessel(final Database db, final Vessel vessel) throws DatabaseException {
        Table table = db.getTable(TABLENAME_VESSEL);

        if (vessel.getVesselId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillOrgFormatter(formatter, vessel);

            vessel.setVesselId(table.insertSequencedRow(formatter, "vesselid"));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillOrgFormatter(formatter, vessel);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("vesselid", vessel.getVesselId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillOrgFormatter(final SqlFormatter formatter, final Vessel vessel) {
        formatter.append("orgid", vessel.getOrgId());
        formatter.append("type", vessel.getType());
        formatter.append("name", vessel.getName());
    }
}

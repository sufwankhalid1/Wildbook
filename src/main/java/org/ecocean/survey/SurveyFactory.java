package org.ecocean.survey;

import org.ecocean.Point;
import org.ecocean.rest.SimpleFactory;
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

    private SurveyFactory() {
        // prevent instantiation
    }

    public static SqlStatement getSqlStatement()
    {
        SqlStatement sql = new SqlStatement(TABLENAME_SURVEY,
                                            ALIAS_SURVEY,
                                            ALIAS_SURVEY + ".*, " + ALIAS_SURVEYPART + ".*, " + ALIAS_VESSEL + ".*");
        sql.addInnerJoin(ALIAS_SURVEY, "surveyid", TABLENAME_SURVEYPART, ALIAS_SURVEYPART, "surveyid");
        sql.addLeftOuterJoin(ALIAS_SURVEYPART, "vesselid", TABLENAME_VESSEL, ALIAS_VESSEL, "vesselid");
        sql.addLeftOuterJoin(ALIAS_SURVEY, "orgid", UserFactory.TABLENAME_ORG, UserFactory.ALIAS_ORG, "orgid");
        return sql;
    }


//    private static SurveyPart getSurveyTrack(final Database db, final int id) throws DatabaseException
//    {
//        SqlStatement sql = new SqlStatement(TABLENAME_SURVEYPART, ALIAS_SURVEYPART);
//        sql.addLeftOuterJoin(ALIAS_SURVEYPART, "surveypartid", "surveypoint", "p", "surveypartid");
//        sql.addCondition(ALIAS_SURVEYPART, "surveypartid", SqlRelationType.EQUAL, id);
//        sql.setOrderBy("p.pointtime");
//
//        RecordSet rs = db.getRecordSet(sql.getSql());
//        return readSurveyTrack(rs);
//    }

    private static Survey readSurvey(final RecordSet rs) throws DatabaseException {
        return new Survey(rs.getInteger("surveyid"),
                          UserFactory.readOrganization(rs),
                          rs.getString("surveynumber"));
    }


    private static SurveyPart readSurveyPart(final RecordSet rs) throws DatabaseException
    {
        SurveyPart surveyTrack = new SurveyPart();
        surveyTrack.setSurveyPartId(rs.getInteger("surveypartid"));
        surveyTrack.setSurveyId(rs.getInt("surveyid"));
        surveyTrack.setVessel(readVessel(rs));
        surveyTrack.setPartDate(rs.getLocalDate("partdate"));
        surveyTrack.setStarttime(rs.getOffsetTime("starttime"));
        surveyTrack.setEndtime(rs.getOffsetTime("endtime"));

        surveyTrack.setCode(rs.getString("code"));
        surveyTrack.setComments(rs.getString("comments"));
        surveyTrack.setLocation(SimpleFactory.readLocation(rs));

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

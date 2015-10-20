package db.migration;

import java.sql.Connection;

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import com.samsix.database.Database;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.Table;
import com.samsix.util.string.StringUtilities;

public class V2015_08_24_14_40_39__convert_encounters implements JdbcMigration {
    @Override
    public void migrate(final Connection connection) throws Exception {
        try (Database db = new Database(connection)) {
            RecordSet rs;
            Table tableOldInd = db.getTable("MARKEDINDIVIDUAL");
            Table tableInd = db.getTable("individuals");
            Table tableIndConvert = db.getTable("temp_ind_convert");
            rs = tableOldInd.getRecordSet();
            while (rs.next()) {
                String oldId = rs.getString("INDIVIDUALID");

                //
                // NOTE: The alternateid with CRC prefix is specific to happywhale. Will need to do this
                // differently in general.
                //
                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter.append("alternateid", "CRC - " + oldId);
                formatter.append("dateidentified", rs.getString("DATEFIRSTIDENTIFIED"));
                formatter.append("sex", rs.getString("SEX"));
                formatter.append("nickname", rs.getString("NICKNAME"));
                int individualid = tableInd.insertSequencedRow(formatter, "individualid");

                formatter = new SqlInsertFormatter();
                formatter.append("oldindividualid", oldId);
                formatter.append("individualid", individualid);
                tableIndConvert.insertRow(formatter);
            }

            Table tableOldEnc = db.getTable("ENCOUNTER");
            Table tableEnc = db.getTable("encounters");
            Table tableEncConvert = db.getTable("temp_enc_convert");

            rs = tableOldEnc.getRecordSet();
            while (rs.next()) {
                String catalogNumber = rs.getString("CATALOGNUMBER");
                String oldId = rs.getString("INDIVIDUALID");
                Integer individualId = null;
                RecordSet rs2 = tableIndConvert.getRecordSet("oldindividualid = " + StringUtilities.wrapQuotes(oldId));
                if (rs2.next()) {
                    individualId = rs2.getInteger("individualid");
                }

                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter.append("individualid", individualId);
                formatter.append("encdate", rs.getLong("DATEINMILLISECONDS"));
                formatter.append("latitude", rs.getDoubleObj("DECIMALLATITUDE"));
                formatter.append("longitude", rs.getDoubleObj("DECIMALLONGITUDE"));
                formatter.append("state", rs.getString("STATE"));
                String submitterId = rs.getString("SUBMITTERID");
                if (StringUtils.isBlank(submitterId)) {
                    formatter.append("submitter", rs.getString("SUBMITTEREMAIL"));
                } else {
                    formatter.append("submitter", submitterId);
                }
                formatter.append("locationid", rs.getString("LOCATIONID"));
                formatter.append("verbatimLocation", rs.getString("VERBATIMLOCALITY"));

                int encounterid = tableEnc.insertSequencedRow(formatter, "encounterid");

                formatter = new SqlInsertFormatter();
                formatter.append("catalognumber", catalogNumber);
                formatter.append("encounterid", encounterid);
                tableEncConvert.insertRow(formatter);
            }


            String sql;
            sql = "select encounterid, mediaid from \"ENCOUNTER_IMAGES\" ei"
                    + " inner join temp_enc_convert tec on tec.catalognumber = ei.\"CATALOGNUMBER_OID\""
                    + " inner join temp_spv_media tsm on tsm.spvid = ei.\"DATACOLLECTIONEVENTID_EID\"";
            Table tableEncMedia = db.getTable("encounter_media");
            rs = db.getRecordSet(sql);
            while (rs.next()) {
                SqlInsertFormatter formatter = new SqlInsertFormatter();
                formatter = new SqlInsertFormatter();
                formatter.append("encounterid", rs.getInt("encounterid"));
                formatter.append("mediaid", rs.getInt("mediaid"));
                tableEncMedia.insertRow(formatter);
            }
        }
    }
}

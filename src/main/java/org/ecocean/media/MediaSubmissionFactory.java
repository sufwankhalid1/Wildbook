package org.ecocean.media;

import java.util.ArrayList;
import java.util.List;

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

public class MediaSubmissionFactory {
    public final static String TABLENAME_MEDIASUBMISSION = "mediasubmission";
    public final static String ALIAS_MEDIASUBMISSION = "ms";

    private MediaSubmissionFactory() {
        // prevent instantiation
    }

    public static SqlStatement getStatement()
    {
        SqlStatement sql = new SqlStatement(TABLENAME_MEDIASUBMISSION, ALIAS_MEDIASUBMISSION);
        sql.addLeftOuterJoin(ALIAS_MEDIASUBMISSION,
                UserFactory.PK_USERS,
                UserFactory.TABLENAME_USERS,
                UserFactory.AlIAS_USERS,
                UserFactory.PK_USERS);
        sql.addLeftOuterJoin(UserFactory.AlIAS_USERS,
                "avatarid",
                MediaAssetFactory.TABLENAME_MEDIAASSET,
                MediaAssetFactory.ALIAS_MEDIAASSET,
                "id");
        return sql;
    }


    public static void save(final Database db,
                            final MediaSubmission media)
        throws DatabaseException
    {
        Table table = db.getTable(TABLENAME_MEDIASUBMISSION);
        if (media.getId() == null) {
            SqlInsertFormatter formatter;
            formatter = new SqlInsertFormatter();
            fillFormatter(db, formatter, media);
            media.setId(table.insertSequencedRow(formatter, "id"));
        } else {
            SqlUpdateFormatter formatter;
            formatter = new SqlUpdateFormatter();
            formatter.append("id", media.getId());
            fillFormatter(db, formatter, media);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("id", media.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private static void fillFormatter(final Database db,
                                      final SqlFormatter formatter,
                                      final MediaSubmission media)
    {
        formatter.append("description", media.getDescription());
        formatter.append("email", media.getEmail());
        formatter.append("endtime", media.getEndTime());
        formatter.append("latitude", media.getLatitude());
        formatter.append("longitude", media.getLongitude());
        formatter.append("name", media.getName());
        formatter.append("starttime", media.getStartTime());
        formatter.append("submissionid", media.getSubmissionid());
        formatter.append("timesubmitted", media.getTimeSubmitted());
        if (media.getUser() != null) {
            formatter.append("userid", media.getUser().getId());
        }
        formatter.append("verbatimlocation", media.getVerbatimLocation());
        formatter.append("status", media.getStatus());
    }


    public static List<MediaAsset> getMedia(final Database db,
                                            final long msid) throws DatabaseException
    {
        String sql = "SELECT ma.* FROM mediasubmission_media m"
                + " INNER JOIN mediaasset ma ON ma.id = m.mediaid"
                + " WHERE m.mediasubmissionid = " + msid;
        RecordSet rs = db.getRecordSet(sql);
        List<MediaAsset> media = new ArrayList<>();
        while (rs.next()) {
            media.add(MediaAssetFactory.valueOf(rs));
        }

        return media;
    }


    public static MediaSubmission readMediaSubmission(final RecordSet rs) throws DatabaseException
    {
        MediaSubmission ms = new MediaSubmission();
        ms.setDescription(rs.getString("description"));
        ms.setEmail(rs.getString("email"));
        ms.setEndTime(rs.getLongObj("endtime"));
        ms.setId(rs.getInteger("id"));
        ms.setLatitude(rs.getDoubleObj("latitude"));
        ms.setLongitude(rs.getDoubleObj("longitude"));
        ms.setName(rs.getString("name"));
        ms.setStartTime(rs.getLongObj("starttime"));
        ms.setSubmissionid(rs.getString("submissionid"));
        ms.setTimeSubmitted(rs.getLongObj("timesubmitted"));
        ms.setUser(UserFactory.readSimpleUser(rs));
        ms.setVerbatimLocation(rs.getString("verbatimlocation"));
        ms.setStatus(rs.getString("status"));

        return ms;
    }
}

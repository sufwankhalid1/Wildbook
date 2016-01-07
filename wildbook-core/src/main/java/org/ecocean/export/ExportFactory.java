package org.ecocean.export;

import java.util.ArrayList;
import java.util.List;

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

public class ExportFactory {
    public static final String TABLENAME_EXPORT = "exports";
    public static final String ALIAS_EXPORT = "x";
    public static final String PK_EXPORT = "exportid";

    private ExportFactory() {
        // prevent instantiation
    }

    public static void save(final Database db, final Export export) throws DatabaseException {
        if (export == null) {
            return;
        }

        Table table = db.getTable(TABLENAME_EXPORT);

        if (export.getExportId() == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillExportFormatter(formatter, export);

            export.setExportId(table.insertSequencedRow(formatter, PK_EXPORT));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillExportFormatter(formatter, export);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_EXPORT, export.getExportId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

    private static void fillExportFormatter(final SqlFormatter formatter, final Export export) {
        formatter.append("userid", export.getUserId());
        formatter.append("type", export.getType());
        formatter.append("outputdir", export.getOutputdir());
        formatter.append("status", export.getStatus());
        formatter.append("error", export.getError());
        formatter.append("delivered", export.isDelivered());
        formatter.append("parameters", export.getParamters());
    }

    public static List<Export> getUserUndeliveredExports(final Database db, final int userid) throws DatabaseException{
        List<Export> exports = new ArrayList<>();

        SqlStatement sql = new SqlStatement(ExportFactory.TABLENAME_EXPORT,
                                            ExportFactory.ALIAS_EXPORT);
                sql.addCondition(ALIAS_EXPORT, "userid", SqlRelationType.EQUAL, userid);
                sql.addCondition(ALIAS_EXPORT, "delivered", false);
                sql.setOrderBy(ALIAS_EXPORT, "datetimestamp", true);

        db.select(sql, (rs) -> {
            exports.add(readExport(rs));
        });

        return exports;
    }

    public static Export readExport(final RecordSet rs) throws DatabaseException {
        Integer id = rs.getInteger(PK_EXPORT);
        if (id == null) {
            return null;
        }

        Export export = new Export();

        export.setExportId(rs.getInt("exportid"));
        export.setTimestamp(rs.getDate("datetimestamp"));
        export.setStatus(rs.getInt("status"));
        export.setError(rs.getString("error"));
        export.setOutputdir(rs.getString("outputDir"));
        export.setType(rs.getString("type"));

        return export;
    }
}

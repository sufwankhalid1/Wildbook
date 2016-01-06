package org.ecocean.export;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class ExportFactory {
    public static final String TABLENAME_EXPORT = "exports";
    public static final String ALIAS_EXPORTS = "x";
    public static final String PK_EXPORTS = "exportid";

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

            export.setExportId(table.insertSequencedRow(formatter, PK_EXPORTS));
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillExportFormatter(formatter, export);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_EXPORTS, export.getExportId());
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
    }
}

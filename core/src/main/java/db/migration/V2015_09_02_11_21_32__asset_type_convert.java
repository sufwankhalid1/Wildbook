package db.migration;

import java.sql.Connection;

import org.ecocean.media.MediaAssetType;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import com.samsix.database.Database;
import com.samsix.database.RecordSet;
import com.samsix.database.Table;

public class V2015_09_02_11_21_32__asset_type_convert implements JdbcMigration {
    @Override
    public void migrate(final Connection connection) throws Exception {
        try (Database db = new Database(connection)) {
            Table table = db.getTable("mediaasset");
            String sql = "SELECT id, path FROM mediaasset";
            RecordSet rs = db.getRecordSet(sql);
            while (rs.next()) {
                MediaAssetType type = MediaAssetType.fromFilename(rs.getString("path"));
                table.updateRow("type = " + type.getCode(), "id = " + rs.getInt("id"));
            }
        }
    }
}

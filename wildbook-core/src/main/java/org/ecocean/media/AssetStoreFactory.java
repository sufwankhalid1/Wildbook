package org.ecocean.media;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class AssetStoreFactory {
    private static Logger logger = LoggerFactory.getLogger(AssetStoreFactory.class);

    private static final String TABLENAME_ASSETSTORE = "assetstore";

    private AssetStoreFactory() {
        // do not instantiate
    }

    public static List<AssetStore> getStores(final Database db) throws DatabaseException {
        Table table = db.getTable(TABLENAME_ASSETSTORE);
        return table.selectList((rs) -> {
            return buildAssetStore(rs.getInteger("id"),
                                   rs.getString("name"),
                                   AssetStoreType.valueOf(rs.getString("type")),
                                   new AssetStoreConfig(rs.getString("config")),
                                   rs.getBoolean("writable"));
        });
    }

    private static AssetStore buildAssetStore(final Integer id,
                                              final String name,
                                              final AssetStoreType type,
                                              final AssetStoreConfig config,
                                              final boolean writable)
    {
        if (name == null) throw new IllegalArgumentException("null asset store name");
        if (type == null) throw new IllegalArgumentException("null asset store type");

        switch (type) {
        case LOCAL:
            return new LocalAssetStore(id, name, config, writable);
        default:
            logger.error("Unhandled asset store type: " + type);
            return null;
        }
    }


    private static void fillFormatter(final SqlFormatter formatter, final AssetStore store) {
        formatter.append("name", store.name);
        formatter.append("type", store.type.name());
        formatter.append("config", store.config.configString());
        formatter.append("writable", store.writable);
    }


    public static void save(final Database db, final AssetStore store) throws DatabaseException {
        Table table = db.getTable(TABLENAME_ASSETSTORE);

        if (store.id == null) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillFormatter(formatter, store);

            store.id = table.insertSequencedRow(formatter, "id");

            AssetStore.add(store);
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillFormatter(formatter, store);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("id", store.id);
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    public static void delete(final Database db, final AssetStore store) throws DatabaseException {
        if (store.id == null) {
            return;
        }

        Table table = db.getTable(TABLENAME_ASSETSTORE);

        table.deleteRows("id = " + store.id);

        AssetStore.remove(store);
    }
}

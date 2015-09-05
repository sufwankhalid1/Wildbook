package org.ecocean.media;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class MediaAssetFactory {
    public static final String TABLE_NAME = "mediaasset";
    public static final int NOT_SAVED = -1;


    private MediaAssetFactory() {
        // prevent instantiation
    }


    /**
     * Fetch a single asset from the database by id.
     */
    public static MediaAsset load(final Database db, final long id)
        throws DatabaseException
    {
        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("id", id);

        Table table = db.getTable(TABLE_NAME);

        RecordSet rs = table.getRecordSet(where.getWhereClause(), 1);
        if (rs.next()) {
            return valueOf(rs);
        }
        return null;
    }


    private static Path createPath(final String pathstr)
    {
        if (pathstr == null) {
            return null;
        }

        return new File(pathstr).toPath();
    }


    public static MediaAsset valueOf(final RecordSet rs) throws DatabaseException
    {
        //
        // Will happen if in a left join query there is no associated media asset.
        //
        if (! rs.hasColumn("id")) {
            return null;
        }

        MediaAsset ma = new MediaAsset(rs.getInt("id"),
                                       AssetStore.get(rs.getInteger("store")),
                                       createPath(rs.getString("path")),
                                       MediaAssetType.fromCode(rs.getInt("type")),
                                       rs.getString("category"));
        ma.parentId = rs.getInteger("parent");
        ma.rootId = rs.getInteger("root");
        String[] tags =  (String[]) rs.getArray("tags");
        if (tags != null) {
            ma.tags = new HashSet<String>(Arrays.asList(tags));
        }
        ma.thumbStore = AssetStore.get(rs.getInteger("thumbstore"));
        ma.thumbPath = createPath(rs.getString("thumbpath"));
        ma.submitter = rs.getString("submitter");

        return ma;
    }


    /**
     * Store to the given database.
     */
    public static void save(final Database db, final MediaAsset ma) throws DatabaseException {
        Table table = db.getTable(TABLE_NAME);

        if (ma.id == NOT_SAVED) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillFormatter(formatter, ma);

            ma.id = table.insertSequencedRow(formatter, "id");
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillFormatter(formatter, ma);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("id", ma.id);
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

    /**
     * Fill in formatter values from our properties.
     */
    private static void fillFormatter(final SqlFormatter formatter, final MediaAsset ma) {
        formatter.append("store", ma.store.id);
        formatter.append("path", ma.path.toString());
        formatter.append("type", ma.type.getCode());
        formatter.append("category", ma.category);
        formatter.append("parent", ma.parentId);
        formatter.append("root", ma.rootId);
        formatter.append("thumbstore", ma.thumbStore.id);
        formatter.append("thumbpath", ma.thumbPath.toString());
        String tags;
        if (ma.tags == null) {
            tags = null;
        } else {
            StringBuffer tagb = new StringBuffer("{");
            boolean isFirst = true;
            for (String tag : ma.tags) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    tagb.append(",");
                }
                tagb.append("\"").append(tag).append("\"");
            }
            tags = tagb.append("}").toString();
        }
        formatter.append("tags", tags);
        formatter.append("submitter", ma.getSubmitter());
    }

    /**
     * Delete this asset and any child assets from the given database.
     * Does not delete any asset files.
     *
     * @param db Database where the asset lives.
     */
    public static void delete(final Database db, final int id) throws DatabaseException {
        Table table = db.getTable(TABLE_NAME);
        table.deleteRows("id = " + id);
    }


    public static void deleteFromStore(final MediaAsset ma) {
        if (ma.thumbStore != null) {
            ma.thumbStore.deleteFrom(ma.thumbPath);
        }
        ma.store.deleteFrom(ma.path);
    }
}

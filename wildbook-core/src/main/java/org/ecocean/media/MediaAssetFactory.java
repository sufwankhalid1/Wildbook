package org.ecocean.media;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.ecocean.rest.SimplePhoto;
import org.ecocean.security.UserFactory;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlStatement;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

public class MediaAssetFactory {
    private static final Logger logger = LoggerFactory.getLogger(MediaAssetFactory.class);

    public static final String TABLENAME_MEDIAASSET = "mediaasset";
    public static final String ALIAS_MEDIAASSET = "ma";
    public static final String PK_MEDIAASSET = "id";

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
        where.append(PK_MEDIAASSET, id);

        Table table = db.getTable(TABLENAME_MEDIAASSET);

        RecordSet rs = table.getRecordSet(where.getWhereClause(), 1);
        if (rs.next()) {
            return valueOf(rs);
        }
        return null;
    }

    private static void addSelectColumn(final SqlStatement sql, final String alias, final String colName) {
        sql.addSelect(alias, colName, alias + colName);
    }

    public static void addSelectClause(final SqlStatement sql, final String alias) {
        addSelectColumn(sql, alias, "store");

        addSelectColumn(sql, alias, "store");
        addSelectColumn(sql, alias, "category");
        addSelectColumn(sql, alias, "parent");
        addSelectColumn(sql, alias, "tags");
        addSelectColumn(sql, alias, "root");
        addSelectColumn(sql, alias, "type");
        addSelectColumn(sql, alias, "path");
        addSelectColumn(sql, alias, "thumbstore");
        addSelectColumn(sql, alias, "thumbpath");
        addSelectColumn(sql, alias, "midpath");
        addSelectColumn(sql, alias, "metatimestamp");
        addSelectColumn(sql, alias, "metalat");
        addSelectColumn(sql, alias, "metalong");
        addSelectColumn(sql, alias, "meta");
        addSelectColumn(sql, alias, "submittedon");
        addSelectColumn(sql, alias, "submitterid");
        addSelectColumn(sql, alias, "id");

    }


    private static Path createPath(final String pathstr)
    {
        if (pathstr == null) {
            return null;
        }

        return Paths.get(pathstr);
    }

    public static MediaAsset valueOf(final RecordSet rs) throws DatabaseException {
        return valueOf(rs, "");
    }


    public static MediaAsset valueOf(final RecordSet rs, final String prefix) throws DatabaseException
    {
        //
        // Will happen if in a left join query there is no associated media asset.
        //
        if (! rs.hasColumn(PK_MEDIAASSET)) {
            return null;
        }

        Integer id = rs.getInteger(prefix + PK_MEDIAASSET);

        if (id == null) {
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.quickLog("storeid", rs.getInteger("store")));
            logger.debug(LogBuilder.quickLog("store", AssetStore.get(rs.getInteger("store"))));
        }

        MediaAsset ma = new MediaAsset(rs.getInt(PK_MEDIAASSET),
                                       AssetStore.get(rs.getInteger(prefix + "store")),
                                       createPath(rs.getString(prefix + "path")),
                                       MediaAssetType.fromCode(rs.getInt(prefix + "type")),
                                       rs.getString(prefix + "category"));
        ma.parentId = rs.getInteger(prefix + "parent");
        ma.rootId = rs.getInteger(prefix + "root");
        String[] tags =  (String[]) rs.getArray(prefix + "tags");
        if (tags != null) {
            ma.tags = new HashSet<String>(Arrays.asList(tags));
        }
        ma.thumbStore = AssetStore.get(rs.getInteger(prefix + "thumbstore"));
        ma.thumbPath = createPath(rs.getString(prefix + "thumbpath"));
        ma.midPath = createPath(rs.getString(prefix + "midpath"));
        ma.submitterid = rs.getInteger(prefix + "submitterid");
        ma.setSubmitter(UserFactory.readSimpleUser(rs));
        ma.setMetaTimestamp(rs.getLocalDateTime(prefix + "metatimestamp"));
        ma.setMetaLatitude(rs.getDoubleObj(prefix + "metalat"));
        ma.setMetaLongitude(rs.getDoubleObj(prefix + "metalong"));

        String meta = rs.getString(prefix + "meta");
        if (meta != null) {
            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
            ma.setMeta(new Gson().fromJson(meta, type));
        }

        return ma;
    }


    /**
     * Store to the given database.
     */
    public static void save(final Database db, final MediaAsset ma) throws DatabaseException {
        Table table = db.getTable(TABLENAME_MEDIAASSET);

        if (ma.id == NOT_SAVED) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillFormatter(formatter, ma);

            ma.id = table.insertSequencedRow(formatter, PK_MEDIAASSET);
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillFormatter(formatter, ma);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append(PK_MEDIAASSET, ma.id);
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
        if (ma.thumbStore != null) {
            formatter.append("thumbstore", ma.thumbStore.id);
        } else {
            formatter.appendNull("thumbstore");
        }
        if (ma.thumbPath != null) {
            formatter.append("thumbpath", ma.thumbPath.toString());
        } else {
            formatter.appendNull("thumbpath");
        }
        if (ma.midPath != null) {
            formatter.append("midpath", ma.midPath.toString());
        } else {
            formatter.appendNull("midpath");
        }
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
        formatter.append("submitterid", ma.getSubmitterId());
        formatter.append("submittedon", ma.getSubmittedOn());
        formatter.append("metatimestamp", ma.getMetaTimestamp());
        formatter.append("metalat", ma.getMetaLatitude());
        formatter.append("metalong", ma.getMetaLongitude());
        if (ma.getMeta() == null) {
            formatter.appendNull("meta");
        } else {
            formatter.append("meta", new Gson().toJson(ma.getMeta()));
        }
    }

    /**
     * Delete this asset and any child assets from the given database.
     * Does not delete any asset files.
     *
     * @param db Database where the asset lives.
     */
    public static void delete(final Database db, final int id) throws DatabaseException {
        //
        // We might want to make all the foreign key checks and report them back as a message
        // instead of letting the db throw an exception. If so, here are the keys.
        //
        //  TABLE "encounter_media" CONSTRAINT "encounter_media_mediaid_fkey" FOREIGN KEY (mediaid) REFERENCES mediaasset(id)
        //  TABLE "idservice" CONSTRAINT "idservice_mediaid_fkey" FOREIGN KEY (mediaid) REFERENCES mediaasset(id)
        //  TABLE "individuals" CONSTRAINT "individuals_avatarid_fkey" FOREIGN KEY (avatarid) REFERENCES mediaasset(id)
        //  TABLE "mediaasset" CONSTRAINT "mediaasset_parent_fkey" FOREIGN KEY (parent) REFERENCES mediaasset(id) ON DELETE CASCADE
        //  TABLE "mediasubmission_media" CONSTRAINT "mediasubmission_media_mediaid_fkey2" FOREIGN KEY (mediaid) REFERENCES mediaasset(id)
        //
        Table table = db.getTable(TABLENAME_MEDIAASSET);
        table.deleteRows("id = " + id);
    }


    public static void deleteFromStore(final MediaAsset ma) throws IOException {
        if (ma.thumbStore != null) {
            ma.thumbStore.deleteFrom(ma.thumbPath);
            ma.thumbStore.deleteFrom(ma.midPath);
        }
        ma.store.deleteFrom(ma.path);
    }

    public static SimplePhoto toSimplePhoto(final MediaAsset ma) {
        if (ma == null) {
            return null;
        }

        SimplePhoto photo = new SimplePhoto(ma.getID(), ma.webPathString(), ma.thumbWebPathString(), ma.midWebPathString());
        photo.setTimestamp(ma.getMetaTimestamp());
        photo.setLatitude(ma.getMetaLatitude());
        photo.setLongitude(ma.getMetaLongitude());
        photo.setMeta(ma.getMeta());
        photo.setSubmitterid(ma.getSubmitterId());
        photo.setSubmitter(ma.getSubmitter());
        photo.setSubmittedOn(ma.getSubmittedOn());
        return photo;
    }

    public static SimplePhoto readPhoto(final RecordSet rs) throws DatabaseException
    {
        return readPhoto(rs, "");
    }


    public static SimplePhoto readPhoto(final RecordSet rs, final String prefix) throws DatabaseException
    {
        return toSimplePhoto(valueOf(rs, prefix));
    }


    public static void deleteMedia(final Database db,
                                   final int submissionid,
                                   final int mediaid) throws DatabaseException, IOException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting mediaid [" + mediaid + "] from submission [" + submissionid + "]");
        }

        MediaAsset ma = load(db, mediaid);

        if (ma == null) {
            throw new IllegalArgumentException("No media with id [" + mediaid + "] found.");
        }

        Table table = db.getTable("mediasubmission_media");
        String where = "mediasubmissionid = "
                + submissionid
                + " AND mediaid = "
                + mediaid;
        table.deleteRows(where);

        delete(db, ma.getID());
        deleteFromStore(ma);
    }
}

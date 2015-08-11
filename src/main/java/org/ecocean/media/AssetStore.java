/*
 * This file is a part of Wildbook.
 * Copyright (C) 2015 WildMe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wildbook.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ecocean.media;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.ecocean.ShepherdPMF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

/**
 * AssetStore describes a location and methods for access to a set of
 * MediaAssets.  Concrete subtypes fill in the "hows".
 *
 * @see LocalAssetStore
 */
public abstract class AssetStore {
    private static Logger log = LoggerFactory.getLogger(AssetStore.class);
    private static final String TABLE_NAME = "assetstore";
    private static Map<Long, AssetStore> stores;

    public static final long NOT_SAVED = -1;
    public long id = NOT_SAVED;
    public String name;
    public AssetStoreType type = AssetStoreType.LOCAL;
    public AssetStoreConfig config;
    public boolean writable = true;


    /**
     * Create a new AssetStore.
     */
    protected AssetStore(final long id, final String name,
                         final AssetStoreType type,
                         final AssetStoreConfig config,
                         final boolean writable)
    {
        if (name == null) throw new IllegalArgumentException("null name");
        if (type == null) throw new IllegalArgumentException("null type");

        this.id = id;
        this.name = name;
        this.type = type;
        this.config = config;
        this.writable = writable;
    }


    private static Map<Long, AssetStore> getMap() throws DatabaseException
    {
        if (stores != null) {
            return stores;
        }

        stores = new HashMap<Long, AssetStore>();

        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
            RecordSet rs;
            Table table = db.getTable(TABLE_NAME);

            rs = table.getRecordSet();
            while (rs.next()) {
                AssetStore store;
                store = buildAssetStore(rs.getLong("id"),
                                        rs.getString("name"),
                                        AssetStoreType.valueOf(rs.getString("type")),
                                        new AssetStoreConfig(rs.getString("config")),
                                        rs.getBoolean("writable"));
                stores.put(store.id, store);
            }
        }

        return stores;
    }

    public static AssetStore get(final Long id)
    {
        return stores.get(id);
    }

    public static AssetStore get(final String name) throws DatabaseException
    {
        for (AssetStore store : getMap().values()) {
            if (store.name != null && store.name.equals(name)) {
                return store;
            }
        }

        return null;
    }

    /**
     * Create a new AssetStore.  Used in load().
     */
    private static AssetStore buildAssetStore(final long id,
                                              final String name,
                                              final AssetStoreType type,
                                              final AssetStoreConfig config,
                                              final boolean writable)
    {
        if (name == null) throw new IllegalArgumentException("null name");
        if (type == null) throw new IllegalArgumentException("null type");

        if (type == AssetStoreType.LOCAL) {
            return new LocalAssetStore(id, name, config, writable);
        } else {
            log.error("Unhandled asset store type: " + type);
            return null;
        }
    }

    //
    // do stuff
    //

    public abstract URL webPath(MediaAsset asset);

    public abstract MediaAsset create(Path path, AssetType type);

    public abstract MediaAsset create(String path, AssetType type);

    /**
     * Create a new asset from the given form submission part.  The
     * file is copied in to the store as part of this process.
     *
     * @param db Database to store the new asset in.
     *
     * @param file File to copy in.
     *
     * @param path The (optional) subdirectory and (required) filename
     * relative to the asset store root in which to store the file.
     *
     * @param type Probably AssetType.ORIGINAL.
     */
    public abstract MediaAsset copyIn(Database db, File file,
                                      String path, AssetType type)
        throws IOException;


    /**
     * Fetch the default store (the one with the highest id) from the
     * database.
     */
    public static AssetStore getDefault()
        throws DatabaseException
    {
        for (AssetStore store : getMap().values()) {
            if (store.type == AssetStoreType.LOCAL) {
                return store;
            }
        }

        //
        // Otherwise return the first one in the map?
        //
        if (stores.values().iterator().hasNext()) {
            return stores.values().iterator().next();
        }

        return null;
    }

    /**
     * Store to the given database.
     */
    public void save(final Database db) throws DatabaseException {
        Table table = db.getTable(TABLE_NAME);

        if (id == NOT_SAVED) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillFormatter(formatter);

            id = table.insertSequencedRow(formatter, "id");

            //
            // TODO: Add this to our map!
            //
        } else {
            SqlUpdateFormatter formatter = new SqlUpdateFormatter();
            fillFormatter(formatter);

            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("id", id);
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }

    /**
     * Fill in formatter values from our properties.
     */
    private void fillFormatter(final SqlFormatter formatter) {
        formatter.append("name", name);
        formatter.append("type", type.name());
        formatter.append("config", config.configString());
        formatter.append("writable", writable);
    }

    /**
     * Delete this store from the given database.  Does not delete any
     * asset files.
     */
    public void delete(final Database db) throws DatabaseException {
        if (id == NOT_SAVED) return;

        Table table = db.getTable(TABLE_NAME);

        table.deleteRows("id = " + id);

        getMap().remove(id);
    }
}

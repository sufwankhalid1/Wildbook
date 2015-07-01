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

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;

import com.oreilly.servlet.multipart.FilePart;

import com.samsix.database.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ecocean.ShepherdPMF;

/**
 * AssetStore describes a location and methods for access to a set of
 * MediaAssets.  Concrete subtypes fill in the "hows".
 *
 * @see LocalAssetStore
 */
public abstract class AssetStore {
    private static Logger log = LoggerFactory.getLogger(AssetStore.class);
    private static final String TABLE_NAME = "assetstore";
    public static final long NOT_SAVED = -1;
    protected long id = NOT_SAVED;
    protected String name;
    protected AssetStoreType type = AssetStoreType.LOCAL;
    protected AssetStoreConfig config;
    protected boolean writable = true;


    /**
     * Create a new AssetStore.
     */
    protected AssetStore(long id, String name,
                         AssetStoreType type,
                         AssetStoreConfig config,
                         boolean writable)
    {
        if (name == null) throw new IllegalArgumentException("null name");
        if (type == null) throw new IllegalArgumentException("null type");

        this.id = id;
        this.name = name;
        this.type = type;
        this.config = config;
        this.writable = writable;
    }

    /**
     * Create a new AssetStore.  Used in load().
     */
    private static AssetStore buildAssetStore(long id,
                                              String name,
                                              AssetStoreType type,
                                              AssetStoreConfig config,
                                              boolean writable)
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

    /**
     * Return the store's internal (database) id.  Not really for
     * public use.
     */
    long getID() { return id; }

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
     * @param part File given in a form submission.
     *
     * @param path The (optional) subdirectory and (required) filename
     * relative to the asset store root in which to store the file.
     *
     * @param type Probably AssetType.ORIGINAL.
     */
    public abstract MediaAsset copyIn(FilePart part, String path, AssetType type)
        throws IOException;


    //
    // store/load
    //

    /**
     * Fetch the default store (the one with the highest id) from the
     * database.
     */
    public static AssetStore loadDefault(Database db)
        throws DatabaseException
    {
        // NOTE maybe someday indicate the default store name in
        // commonConfiguration.properties if we regularly use multiple
        // stores.
        return load(db, null, "id desc");
    }

    /**
     * Fetch a single store from the database by name.
     */
    public static AssetStore load(Database db, String name)
        throws DatabaseException
    {
        if (name == null) throw new IllegalArgumentException("null name");

        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("name", name);

        return load(db, where);
    }

    /**
     * Fetch a single store from the database by id.
     */
    public static AssetStore load(Database db, long id)
        throws DatabaseException
    {
        if (id < 1) throw new IllegalArgumentException("bad id");

        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("id", id);

        return load(db, where);
    }

    /**
     * Fetch a single store from the database.
     */
    public static AssetStore load(Database db, SqlWhereFormatter where)
        throws DatabaseException
    {
        if (where == null)
            throw new IllegalArgumentException("null where formatter");

        return load(db, where, null);
    }

    /**
     * Fetch a single store from the database.
     */
    public static AssetStore load(Database db,
                                  SqlWhereFormatter where,
                                  String orderBy)
        throws DatabaseException
    {
        // null "where" or orderBy is ok.
        String whereStr = where != null ? where.getWhereClause() : null;

        if (db == null)
            throw new IllegalArgumentException("null database");

        Table table = db.getTable(TABLE_NAME);

        RecordSet rs = table.getRecordSet(whereStr, orderBy);
        if (rs.next()) {
            return buildAssetStore(rs.getLong("id"),
                                   rs.getString("name"),
                                   AssetStoreType.valueOf(rs.getString("type")),
                                   new AssetStoreConfig(rs.getString("config")),
                                   rs.getBoolean("writable"));
        } else {
            return null;
        }
    }

    /**
     * Store to the given database.
     */
    public void save(Database db) throws DatabaseException {
        Table table = db.getTable(TABLE_NAME);

        if (id == NOT_SAVED) {
            SqlInsertFormatter formatter = new SqlInsertFormatter();
            fillFormatter(formatter);

            id = (int)table.insertSequencedRow(formatter, "id");
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
    private void fillFormatter(SqlFormatter formatter) {
        formatter.append("name", name);
        formatter.append("type", type.name());
        formatter.append("config", config.configString());
        formatter.append("writable", writable);
    }

    /**
     * Delete this store from the given database.  Does not delete any
     * asset files.
     */
    public void delete(Database db) throws DatabaseException {
        if (id == NOT_SAVED) return;

        Table table = db.getTable(TABLE_NAME);

        table.deleteRows("id = " + id);
    }
}

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
import java.net.URL;
import java.nio.file.Path;

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
 * MediaAsset describes a photo or video that can be displayed or used
 * for processing and analysis.
 */
public class MediaAsset {
    private static Logger log = LoggerFactory.getLogger(MediaAsset.class);
    private static final String TABLE_NAME = "mediaasset";
    public static final long NOT_SAVED = -1;
    public long id = NOT_SAVED;
    public AssetStore store;
    public Path path;
    public AssetType type;
    // public Set<Keyword> keywords;
    // public Set<Object> metadata;


    /**
     * Look for an existing asset with the given store and path.  If
     * one exists, return it.  If not, create, save, and return one.
     */
    protected static MediaAsset findOrCreate (Database db, AssetStore store,
                                              Path path, AssetType type)
    {
        try {
            MediaAsset ma = load(db, store, path);

            if (ma == null) {
                ma = new MediaAsset(store, path, type);
                ma.save(db);
            }

            return ma;
        } catch (DatabaseException e) {
            log.warn("finding or creating", e);
            return null;
        }
    }

    /**
     * To be called by AssetStore factory method.
     */
    protected MediaAsset(AssetStore store, Path path, AssetType type)
    {
        this(NOT_SAVED, store, path, type);
    }

    /**
     * To be called by load().
     */
    private MediaAsset(long id, AssetStore store, Path path, AssetType type)
    {
        if (store == null) throw new IllegalArgumentException("Null store");
        if (path == null) throw new IllegalArgumentException("Null path");
        if (type == null) throw new IllegalArgumentException("Null type");

        this.id = id;
        this.store = store;
        this.path = path;
        this.type = type;
    }

    //
    // asset file access
    //

    /**
     * Return a full web-accessible url to the asset, or null if the
     * asset is not web-accessible.
     */
    public URL webPath() {
        return store.webPath(this);
    }

    /**
     * Return the html to display this asset in a web page, or null if
     * it is not web-accessible.
     */
    public String getDOM() {
        URL path = webPath();

        if (path != null) {
            return String.format("<img src=\"%s\"/>", path);
        } else {
            return null;
        }
    }

    //
    // familial relationships
    //
    /*
    public MediaAsset getParent() {
        return null; // TODO - ask db on demand
    }

    public Set<MediaAsset> getChildren() {
        return null; // TODO - ask db on demand
    }

    // TODO MAYBE?
    public Set<MediaAsset> getChildren(AssetType type) {
        return null; // TODO - ask db on demand
    }
    */
    // findFirst(type) / findAll(type)
    /*
    public Set<MediaAsset> findChildren(AssetType type) {
        // NOTE we don't keep list of children!
        return null; // TODO - ask db on demand
    }

    public MediaAsset getThumbnail() {
        // TODO: best way to handle this?
        // like findChildren(), but return first of known type.
        return null;
    }
    */

    //
    // CRUD
    //

    /**
     * Fetch a single asset from the database by id.
     */
    public static MediaAsset load(Database db, long id)
        throws DatabaseException
    {
        if (id < 1) throw new IllegalArgumentException("bad id");

        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("id", id);

        return load(db, where);
    }

    /**
     * Fetch a single asset from the database by store and path.
     */
    public static MediaAsset load(Database db, AssetStore store, Path path)
        throws DatabaseException
    {
        if (store == null) throw new IllegalArgumentException("null store");
        if (path == null) throw new IllegalArgumentException("null path");

        SqlWhereFormatter where = new SqlWhereFormatter();
        where.append("store", store.id);
        where.append("path", path.toString());

        return load(db, where);
    }

    /**
     * Fetch a single asset from the database.
     */
    public static MediaAsset load(Database db, SqlWhereFormatter where)
        throws DatabaseException
    {
        if (db == null)
            throw new IllegalArgumentException("null database");
        if (where == null)
            throw new IllegalArgumentException("null where formatter");

        Table table = db.getTable(TABLE_NAME);

        RecordSet rs = table.getRecordSet(where.getWhereClause(), 1);
        if (rs.next()) {
            AssetStore store = AssetStore.load(db, rs.getLong("store"));
            Path path = new File(rs.getString("path")).toPath();

            return new MediaAsset(rs.getLong("id"),
                                  store,
                                  path,
                                  AssetType.valueOf(rs.getString("type")));
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

            id = table.insertSequencedRow(formatter, "id");
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
        formatter.append("store", store.id);
        formatter.append("path", path.toString());
        formatter.append("type", type.name());
    }

    /**
     * Delete this asset and any child assets from the given database.
     * Does not delete any asset files.
     *
     * @param db Database where the asset lives.
     */
    public void delete(Database db) throws DatabaseException {
        if (id == NOT_SAVED) return;

        Table table = db.getTable(TABLE_NAME);
        table.deleteRows("id = " + id);
    }
}

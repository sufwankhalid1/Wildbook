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

import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

/**
 * MediaAsset describes a photo or video that can be displayed or used
 * for processing and analysis.
 */
public class MediaAsset {
    protected int id = MediaAssetFactory.NOT_SAVED;

    protected AssetStore store;
    protected Path path;

    protected MediaAssetType type;

    protected String category;
    protected Set<String> tags;
    protected Integer parentId;
    protected Integer rootId;

    protected AssetStore thumbStore;
    protected Path thumbPath;
    protected Integer submitterid;


    /**
     * To be called by AssetStore factory method.
     */
    public MediaAsset(final AssetStore store, final Path path, final String category)
    {
        this(MediaAssetFactory.NOT_SAVED, store, path, MediaAssetType.fromFilename(path.toString()), category);
    }


    public MediaAsset(final AssetStore store, final Path path)
    {
        this(store, path, null);
    }


    public MediaAsset(final int id,
                      final AssetStore store,
                      final Path path,
                      final MediaAssetType type,
                      final String category)
    {
        this.id = id;
        this.store = store;
        this.path = path;
        this.type = type;
        this.category = category;
    }


    private URL getUrl(final AssetStore store, final Path path) {
        if (store == null) {
            return null;
        }

        return store.webPath(path);
    }

    private String getUrlString(final URL url) {
        if (url == null) {
            return null;
        }

        return url.toExternalForm();
    }


    public int getID()
    {
        return id;
    }

    public AssetStore getStore()
    {
        return store;
    }

    public Path getPath()
    {
        return path;
    }

    public MediaAssetType getType() {
        return type;
    }

    /**
     * Return a full web-accessible url to the asset, or null if the
     * asset is not web-accessible.
     */
    public URL webPath() {
        return getUrl(store, path);
    }

    public String webPathString() {
        return getUrlString(webPath());
    }

    public String thumbWebPathString() {
        return getUrlString(thumbWebPath());
    }


    /**
     * Return a full web-accessible url to the asset, or null if the
     * asset is not web-accessible.
     */
    public URL thumbWebPath() {
        return getUrl(thumbStore, thumbPath);
    }

    public void setThumb(final AssetStore store, final Path path)
    {
        thumbStore = store;
        thumbPath = path;
    }


    public Integer getSubmitterId() {
        return submitterid;
    }


    public void setSubmitterId(final Integer submitterid) {
        this.submitterid = submitterid;
    }
}

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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ecocean.rest.SimpleUser;

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
    protected Path midPath;
    //
    // Sometimes our queries only give us the submitterid and other times
    // we are able to retrieve the entire submitter. Hence this duplication here.
    //
    protected Integer submitterid;
    private SimpleUser submitter;
    protected LocalDateTime submittedOn;

    private LocalDateTime metaTimestamp;
    private Double metaLatitude;
    private Double metaLongitude;
    private Map<String, String> meta;


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

    public Path getFullPath() {
        return store.getFullPath(path);
    }

    public Path getThumbPath()
    {
        return thumbPath;
    }

    public Path getMidPath()
    {
        return midPath;
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

    public String midWebPathString() {
        return getUrlString(midWebPath());
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

    public AssetStore getThumbstore() {
        return thumbStore;
    }

    public URL midWebPath() {
        if (midPath == null) {
            return webPath();
        }

        //
        // Just use thumb store for now.
        //
        return getUrl(thumbStore, midPath);
    }

    public void setMid(final Path path) {
        //
        // Just use thumb store for now.
        //
        this.midPath = path;
    }

    public Integer getSubmitterId() {
        return submitterid;
    }

    public void setSubmitterId(final Integer submitterid) {
        this.submitterid = submitterid;
    }

    public SimpleUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(final SimpleUser submitter) {
        this.submitter = submitter;
    }

    public LocalDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(final LocalDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }

    public LocalDateTime getMetaTimestamp() {
        return metaTimestamp;
    }

    public void setMetaTimestamp(final LocalDateTime metaTimestamp) {
        this.metaTimestamp = metaTimestamp;
    }

    public Double getMetaLatitude() {
        return metaLatitude;
    }

    public void setMetaLatitude(final Double metaLatitude) {
        this.metaLatitude = metaLatitude;
    }

    public Double getMetaLongitude() {
        return metaLongitude;
    }

    public void setMetaLongitude(final Double metaLongitude) {
        this.metaLongitude = metaLongitude;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(final Map<String, String> meta) {
        this.meta = meta;
    }

    public void addMeta(final String key, final String value) {
        if (meta == null) {
            meta = new HashMap<>();
        }
        meta.put(key, value);
    }

    public void setParentInfo(final Integer parentId, final Integer rootId) {
        this.parentId = parentId;
        this.rootId = rootId;
    }

    public Integer getParentId() {
        return this.getParentId();
    }

    public Integer getRootId() {
        return this.getRootId();
    }
}

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
import java.lang.*;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;

import com.oreilly.servlet.multipart.FilePart;

import com.samsix.database.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalAssetStore references MediaAssets on the current host's filesystem.
 */
public class LocalAssetStore extends AssetStore {
    private static Logger log = LoggerFactory.getLogger(LocalAssetStore.class);
    private static final String KEY_ROOT = "root";
    private static final String KEY_WEB_ROOT = "webroot";


    /**
     * Create a new local filesystem asset store.
     *
     * @param name Friendly name for the store.
     *
     * @param root Filesystem path to the base of the asset directory.
     * Must not be null.
     *
     * @param webRoot Base web url under which asset paths are
     * appended.  If null, this store offers no web access to assets.
     *
     * @param wriable True if we are allowed to save files under the
     * root.
     */
    public LocalAssetStore(String name, Path root,
                           URL webRoot, boolean writable)
    {
        this(NOT_SAVED, name, makeConfig(root, webRoot), writable);
    }

    /**
     * Create a new local filesystem asset store.  Should only be used
     * internal to AssetStore.buildAssetStore().
     *
     * @param root Filesystem path to the base of the asset directory.
     * Must not be null.
     *
     * @param webRoot Base web url under which asset paths are
     * appended.  If null, this store offers no web access to assets.
     */
    LocalAssetStore(long id, String name,
                    AssetStoreConfig config, boolean writable)
    {
        super(id, name, AssetStoreType.LOCAL, config, writable);
    }

    /**
     * Create our config map.
     */
    private static AssetStoreConfig makeConfig(Path root, URL webRoot) {
        AssetStoreConfig config = new AssetStoreConfig();

        if (root != null) config.put(KEY_ROOT, root);
        if (webRoot != null) config.put(KEY_WEB_ROOT, webRoot);

        return config;
    }

    private Path root() { return config.getPath(KEY_ROOT); }
    private URL webRoot() { return config.getURL(KEY_WEB_ROOT); }

    /**
     * Create a new MediaAsset that points to an existing file under
     * our root.
     *
     * @param path Relative or absolute path to a file.  Must be under
     * the asset store root.
     *
     * @return The MediaAsset, or null if the path is invalid (not
     * under the asset root or nonexistent).
     */
    public MediaAsset create(Path path, AssetType type) {
        try {
            return new MediaAsset(this, ensurePath(root(), path), type);
        } catch (IllegalArgumentException e) {
            log.warn("Bad path", e);
            return null;
        }
    }

    /**
     * Create a new MediaAsset that points to an existing file under
     * our root.
     *
     * @param path Relative or absolute path to a file.  Must be under
     * the asset store root.
     *
     * @return The MediaAsset, or null if the path is invalid (not
     * under the asset root or nonexistent).
     */
    public MediaAsset create(String path, AssetType type) {
        try {
            Path p = new File(path).toPath();
            return new MediaAsset(this, ensurePath(root(), p), type);
        } catch (IllegalArgumentException e) {
            log.warn("Bad path", e);
            return null;
        }
    }

    /**
     * Create a new asset from the given form submission part.  The
     * file is copied in to the store as part of this process.
     *
     * @param file File to copy in.
     *
     * @param path The (optional) subdirectory and (required) filename
     * relative to the asset store root in which to store the file.
     *
     * @param type Probably AssetType.ORIGINAL.
     */
    public MediaAsset copyIn(Database db, File file,
                             String path, AssetType type)
        throws IOException
    {
        Path root = root();
        Path p = new File(path).toPath();
        Path subpath = checkPath(root, p);
        Path fullpath = root.resolve(subpath);

        fullpath.getParent().toFile().mkdirs();
        log.debug("copying from " + file + " to " + fullpath);

        Files.copy(file.toPath(), fullpath, REPLACE_EXISTING);

        return MediaAsset.findOrCreate(db, this, subpath, type);
    }

    /**
     * Make sure path is under the root, either passed in as a
     * relative path or as an absolute path under the root.
     *
     * @return Subpath to the file relative to the root.
     */
    public static Path checkPath(Path root, Path path) {
        if (path == null) throw new IllegalArgumentException("null path");

        Path result = root.resolve(path);
        result = root.relativize(result.normalize());

        if (result.startsWith(".."))
            throw new IllegalArgumentException("Path not under given root");

        return result;
    }

    /**
     * Like checkPath(), but throws an IllegalArgumentException if the
     * resulting file doesn't exist.
     *
     * @return Subpath to the file relative to the root.
     */
    public static Path ensurePath(Path root, Path path) {
        Path result = checkPath(root, path);

        Path full = root.resolve(path);
        if (!full.toFile().exists())
            throw new IllegalArgumentException(full + " does not exist");

        return result;
    }

    /**
     * Return a full URL to the given MediaAsset, or null if the asset
     * is not web-accessible.
     */
    public URL webPath(MediaAsset asset) {
        if (webRoot() == null) return null;
        if (asset == null) return null;

        try {
            return new URL(webRoot(), asset.path.toString());
        } catch (MalformedURLException e) {
            log.warn("Can't construct web path", e);
            return null;
        }
    }
}

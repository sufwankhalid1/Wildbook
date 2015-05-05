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
import java.lang.*;
// import java.util.Set;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.ecocean.*;

/**
 * LocalAssetStore keeps MediaAssets on the current host's filesystem.
 */
public class LocalAssetStore implements AssetStore {
    private static Logger log = LoggerFactory.getLogger(LocalAssetStore.class);
    private Path root;


    /**
     * Create a new store based at the root.
     */
    public LocalAssetStore(Path root) {
        this.root = root;
    }

    /**
     * Create a new MediaAsset that points to an existing file under
     * our root.
     *
     * @param path Relative or absolute path to a file.  Must be under
     * the asset store root.
     */
    public MediaAsset create(Path path) {
        try {
            return new MediaAsset(this, checkPath(root, path));
        } catch (IllegalArgumentException e) {
            log.warn("Bad path", e);
            return null;
        }
    }

    /**
     * Make sure path is under the root, either passed in as a
     * relative path or as an absolute path under the root.
     *
     * @return Subpath to the file relative to the root.
     */
    public static Path checkPath(Path root, Path path) {
        if (path == null) throw new IllegalArgumentException("null path");

        Path result = root.relativize(path);

        if (result.startsWith(".."))
            throw new IllegalArgumentException("Path not under given root");

        return result;
    }

    //MediaAsset importFrom(MediaAsset source);
}

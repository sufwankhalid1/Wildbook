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

import com.samsix.database.*;
import com.google.gson.*;
import com.google.gson.reflect.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ecocean.ShepherdPMF;

/**
 * AssetStoreConfig manages configuration variables for subclasses of
 * AssetStore.
 */
public class AssetStoreConfig {
    private static Logger log = LoggerFactory.getLogger(AssetStoreConfig.class);
    private Map<String, String> config;


    /**
     * Create an empty config.
     */
    public AssetStoreConfig() {
        config = new HashMap<>();
    }

    /**
     * Create a config by deserializing a string.
     */
    public AssetStoreConfig(String configString) {
        Type type = new TypeToken<HashMap<String, String>>() {}.getType();

        config = new Gson().fromJson(configString, type);
    }

    /**
     * Add a config variable.
     */
    public void put(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("null key");
        if (value == null) throw new IllegalArgumentException("null value");
        config.put(key, value.toString());
    }

    /**
     * Return a config variable of the URL type, or null if it doesn't
     * exist or cannot be converted to a URL.
     */
    public URL getURL(String key) {
        String value = config.get(key);

        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            log.warn("Can't convert to URL: " + value);
            return null;
        }
    }

    /**
     * Return a config variable of the Path type.
     */
    public Path getPath(String key) {
        return new File(config.get(key)).toPath();
    }

    /**
     * Return a config variable of the String type.
     */
    public String getString(String key) {
        return config.get(key);
    }

    /**
     * Serialize our config object to a storable string.
     */
    public String configString() {
        return new Gson().toJson(config);
    }

}

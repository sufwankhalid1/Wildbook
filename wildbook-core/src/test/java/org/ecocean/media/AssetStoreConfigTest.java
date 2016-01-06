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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Test AssetStoreConfig routines.
 */
public class AssetStoreConfigTest {

	@Test
    public void testConfigToString() {
        AssetStoreConfig config = new AssetStoreConfig();

		assertEquals("Empty config", "{}", config.configString());

        config.put("root", Paths.get("/tmp"));
		assertEquals("Simple config", "{\"root\":\"/tmp\"}", config.configString());
    }

	@Test
    public void testGetTypes() {
        try {
            AssetStoreConfig config = new AssetStoreConfig();

            Path path = Paths.get("/tmp");
            URL url = new URL("http://example.com/assets");

            config.put("root", path);
            config.put("webRoot", url);

            assertEquals("Path", path, config.getPath("root"));
            assertEquals("URL", url, config.getURL("webRoot"));
        } catch (MalformedURLException e) {
            fail("Bad URL format");
        }
    }

    @Test
    public void testGetConfigString() {
        AssetStoreConfig config = new AssetStoreConfig();

        String string = "/tmp";
        Path path = Paths.get(string);

        config.put("root", path);

        assertEquals("String", string, config.getString("root"));
    }
}

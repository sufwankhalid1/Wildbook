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

import org.junit.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalAssetStoreTest tests LocalAssetStore.
 */
public class LocalAssetStoreTest {
    private static Logger log = LoggerFactory.getLogger(LocalAssetStoreTest.class);

    /** Subdir of resources dir that contains our test resource files. */
    private final static String TEST_DIR = "media";
    private final static String TEST_FILE = "test.txt";
    private final static String TEST_SUBPATH = TEST_DIR + File.separator + TEST_FILE;


    /**
     * Make sure webPath() works correctly.
     */
    @Test
    public void checkWebPath() {
        AssetStore las = new LocalAssetStore("test", rootPath(), webRoot(), true);

        MediaAsset ma = las.create(testFilePath(), AssetType.ORIGINAL);
        assertNotNull("Null MediaAsset", ma);

        URL url = ma.webPath();
        assertNotNull("Null URL", url);

        assertEquals("Bad URL", "http://example.com/assets/media/test.txt", url.toString());

        // now with more nulls!
        las = new LocalAssetStore("test", rootPath(), null, true);

        ma = las.create(testFilePath(), AssetType.ORIGINAL);
        assertNotNull("Null MediaAsset", ma);
        url = ma.webPath();
        assertNull("Not null URL", url);
    }

    /**
     * Make sure we can't pass a null path to checkPath().
     */
	@Test
    public void checkPathNullPath() {
        try {
            LocalAssetStore.checkPath(rootPath(), null);
            fail("Didn't throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // kind of redundant i suppose
        }
    }

    /**
     * Make sure we handle subpaths in checkPath().
     */
    @Test
    public void checkPathSubPath() {
        Path root = rootPath();
        Path path = testFilePath();

        // good file
        Path result = LocalAssetStore.checkPath(root, path);

        assertNotNull(result);
        assertEquals(new File(TEST_SUBPATH).toPath(), result);

        // make sure we fail if file missing
        path = new File("nonexistent").toPath();
        try {
            result = LocalAssetStore.ensurePath(root, path);
            fail("Didn't throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // yay!
        }
    }

    /**
     * Make sure we handle absolute paths in checkPath().
     */
    @Test
    public void checkPathAbsPath() {
        Path root = rootPath();

        // build abs path ourself
        Path path = root.resolve(testFilePath());
        Path result = LocalAssetStore.checkPath(root, path);

        assertNotNull(result);
        assertEquals(new File(TEST_SUBPATH).toPath(), result);

        // abs path outside root (should fail)
        path = new File("/tmp/blahblah.txt").toPath();
        try {
            result = LocalAssetStore.checkPath(root, path);
            fail("Didn't throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // yay!
        }
    }

    /**
     * Return the path to the root resource directory.
     */
    private Path rootPath() {
        return testFilePath().getParent().getParent();
    }

    /**
     * Return the path to a test file.
     */
    private Path testFilePath() {
        try {
            // need initial '/' to load from resource root dir
            String path = File.separator + TEST_SUBPATH;
            URL url = getClass().getResource(path);
            assertNotNull(url);
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return an example web root url for testing.
     */
    private URL webRoot() {
        try {
            return new URL("http://example.com/assets/");
        } catch (MalformedURLException e) {
            log.warn("Can't construct test web root", e);
            return null;
        }
    }
}

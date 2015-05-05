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


public class LocalAssetStoreTest {
    /** Subdir of resources dir that contains our test resource files. */
    private final static String TEST_DIR = "media";
    private final static String TEST_FILE = "test.txt";
    private final static String TEST_SUBPATH = TEST_DIR + File.separator + TEST_FILE;


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
            result = LocalAssetStore.checkPath(root, path);
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


    /** Return the path to the root resource directory. */
    private Path rootPath() {
        return testFilePath().getParent().getParent();
    }

    /** Return the path to a test file. */
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

    // @Before
    // public void setUp() {

    // }

    // @After
    // public void tearDown() {

    // }
}

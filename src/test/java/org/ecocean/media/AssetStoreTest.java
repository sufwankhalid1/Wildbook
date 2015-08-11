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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.ecocean.ShepherdPMF;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

/**
 * Test AssetStore routines.
 */
public class AssetStoreTest {
    private static Logger log = LoggerFactory.getLogger(AssetStoreTest.class);

    @Test
    public void testSaveLoadDelete() {
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo("Test");

        if (ci == null) return;

        try (Database db = new Database(ci)) {
            String name = "test store";
            AssetStore store;

            // clean up possible leftovers from a previous run
            store = AssetStore.get(name);
            if (store != null) {
                store.delete(db);
            }

            // should be gone now
            store = AssetStore.get(name);
            assertNull("Leftover store not deleted", store);

            // new from scratch
            store = new LocalAssetStore(name, null, null, true);
            store.save(db);
            long id = store.id;
            assertTrue("ID not set", id != AssetStore.NOT_SAVED);

            // load by id
            store = AssetStore.get(store.id);
            assertNotNull("Store not loaded by id", store);

            // load by name
            store = AssetStore.get(name);
            assertNotNull("Store not loaded by name", store);

            // load default
            store = AssetStore.getDefault();
            assertNotNull("Default store not loaded", store);

            // delete
            store.delete(db);
            store = AssetStore.get(name);
            assertNull("New store not deleted", store);

        } catch (DatabaseException ex) {
            fail(ex.getMessage());
        }
    }
}

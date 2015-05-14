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

import java.util.*;

import com.samsix.database.*;
import org.ecocean.ShepherdPMF;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test AssetStore routines.
 */
public class AssetStoreTest {

    @Test
    public void testSaveLoadDelete() {
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo("Test");

        try (Database db = new Database(ci)) {
            String name = "test store";

            // clean up possible leftovers from a previous run
            AssetStore store = AssetStore.load(db, name);
            if (store != null) {
                store.delete(db);
            }

            // should be gone now
            store = AssetStore.load(db, name);
            assertNull("Leftover store not deleted", store);

            // new from scratch
            store = new LocalAssetStore(name, null, null, true);
            store.save(db);
            long id = store.getID();
            assertTrue("ID not set", id != AssetStore.NOT_SAVED);

            // load by id
            store = AssetStore.load(db, store.getID());
            assertNotNull("Store not loaded by id", store);

            // load by name
            store = AssetStore.load(db, name);
            assertNotNull("Store not loaded by name", store);

            // delete
            store.delete(db);
            store = AssetStore.load(db, name);
            assertNull("New store not deleted", store);

        } catch (DatabaseException ex) {
            fail(ex.getMessage());
        }
    }
}

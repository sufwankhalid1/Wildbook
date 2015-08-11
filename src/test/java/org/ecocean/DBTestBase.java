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

package org.ecocean;

import org.ecocean.media.AssetStore;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

/**
 * DBTestBase is a base class for tests that use the s6 test db.  It
 * ensures each test is run in an clean database.
 */
public class DBTestBase {
    private static Logger log = LoggerFactory.getLogger(DBTestBase.class);
    protected ConnectionInfo ci;

    @Before
    @After
    public void cleanTestDB() {
        ci = ShepherdPMF.getConnectionInfo("Test");

        if (ci == null) {
            return;
        }

        try (Database db = new Database(ci)) {
            // delete any existing asset stores left over from other
            // runs.  this will delete any referenced mediaassets too.
            for (AssetStore as = AssetStore.getDefault();
                 as != null;
                 as = AssetStore.getDefault())
            {
                as.delete(db);
            }
        } catch (DatabaseException ex) {
            log.warn("cleaning test db", ex);
        }
    }
}

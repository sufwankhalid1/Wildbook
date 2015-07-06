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

import java.util.*;
import java.nio.*;
import java.nio.file.*;

import org.ecocean.servlet.ServletUtilities;
import org.ecocean.media.*;

import com.samsix.database.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test some User routines.
 */
public class UserTest {
    private static Logger log = LoggerFactory.getLogger(UserTest.class);
    private static final String USERNAME = "test";


	@Test
    public void testSaveLoadDelete() {
        Shepherd shepherd = new Shepherd("Test");
        shepherd.beginDBTransaction();

        User user = createTestUser(shepherd);
        assertNotNull("Didn't create user", user);
        assertNull("Image already exists 1", user.getUserImageID());
        
        // test load
        user = shepherd.getUser(USERNAME);
        assertNotNull("Didn't load user", user);
        assertNull("Image already exists 2", user.getUserImageID());

        // test user image
        ConnectionInfo ci = ShepherdPMF.getConnectionInfo("Test");
        try (Database db = new Database(ci)) {
            db.beginTransaction();

            // NOTE: make image file doesn't need to exist for this test
            AssetStore las = new LocalAssetStore("test", Paths.get("/etc"), null, true);
            las.save(db);

            try {
                MediaAsset ma = las.create(Paths.get("/etc/hosts"), AssetType.ORIGINAL);
                assertNotNull("Null MediaAsset", ma);
                ma.save(db);

                user.setUserImage(ma);
            } catch (IllegalArgumentException iaex) {
                db.rollbackTransaction();
                fail(iaex.getMessage());
            }

            // NOTE: we're not actually persisting the user with the asset,
            // as that would require committing to the db which we
            // don't want to do for the test.

            MediaAsset ma2 = user.getUserImage();
            assertNotNull("Failed to retrieve image", ma2);
            
            // delete user image (not implemented)

            db.rollbackTransaction();
        } catch (DatabaseException ex) {
            fail(ex.getMessage());
        }

        //shepherd.commitDBTransaction();
        shepherd.rollbackDBTransaction();
        shepherd.closeDBTransaction();
    }

    private User createTestUser(Shepherd shepherd) {
        String salt = ServletUtilities.getSalt().toHex();
        String hashedPassword = ServletUtilities.hashAndSaltPassword("not2,bGuesd", salt);
        User user = new User(USERNAME, hashedPassword, salt);
        shepherd.getPM().makePersistent(user);
        //shepherd.commitDBTransaction();
        return user;
    }
}

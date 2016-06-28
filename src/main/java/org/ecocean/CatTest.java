/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean;

import org.json.JSONObject;
import org.json.JSONArray;
//import java.util.Vector;
//import org.apache.commons.lang3.builder.ToStringBuilder;

public class CatTest {
    private int id;
    private String username;
    private String trial;
    private long timestamp;
    private String results;

    public CatTest() {
    }

    public CatTest(String username, String trial, String results) {
        this.username = username;
        this.trial = trial;
        this.results = results;
        this.timestamp = System.currentTimeMillis();
    }


    public JSONArray getResultsAsJSONArray() {
        if (results == null) return null;
        return new JSONArray(results);
    }


    public static String currentTrial(Shepherd myShepherd) {
        return "test";
    }

    public static CatTest save(Shepherd myShepherd, String username, String trial, String results) {
        CatTest c = new CatTest(username, trial, results);
        myShepherd.getPM().makePersistent(c);
        return c;
    }


    public static boolean trialAvailableToUser(Shepherd myShepherd, String username) {
        return trialAvailableToUser(myShepherd, username, currentTrial(myShepherd));
    }
    public static boolean trialAvailableToUser(Shepherd myShepherd, String username, String trialName) {
        if (username == null) return false;
        return true;
    }

/*
    public String toString() {
        return new ToStringBuilder(this)
                .append(indexname)
                .append(readableName)
                .toString();
    }
*/

}

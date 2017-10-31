/*
 * Wildbook - A Mark-Recapture Framework
 * Copyright (C) 2017 Jason Holmberg
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

package org.ecocean.plugins.WildbookIA;

//import java.util.UUID;
import org.ecocean.Annotation;

//import java.util.Collections;
//import org.ecocean.Annotation;
//import org.ecocean.Util;
import java.util.HashMap;
//import org.json.JSONObject;
import java.util.List;
//import java.util.ArrayList;


public class Staging extends Annotmatch {
    //note that the following 3-tuple of (annot1, annot2, count) should be unique *regardless of review order*
        //TODO try to make this work with psql constraint + trigger??
    private long timestampServerStart;
    private long timestampServerEnd;
    private long timestampClientStart;
    private long timestampClientEnd;

    //TODO how to let count automagically incremented?
    public Staging(Annotation a1, Annotation a2, int ct) {
        super(a1, a2);
        count = ct;
    }

    public Staging(Annotation a1, Annotation a2) {
        super(a1, a2);
        count = 1;
    }

    public void setTimestampServerStart(long s) {
        timestampServerStart = s;
    }
    public long getTimestampServerStart() {
        return timestampServerStart;
    }

    public void setTimestampServerEnd(long s) {
        timestampServerEnd = s;
    }
    public long getTimestampServerEnd() {
        return timestampServerEnd;
    }

    public void setTimestampClientStart(long s) {
        timestampClientStart = s;
    }
    public long getTimestampClientStart() {
        return timestampClientStart;
    }

    public void setTimestampClientEnd(long s) {
        timestampClientEnd = s;
    }
    public long getTimestampClientEnd() {
        return timestampClientEnd;
    }

/*
    private static HashMap<String,Object> adderDataHack(List<Annotmatch> list) {
        HashMap<String,Object> map = Annotmatch.adderData((List<Annotmatch>)(List<?>) list);
        for (Annotmatch a : list) {
            Staging s = (Staging)a;
        }
        map.put("FOOOOOO", "bar");
        return map;
    }
*/

}

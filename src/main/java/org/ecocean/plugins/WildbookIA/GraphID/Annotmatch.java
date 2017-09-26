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

import java.util.UUID;
import java.util.Collections;

//import org.apache.commons.lang3.builder.ToStringBuilder;

public class Annotmatch {
    //note that the following 2-tuple should be unique *regardless of order*
        //TODO try to make this work with psql constraint + trigger??
    private UUID annot1;
    private UUID annot2;

    private String evidenceDecision;  //match|nomatch|notcomp|unknown|NULL
    private String metaDecision;  //same|different|NULL
    private String tags;  //semicolon-delimited
    private Double confidence;
    private String userId;
    private int count;

    private long timestampModified;

    public Annotmatch();

    public Annotmatch(UUID a1, UUID a2) {
        if ((a1 == null) || (a2 == null) || a1.equals(a2)) throw new RuntimeException("invalid Annotation IDs passed.");
        if (a1.compareTo(a2) < 0) {
            annot1 = a1;
            annot2 = a2;
        } else {
            annot1 = a2;
            annot2 = a1;
        }
        timestampModified = System.currentTimeMillis();
        count = 0;
    }

    public String[] tagsArray() {
        if (tags == null) return null;
        return tags.split(";");
    }

}
